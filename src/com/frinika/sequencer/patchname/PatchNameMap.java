package com.frinika.sequencer.patchname;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Synthesizer;

public class PatchNameMap implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Vector<Node> topList = new Vector<Node>();
    // these are used to build keynames whilst parsing a text file.
    transient private Node currentPatchNode;
    transient private String[] keyNames;
    // String mapName;

    public PatchNameMap(InputStream str) throws Exception {
        // mapName=file.getName();

        BufferedReader reader = new BufferedReader(new InputStreamReader(str));

        boolean readPatches = false;

        String line;

        while ((line = reader.readLine()) != null) {

            int opB = line.indexOf('[');
            if (opB == -1) {
                continue;
            }
            int clB = line.indexOf(']');

            if (clB == -1) {
                throw new Exception(" Parse error " + line + "<");
            }

            String cmd = line.substring(opB + 1, clB);
            String rest = line.substring(clB + 1);
            String name = rest.trim();

       //     System.out.println(cmd);

            if (!readPatches) {

                if (cmd.equals("define patchnames")) {
                    readPatches = true;
                }
            } else {
                if (cmd.charAt(0) == 'p') {
                    if (keyNames != null) {
                        currentPatchNode.keynames = keyNames;
                        keyNames = null;
                    }
                    patchName(cmd, name);

                } else if (cmd.charAt(0) == 'g') {
                    groupName(cmd, name);

                } else if (cmd.charAt(0) == 'k') {
                    if (keyNames == null) {
                        keyNames = new String[128];
                    }

                    int key = Integer.parseInt(cmd.substring(2));
                //    System.out.println(" keyname["+key+"]="+name);
                    keyNames[key] = name;
                }
            }
        }

        if (keyNames != null) {
            currentPatchNode.keynames = keyNames;
            keyNames = null;
        }
    }

    public PatchNameMap(Synthesizer synthesizer, int channel) {

  //      System.out.println(" Creating patch map for " + synthesizer.toString() + " channel =" + channel);
        Instrument[] loadedins = synthesizer.getLoadedInstruments();
        Instrument[] availins = synthesizer.getAvailableInstruments();
        Instrument[] availableInstruments;
        if (loadedins == availins) {
            availableInstruments = loadedins;
        } else {
            availableInstruments = new Instrument[loadedins.length + availins.length];
            int ix = 0;
            for (int i = 0; i < loadedins.length; i++) {
                availableInstruments[ix++] = loadedins[i];
            }
            for (int i = 0; i < availins.length; i++) {
                availableInstruments[ix++] = availins[i];
            }
        }

        Method getChannels = null;
        Method getKeys = null;
        if (availableInstruments.length > 0) {
            try {
                getChannels = availableInstruments[0].getClass().getMethod(
                        "getChannels");
            } catch (Exception e) {
            }
            try {
                getKeys = availableInstruments[0].getClass().getMethod(
                        "getKeys");
            } catch (Exception e) {
            }
        }

        for (Instrument instr : availableInstruments) {
   //         System.out.println(" Loading patch for " +instr.getName());
            Patch p = instr.getPatch();
            boolean[] channels = null;
            if (getChannels != null) {
                try {
                    channels = (boolean[]) getChannels.invoke(instr);
                } catch (Exception e) {
                }
            }
            String[] keynames = null;
            if (getKeys != null) {
                try {
                    keynames = (String[]) getKeys.invoke(instr);
                } catch (Exception e) {
                }
            }
            if (channels == null || channels[channel]) {
    //            System.out.println("OK for the channel ");
                MyPatch patch = new MyPatch(p.getProgram(), p.getBank() >> 7, p.getBank() & 0x7f);
                Node node = new Node(instr.getName(), patch);
                node.keynames = keynames;
                listAtLevel(0).add(node);

            } else {
 //              System.out.println("channel does not support it");

            }
        }
    }

    private void groupName(String cmd, String name) throws IOException {
        int level = Integer.parseInt(cmd.substring(1));
        listAtLevel(level).add(new Node(name, new Vector<Node>()));

    // System.out.println(" g"+level+ " > "+name);
    }

    private Vector<Node> listAtLevel(int level) {

        if (level == 1) {
            return topList;
        }
        Vector<Node> p = topList;

        while (level > 1) {
            p = (Vector<Node>) (p.lastElement().getData());
            level--;
        }
        return (Vector<Node>) p;
    }

    private void patchName(String cmd, String name) throws IOException {
        String toks[] = cmd.split(",");

        int level = Integer.parseInt(toks[0].substring(1));

        int prog = Integer.parseInt(toks[1].trim());
        int msb = Integer.parseInt(toks[2].trim());
        int lsb;

        if (toks.length < 4) {
       //     int msb_orig = msb;
            lsb = msb % 128;
            msb = msb / 128;
     //       System.out.println(msb_orig + " --- > " + msb + ":" + lsb);
        } else {
            lsb = Integer.parseInt(toks[3].trim());
        }

        MyPatch patch = new MyPatch(prog, msb, lsb);

        currentPatchNode = new Node(name, patch);
        listAtLevel(level).add(currentPatchNode);
    // System.out.println(" p"+level+ " > "+name + " " +
    // prog+"|"+msb+"|"+lsb);
    // TODO Auto-generated method stub

    }

    /**
     *
     * Find patches which contain the substring name
     *
     * @param name
     * @return
     */
    public List<MyPatch> getPatchesWithNamesLike(String name) {
        Vector<MyPatch> list = new Vector<MyPatch>();
        getPatchesWithNameLike(name, topList, list);
        return list;
    }

    private void getPatchesWithNameLike(String name, Vector<Node> root,
            List<MyPatch> list) {

        for (Node o : root) {
            if (o.getData() instanceof Vector) {
                getPatchesWithNameLike(name, (Vector<Node>) (o.getData()), list);
            } else if (o.toString().toLowerCase().contains(name)) {
                list.add((MyPatch) o.getData());
            }
        }

    }

    boolean contians(String name, Vector<Node> searchMe,
            Vector<Vector<Node>> ret) {

        for (Node o : searchMe) {

            if (o.getData() instanceof Vector) {
                if (contians(name, (Vector<Node>) (o.getData()), ret)) {
                    ret.insertElementAt((Vector<Node>) (o.getData()), 0);
                    System.out.println(" in list " + o.getData());
                    return true;
                }
            } else if (name.equals(o.toString())) {
                System.out.println(" FOUND NAME " + name);
                return true;
            } else {
                System.out.println(" It's not " + o.toString());
            }
        }
        return false;

    }

    public Vector<Node> getList() {
        // TODO Auto-generated method stub
        return topList;
    }

    public static void main(String args) throws FileNotFoundException, Exception {
        File file = new File("/home/pjl/frinika/patchnames/xgnames.txt");
        FileInputStream io = new FileInputStream(file);
        new PatchNameMap(io);
    }
}
