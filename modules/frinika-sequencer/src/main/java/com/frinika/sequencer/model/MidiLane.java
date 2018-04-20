/*
 * Created on Feb 22, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.model;

import com.frinika.audio.midi.MidiListProvider;
import com.frinika.audio.model.ControllerListProvider;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.midi.MidiMessageListener;
import com.frinika.model.EditHistoryRecordable;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.MidiResource;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.midi.MonitorReceiver;
import com.frinika.sequencer.patchname.MyPatch;
import com.frinika.sequencer.project.AbstractProjectContainer;
import com.frinika.synth.Synth;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.MySampler;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import com.frinika.tootX.MidiPeakMonitor;
import com.frinika.tootX.midi.MidiInDeviceManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.Icon;
import rasmus.midi.provider.RasmusSynthesizer;

public class MidiLane extends Lane implements RecordableLane {

    private static final long serialVersionUID = 7079152267067539976L;
    static Icon iconNoteLane = new javax.swing.ImageIcon(
            RasmusSynthesizer.class.getResource("/icons/midilane.png"));
    static Icon iconDrumLane = new javax.swing.ImageIcon(
            RasmusSynthesizer.class.getResource("/icons/drumlane.png"));
    transient FrinikaTrackWrapper ftw;
    MidiPart trackHeaderPart = null;
    ProgramChangeEvent programEvent;
    String voiceName;
    /**
     * Used to store the midi device/channel used when saving
     */
    Integer midiDeviceIndex;
    int midiChannel;
    /**
     * Stuff to let the GUI know what sort of lane it is.
     *
     */
    static public final int UNKNOWN_TYPE = 0;
    static public final int MELODIC = 1;
    static public final int DRUM = 2;
    static public final int SCORE = 4;
    static public final int META = 8;

    static int nameCount = 0;
    int laneType;
    MidiPlayOptions playOptions = new MidiPlayOptions(); // Jens
    String patchMapName;

    public String getPatchMapName() {
        return patchMapName;
    }

    public void setPatchMapName(String patchMapName) {
        this.patchMapName = patchMapName;
    }
    transient Collection<MidiMessageListener> midiMessageListeners = new HashSet<MidiMessageListener>(); // Jens
    transient MidiPeakMonitor peakMonitor = null;
    transient String keyNames[] = null;
    transient boolean isSolo;

    /* Serializable constructor */
    protected MidiLane() {
    }

    /**
     * Constructor for deepClone
     *
     * @param cloneMe
     */
    private MidiLane(MidiLane cloneMe) {
        super("Copy of " + cloneMe.getName(), cloneMe.frinikaProject);
        trackHeaderPart = (MidiPart) (cloneMe.trackHeaderPart.deepCopy(null));
        trackHeaderPart.lane = this;
        midiDeviceIndex = cloneMe.midiDeviceIndex;
        midiChannel = cloneMe.midiChannel;
        keyNames = cloneMe.keyNames;
        for (Part part : cloneMe.getParts()) {
            part.deepCopy(this);
        }

        ftw = cloneMe.ftw.getSequence().createFrinikaTrack();
        ftw.setMidiChannel(midiChannel);
        setUpKeys();

    }

    /**
     * Construct an empty lane
     *
     * @param ftw
     * @param project
     */
    public MidiLane(FrinikaTrackWrapper ftw, AbstractProjectContainer project) {
        super("Midi " + nameCount++, project);
        this.ftw = ftw;
        // cntrlList=new ControllerList(); // TODO different lists

        // We do not want this part to be in the part list
        trackHeaderPart = new MidiPart();
        trackHeaderPart.lane = this;
        programEvent = new ProgramChangeEvent(trackHeaderPart, 0, 0, 0, 0);
        programEvent.commitAdd();
        midiChannel = ftw.getMidiChannel();

    }

    public MidiPart getHeadPart() {
        return trackHeaderPart;
    }

    // Hide this because midi lane duplicates some of the state and we don't
    // want anyone to cheat.
    public FrinikaTrackWrapper getTrack() {
        return ftw;
    }

    public MidiDevice getMidiDevice() {
        return ftw.getMidiDevice();
    }

    /**
     *
     * @return The reciever associated with this lane
     */
    public Receiver getReceiver() {
        MidiDevice dev = ftw.getMidiDevice();

        if (dev == null) {
            return null;
        }

        try {
            // Jens:
            // return getTrack().getMidiDevice().getReceiver();
            Receiver r = ftw.getMidiDevice().getReceiver();
            if (r != null) {
                return new MonitorReceiver(midiMessageListeners, r);
            } else {
                return null;
            }
        } catch (MidiUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    public int getMidiChannel() {
        return ftw.getMidiChannel();
    }

    /**
     * The midi channel stored in the saved project
     *
     * @return
     */
    public int getStoredMidiChannel() {
        return midiChannel;
    }

    @Override
    public void restoreFromClone(EditHistoryRecordable object) {
        // TODO Auto-generated method stub
    }

    public ControllerListProvider getControllerList() {
        MidiDevice dev = getMidiDevice();
        if (dev == null) {
            return MidiResource.getDefaultControllerList();
        }
        if (dev instanceof MidiListProvider) {
            return ((MidiListProvider) dev).getControllerList();
        }
        // TODO default
        return MidiResource.getDefaultControllerList();
    }

    // TODO Can we purge these 2 and replace with mute ?
    public void attachFTW() {
        ftw.attachToSequence();
    }

    public void detachFTW() {
        ftw.detachFromSequence();
    }

    /*
     * public ProgramChangeEvent getPatch(){ return patch; }
     */
    /**
     * This is used to find the entry in Voice To Patch map.
     *
     * @return name associated with the midipatch (prog,bank) pair
     */
    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String name) {
        voiceName = name;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.midiChannel = ftw.getMidiChannel();
        if (ftw.getMidiDevice() != null) {
            this.midiDeviceIndex = frinikaProject.getMidiDeviceIndex(ftw.getMidiDevice());
        }
        out.defaultWriteObject();
    }

    @Override
    /**
     * This will set the correct MidiDevice when the project is reloaded
     */
    public void onLoad() {
        super.onLoad();
        ftw.setMidiChannel(midiChannel);
        if (midiDeviceIndex != null) {
            try {
                ftw.setMidiDevice(frinikaProject.getSequencer().listMidiOutDevices().get(midiDeviceIndex));
            } catch (Exception e) {
                System.out.println("WARNING: Was unable to connect to external midi device");
            }
        }
        setUpKeys();
    }

    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        // Create the Frinika Track Wrapper so that when reading MultiEvents the
        // MidiEvents can be generated as well

        /**
         * PJS: The sequence is created for the first time here - reason why not
         * in the ProjectContainer is because the ticksPerQuarterNote variable
         * might not be loaded before the object is entirely read. Chicken and
         * egg problem - but this solves it...
         */
        if (this.project.getSequence() == null) {
            this.project.createSequence();
        }

        this.ftw = this.project.getSequence().createFrinikaTrack();

        in.defaultReadObject();

        if (programEvent == null) { // previous versions
            trackHeaderPart = new MidiPart();
            trackHeaderPart.lane = this;
            programEvent = new ProgramChangeEvent(trackHeaderPart, 0, 0, 0, 0);
        }

        midiMessageListeners = new HashSet<>(); // Jens

        programEvent.commitAdd();
        //	getPlayOptions();

        // setUpKeys();
    }

    public void setProgram(int prog, int msb, int lsb) {
        programEvent.commitRemove();
        programEvent.setProgram(prog, msb, lsb);
        programEvent.commitAdd();
        setUpKeys();

    }

    public void setProgram(MyPatch patch) {
        programEvent.commitRemove();
        programEvent.setProgram(patch.prog, patch.msb, patch.lsb);
        programEvent.commitAdd();
        setUpKeys();

    }

    public MyPatch getProgram() {

        MyPatch patch = programEvent.getPatch();
        if (patch != null) {
            return patch;
        }

        FrinikaTrackWrapper track = ftw;
        int count = track.size();
        for (int i = 0; i < count; i++) {
            MidiEvent event = track.get(i);
            if (event.getTick() != 0) {
                return patch;
            }
            MidiMessage msg = event.getMessage();
            if (msg instanceof ShortMessage) {
                ShortMessage sms = (ShortMessage) msg;
                if (sms.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                    if (patch == null) {
                        patch = new MyPatch(0, 0, 0);
                    }
                    patch.prog = sms.getData1();
                }
                if (sms.getCommand() == ShortMessage.CONTROL_CHANGE) {
                    if (sms.getData1() == 0) {
                        if (patch == null) {
                            patch = new MyPatch(0, 0, 0);
                        }
                        patch.msb = sms.getData2();
                    }
                    if (sms.getData1() == 0x20) {
                        if (patch == null) {
                            patch = new MyPatch(0, 0, 0);
                        }
                        patch.lsb = sms.getData2();
                    }
                }
            }
        }
        return patch;
    }

    @Override
    public Selectable deepCopy(Selectable parent) {
        MidiLane clone = new MidiLane(this);

        return clone;
    }

    /**
     * Lanes just move down the list. SIlly generic interface.
     */
    @Override
    public void deepMove(long tick) {
    }

    @Override
    public void addToModel() {
        super.addToModel();
        onLoad();

    }

    @Override
    public boolean isRecording() {
        return frinikaProject.getSequencer().isRecording(this);
    }

    @Override
    public boolean isMute() {
        // return project.getSequencer().isMute(this);
        return playOptions.muted; // Jens
    }

    public boolean isSolo() {
        return isSolo;
        //	return project.getSequencer().isSolo(this);
    }

    @Override
    public void setRecording(boolean b) {

        MidiInDeviceManager.open(FrinikaGlobalProperties.MIDIIN_DEVICES_LIST.getStringList()); // Possibly redundant now?
        if (b) {
            frinikaProject.getSequencer().recordEnable(this);

        } else {
            frinikaProject.getSequencer().recordDisable(this);
        }
    }

    @Override
    public void setMute(boolean b) {
        // project.getSequencer().setMute(this,b);
        playOptions.muted = b; // Jens
    }

    public void setSolo(boolean b) {
        isSolo = b;
        // project.getSequencer().setSolo(this, b);
    }

    @Override
    public double getMonitorValue() {
        if (peakMonitor == null) {
            peakMonitor = new MidiPeakMonitor();
            addMidiMessageListener(peakMonitor);
        }

        return peakMonitor.getPeak();
    }

    // Jens:
    public MidiPlayOptions getPlayOptions() {
        if (playOptions == null) {
            playOptions = new MidiPlayOptions();
        } // hack to allow loading of older files
        return playOptions;
    }

    public void addMidiMessageListener(MidiMessageListener l) {
        midiMessageListeners.add(l);
    }

    public void removeMidiMessageListener(MidiMessageListener l) {
        midiMessageListeners.remove(l);
    }

    public void setMidiChannel(int channel) {
        midiChannel = channel;
        ftw.setMidiChannel(midiChannel);
        setUpKeys();
    }

    public boolean isDrumLane() {
        // System. out.println(" Chn =" + midiChannel);
        return (laneType == DRUM) || (laneType == UNKNOWN_TYPE && midiChannel == 9);
    }

    public void setMidiDevice(MidiDevice dev) {
        ftw.setMidiDevice(dev);
        setUpKeys();
    }

    public void setType(int type) {
        laneType = type;
        setUpKeys();
    }

    public int getType() {
        return laneType;
    }

    public void setKeyNames(String[] keyNames) {
        if (keyNames == null) {
            keyNames = null;
        } else {
            if (keyNames.length != 128) {

                String[] nn = new String[128];
                for (int i = 0; i < keyNames.length; i++) {
                    nn[i] = keyNames[i];
                }

                for (int i = keyNames.length; i < 128; i++) {
                    nn[i] = "";
                }
                System.err.println("Strange keyNames.length =" + keyNames.length);
//                try {
//                    throw new Exception("Strange keyNames.length =" + keyNames.length);
//                } catch (Exception ex) {
//                    Logger.getLogger(MidiLane.class.getName()).log(Level.SEVERE, null, ex);
//                }
                keyNames = nn;
            }
            this.keyNames = keyNames;
        }
        notifyFocusListeners();
    }

    // possibly .......,.
    @Deprecated
    protected void setUpKeys() {

        getPlayOptions();

        if (!isDrumLane()) {
            getPlayOptions().drumMapped = false;

        } else {
            if (playOptions.noteMap != null) {
                playOptions.drumMapped = true;
            }
        }
        notifyFocusListeners();

        if (true) {
            return;
        }

        keyNames = null;
        if (!isDrumLane()) {
            playOptions.drumMapped = false;

        } else {
            if (playOptions.noteMap != null) {
                playOptions.drumMapped = true;
            }
            Instrument inst = null;
            MidiDevice dev = getMidiDevice();

            if (dev instanceof SynthRack) {
                Synth syn = ((SynthRack) dev).getSynth(midiChannel);
                if (syn instanceof MySampler) {
                    keyNames = new String[128];
                    MySampler mys = (MySampler) syn;
                    SampledSoundSettings[][] ssss = mys.sampledSounds;

                    for (int i = 0; i < 128; i++) {
                        if (ssss[i][0] != null) {
                            keyNames[i] = ssss[i][0].toString();
                        }
                    }
                }
            } else if (dev instanceof SynthWrapper) {
                dev = ((SynthWrapper) dev).getRealDevice();
                if (dev instanceof Synthesizer) {
                    Synthesizer synth = (Synthesizer) dev;
                    //      System.out.println(synth);
                    MyPatch patch = getProgram();

                    Method getChannels = null;

                    //         System.out.println(" LANE PATCH " + patch);
                    Instrument insts[] = synth.getLoadedInstruments();
                    for (Instrument ins : insts) {
                        //        System.out.println(" INST :" + ins);
                        Instrument li = (Instrument) ins;

                        boolean[] channels = null;
                        try {
                            if (getChannels != null) {
                                if (getChannels.getDeclaringClass() != li.getClass()) {
                                    getChannels = null;
                                }
                            }
                            if (getChannels == null) {
                                getChannels = li.getClass().getMethod(
                                        "getChannels");
                            }
                            if (getChannels != null) {
                                channels = (boolean[]) getChannels.invoke(li, (Object[]) null);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                        }

//                         System. out.print(ins.getName() + " "
//                         + ins.getPatch().getBank() + " "
//                         + ins.getPatch().getProgram() + " ");
//                         for (int i = 0; i < li.getChannels().length; i++) {
//                         if (li.getChannels()[i])
//                         System. out.print(i + "|");
//                         }
//                        
//                         System. out.println(li.getChannels());
                        if (channels != null) {
                            if ((ins.getPatch().getProgram() == patch.prog) && channels[midiChannel]) {
                                inst = ins;
                                break;
                            }
                        }
                    }

                    if (inst == null) {
                        insts = synth.getAvailableInstruments();
                        for (Instrument ins : insts) {
                            Instrument li = (Instrument) ins;

                            boolean[] channels = null;
                            try {
                                if (getChannels != null) {
                                    if (getChannels.getDeclaringClass() != li.getClass()) {
                                        getChannels = null;
                                    }
                                }
                                if (getChannels == null) {
                                    getChannels = li.getClass().getMethod(
                                            "getChannels");
                                }
                                if (getChannels != null) {
                                    channels = (boolean[]) getChannels.invoke(li, (Object[]) null);
                                }
                            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                            }
                            // System. out.print(ins.getName() + " "
                            // + ins.getPatch().getBank() + " "
                            // + ins.getPatch().getProgram() + " ");
                            // for (int i = 0; i < li.getChannels().length; i++) {
                            // if (li.getChannels()[i])
                            // System. out.print(i + "|");
                            // }
                            //
                            // System. out.println(li.getChannels());
                            if (channels != null) {
                                if ((ins.getPatch().getProgram() == patch.prog) && channels[midiChannel]) {
                                    inst = ins;
                                    break;
                                }
                            }
                        }
                    }

                    if (inst != null) {
                        try {
                            Method getKeys = inst.getClass().getMethod(
                                    "getKeys");
                            if (getKeys != null) {
                                keyNames = (String[]) getKeys.invoke(inst, (Object[]) null);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                        }
                    }
                }
            }
        }

        // System. out.println(" Setup keynames " + keyNames);
        notifyFocusListeners();
    }

    private void notifyFocusListeners() {
        Part part = frinikaProject.getPartSelection().getFocus();
        if (part == null) {
            return;
        }
        if (part.getLane() == this) {
            frinikaProject.getPartSelection().setDirty();
            frinikaProject.getPartSelection().notifyListeners();
        }
    }

    public String[] getKeyNames() {
        return keyNames;
    }

    public void setDrumMapping(int k, int index) {
        playOptions.drumMapped = true;
        if (playOptions.noteMap == null) {
            playOptions.noteMap = new int[128];
            for (int i = 0; i < 128; i++) {
                playOptions.noteMap[i] = i;
            }
        }
        playOptions.noteMap[k] = index;

        System.out.println(k + "--->" + index);
        notifyFocusListeners();
    }

    public int mapNote(int num) {
        if (playOptions.drumMapped) {
            return playOptions.noteMap[num];
        } else {
            return num;
        }
    }

    @Override
    public Part createPart() {
        return new MidiPart(this);
    }

    @Override
    public Icon getIcon() {
        if (isDrumLane()) {
            return iconDrumLane;
        } else {
            return iconNoteLane;
        }
    }

    /**
     * The index of the midiDevice according to the stored order in the saved
     * project
     *
     * @return
     */
    public Integer getMidiDeviceIndex() {
//
        // PJL added this (not sure about this)
        if (midiDeviceIndex == null) {
            this.midiChannel = ftw.getMidiChannel();
            if (ftw.getMidiDevice() != null) {
                this.midiDeviceIndex = frinikaProject.getMidiDeviceIndex(ftw.getMidiDevice());
            }
        }
//        //-----------------

        return midiDeviceIndex;
    }
}
