/*
 * Copyright (c) 2004-2010 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *
 * http://www.frinika.com
 *
 * This file is part of Frinika.
 *
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.frinika.radio;

import com.frinika.global.FrinikaConfig;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class RadioStreamTargetDataLine implements TargetDataLine {
    PipedInputStream queue = null;
    PipedOutputStream pos = null;
    int bufferSize;

    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
        queue = new PipedInputStream();
        this.bufferSize = bufferSize;
        try {
            pos = new PipedOutputStream(queue);
        } catch (IOException ex) {         
            throw new LineUnavailableException(ex.toString());
        }
    }

    public void open(AudioFormat format) throws LineUnavailableException {
        open(format, 1024*1024*8);
    }

    public int read(byte[] b, int off, int len) {
        try {
            return queue.read(b, off, len);
        } catch (IOException ex) {
            return -1;
        }
    }

    public void drain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void flush() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void start() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isActive() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AudioFormat getFormat() {
        return new AudioFormat((float) FrinikaConfig.sampleRate,16,2,true,false);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int available() {
        try {
            return queue.available();
        } catch (IOException ex) {
            Logger.getLogger(RadioStreamTargetDataLine.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public int getFramePosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLongFramePosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getLevel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Info getLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void open() throws LineUnavailableException {
        open(getFormat());
    }

    public void close() {
        try {
            queue.close();
        } catch (IOException ex) {
            Logger.getLogger(RadioStreamTargetDataLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            pos.close();
        } catch (IOException ex) {
            Logger.getLogger(RadioStreamTargetDataLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        queue = null;
        pos = null;
    }

    public boolean isOpen() {
        return queue !=null;
    }

    public Control[] getControls() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isControlSupported(Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control getControl(Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void addFrame(byte[] b) {
        try {
            if(queue!=null && queue.available()<(bufferSize-4))
            {
                pos.write(b);
            }
        } catch (IOException ex) {
        }
    }   

}
