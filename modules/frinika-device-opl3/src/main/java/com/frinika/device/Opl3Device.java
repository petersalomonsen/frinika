/*
 * Created on Oct 01, 2006
 *
 * Copyright (c) 2006 Peter Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.device;

import com.cozendey.opl3.OPL3;
import com.frinika.audio.gui.ChannelListProvider;
import com.frinika.audio.midi.MidiDeviceIconProvider;
import com.frinika.voiceserver.VoiceServer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import rasmus.midi.provider.RasmusSynthesizer;

public class Opl3Device implements Synthesizer, ChannelListProvider, MidiDeviceIconProvider, Mixer {

    private OPL3 opl = new OPL3();
    private static Icon DEVICE_ICON = new javax.swing.ImageIcon(RasmusSynthesizer.class.getResource("/icons/frinika.png"));
    MidiDevice.Info deviceInfo = new Opl3DeviceInfo();

    public Opl3Device(VoiceServer voiceServer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxPolyphony() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MidiChannel[] getChannels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadInstrument(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadInstrument(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Soundbank getDefaultSoundbank() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument[] getAvailableInstruments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument[] getLoadedInstruments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MidiDevice.Info getDeviceInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void open() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxReceivers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxTransmitters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Receiver> getReceivers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Transmitter> getTransmitters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] getList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Icon getIcon() {
        Icon icon = DEVICE_ICON;
        if (icon.getIconHeight() > 16 || DEVICE_ICON.getIconWidth() > 16) {
            BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = img.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            Image im = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            icon = new ImageIcon(im);
        }
        return icon;
    }

    @Override
    public Mixer.Info getMixerInfo() {
        return null;
    }

    @Override
    public Line.Info[] getSourceLineInfo() {
        return null;
    }

    @Override
    public Line.Info[] getSourceLineInfo(Line.Info info) {
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info[] getTargetLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLineSupported(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Line getLine(Line.Info info) throws LineUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxLines(javax.sound.sampled.Line.Info info) {
        return 0;
    }

    @Override
    public Line[] getSourceLines() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Line[] getTargetLines() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void synchronize(Line[] lines, boolean maintainSync) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unsynchronize(Line[] lines) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Line.Info getLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Control[] getControls() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isControlSupported(Control.Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Control getControl(Control.Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class Opl3DeviceInfo extends MidiDevice.Info {

        Opl3DeviceInfo() {
            super("OPL3 Device", "opl3.cozendey.com", "Emulator of Yamaha OPL3 chip", "0.1.0");
        }
    }
}
