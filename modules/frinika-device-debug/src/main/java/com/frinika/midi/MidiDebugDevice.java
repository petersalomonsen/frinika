package com.frinika.midi;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

/**
 *
 * Midi device that acts like a hub. Messages coming in are sent to all
 * recievers.
 * <pre>
 * MidiDebugDevice dev;
 *
 * Transmitter t1,t2; Reciever r1;
 *
 * dev.getReciever().addTransmitter(t1); dev.getReciever().addTransmitter(t2);
 *
 * r1.addTransmitter(dev.getTransmitter());
 * </pre>
 *
 * @author pjl
 * @version 1.0
 */
public class MidiDebugDevice implements MidiDevice {

    List<Transmitter> trans = new ArrayList<>();
    List<Receiver> recvs = new ArrayList<>();

    static class DeviceInfo extends MidiDevice.Info {

        DeviceInfo() {
            super("DebugDevice", "DrPJ", "debug device", "0.1a");
        }
    }
    static DeviceInfo info = new DeviceInfo();
    boolean isOpen;

    class Rec implements Receiver {

        @Override
        public void close() {
            MidiDebugDevice.this.recvs.remove(this);
        }

        @Override
        public void send(MidiMessage mess, long timeStamp) {
            // byte a[] = mess.getMessage();

            // StringBuffer buff = new StringBuffer(" MESS: ");
            byte[] msgBytes = mess.getMessage();
            if (msgBytes[0] == -1 && msgBytes[1] == 0x51 && msgBytes[2] == 3) {
                int mpq = ((msgBytes[3] & 0xff) << 16) | ((msgBytes[4] & 0xff) << 8) | (msgBytes[5] & 0xff);
                // sequencer
                // .setTempoInBPM((int) (60000000f / mpq));
                // PJL remove cast to int
                System.out.println("DEBUG RECVIEVED TMEPO " + 60000000f / mpq);
            }

            if (mess instanceof ShortMessage) {

                ShortMessage shm = (ShortMessage) mess;

                switch (shm.getCommand()) {
                    case ShortMessage.CONTROL_CHANGE:
                        System.out.println("ControlChange :" + shm.getData1() + " " + shm.getData2());

                        break;

                    case ShortMessage.PITCH_BEND:

                        short low = (byte) shm.getData1();

                        short high = (byte) shm.getData2();

                        short val = (short) ((high << 7) | low);

                        System.out.println(" Pitch Change = " + high + ":" + low + "  " + val);
                        break;

                    default:

                        int cmd = shm.getCommand();
                        if (cmd != 240) {
                            int chn = shm.getChannel();

                            int dat1 = shm.getData1();
                            int dat2 = shm.getData2();

                            System.out.println(" cmd:" + cmd + " chn:" + chn + " data:" + dat1 + " " + dat2);

                        }
                }

                for (Transmitter t : MidiDebugDevice.this.trans) {
                    if (((Trans) t).recv != null) {
                        ((Trans) t).recv.send(mess, -1);
                    }
                }
            }
        }
    }

    class Trans implements Transmitter {

        Receiver recv;

        @Override
        public void close() {
            MidiDebugDevice.this.trans.remove(this);
            if (this.recv != null) {
                this.recv.close();
            }
            this.recv = null;
        }

        @Override
        public Receiver getReceiver() {
            return this.recv;
        }

        @Override
        public void setReceiver(Receiver recv) {
            this.recv = recv;
        }
    }

    public MidiDebugDevice() {
    }

    @Override
    public long getMicrosecondPosition() {
        return -1;
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return this.trans;
    }

    @Override
    public List<Receiver> getReceivers() {
        return this.recvs;
    }

    @Override
    public void close() {
    }

    @Override
    public Transmitter getTransmitter() {
        Trans t = new Trans();
        this.trans.add(t);
        return t;
    }

    @Override
    public Receiver getReceiver() {
        Rec r = new Rec();
        this.recvs.add(r);
        return r;
    }

    @Override
    public int getMaxTransmitters() {
        return -1;
    }

    @Override
    public int getMaxReceivers() {
        return -1;
    }

    @Override
    public MidiDevice.Info getDeviceInfo() {
        return info;
    }

    @Override
    public void open() {
        this.isOpen = true;
    }

    @Override
    public boolean isOpen() {
        return this.isOpen;
    }

    static public String eventToString(MidiMessage mess) {

        String ret = null;

        byte[] msgBytes = mess.getMessage();
        if (msgBytes[0] == -1 && msgBytes[1] == 0x51 && msgBytes[2] == 3) {
            int mpq = ((msgBytes[3] & 0xff) << 16) | ((msgBytes[4] & 0xff) << 8) | (msgBytes[5] & 0xff);
            // sequencer
            // .setTempoInBPM((int) (60000000f / mpq));
            // PJL remove cast to int
            ret = "TMEPO " + (60000000f / mpq);
        }

        if (mess instanceof ShortMessage) {

            ShortMessage shm = (ShortMessage) mess;

            switch (shm.getCommand()) {

                case ShortMessage.NOTE_ON:
                    return null;

                case ShortMessage.NOTE_OFF:
                    return null;

                case ShortMessage.CONTROL_CHANGE:
                    ret = "ControlChange :" + shm.getData1() + " " + shm.getData2();

                    break;

                case ShortMessage.PITCH_BEND:

                    short low = (byte) shm.getData1();

                    short high = (byte) shm.getData2();

                    short val = (short) ((high << 7) | low);

                    ret = " Pitch Change = " + high + ":" + low + "  " + val;
                    break;

                default:

                    int cmd = shm.getCommand();
                    if (cmd != 240) {
                        int chn = shm.getChannel();

                        int dat1 = shm.getData1();
                        int dat2 = shm.getData2();

                        ret = " cmd:" + cmd + " chn:" + chn + " data:" + dat1 + " " + dat2;

                    }
            }
        }

        return ret;
    }
}
