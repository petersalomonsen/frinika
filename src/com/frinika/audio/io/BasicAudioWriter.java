
/*
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
package com.frinika.audio.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.sound.sampled.AudioFormat;

/**
 * 
 *  minimal implementation of a wav writer. Writes bytes data.
 *
 * @author pjl
 */
public class BasicAudioWriter {

    protected byte[] byteBuffer;
    int count = 0;
    File file = null;
    protected RandomAccessFile fis;
    protected int nChannel;

    public BasicAudioWriter(File file, AudioFormat format) throws IOException {

        this.file = file;
        this.fis = new RandomAccessFile(file, "rw");
        this.fis.setLength(0);

        fis.seek(0);
        fis.write("RIFF".getBytes(), 0, 4);
        writeInt(36, fis);
        fis.write("WAVE".getBytes(), 0, 4);
        fis.write("fmt ".getBytes(), 0, 4);
        writeInt(0x10, fis);


        writeShort(1, fis);

        writeShort(nChannel = format.getChannels(), fis);

        // frame rate
        writeInt((int) (format.getFrameRate()), fis);

        // byte rate
        writeInt((int) (format.getFrameRate()) * format.getChannels() * 2, fis);

        // block align (bytes per frame ?)
        writeShort((short) format.getChannels() * 2, fis);
        writeShort(16, fis);
        fis.write("data".getBytes(), 0, 4);
        writeInt(0, fis); // 4

    }

    public void close() {
        if (fis == null) {
            return;
        }
        if (count == 0) {
            discard();
            return;
        }
        long fileSize = 0;
        try {
            fileSize = fis.getFilePointer();
            fis.seek(4);
            writeInt((int) (fileSize - 8), fis);
            fis.seek(40);
            writeInt((int) count, fis);
            fis.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Closing " + file + "  size/count:" + fileSize + "/" + count);
        fis = null;
    }

    /**
     * If file ends up not being used then call this.
     *
     */
    public void discard() {
        if (fis == null) {
            return;
        }
        try {
            fis.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fis = null;
        file.delete();
        file = null;
    }

    protected void finalize() {
        // also check if count is zero DOes this ever get called ?
        if (fis == null) {
            return;
        }
        close();
    }

    public File getFile() {
        return file;
    }

    public void open() {
    }

    public void write(byte[] byteBuffer, int offSet, int n) throws IOException {
        fis.write(byteBuffer, offSet, n);
        count += n;
    }

    private void writeInt(int i, RandomAccessFile fis2) throws IOException {
        byte[] buff = new byte[4];
        buff[0] = (byte) (255 & i);
        buff[1] = (byte) (255 & (i >> 8));
        buff[2] = (byte) (255 & (i >> 16));
        buff[3] = (byte) (255 & (i >> 24));
        fis.write(buff, 0, 4);
    }

    private void writeShort(int i, RandomAccessFile fis2) throws IOException {
        byte[] buff = new byte[2];
        buff[0] = (byte) (255 & i);
        buff[1] = (byte) (255 & (i >> 8));
        fis.write(buff, 0, 2);
    }
}
