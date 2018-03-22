/*
 * Created on Nov 26, 2010
 *
 * Copyright (c) 2010 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.ogg.vorbis;

import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Control;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.PipedInputStream;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class VorbisAudioFileWriterTest {

    public VorbisAudioFileWriterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getAudioFileTypes method, of class VorbisAudioFileWriter.
     */
    @Test
    public void testGetAudioFileTypes() {
        System.out.println("getAudioFileTypes");
        VorbisAudioFileWriter instance = new VorbisAudioFileWriter();

        Type[] result = instance.getAudioFileTypes();
        assertEquals(new Type("OGG", "ogg"), result[0]);
    }

    /**
     * Test of getAudioFileTypes method, of class VorbisAudioFileWriter.
     */
    @Test
    public void testGetAudioFileTypes_AudioInputStream() {
        System.out.println("getAudioFileTypes");
        AudioInputStream stream = null;
        VorbisAudioFileWriter instance = new VorbisAudioFileWriter();
        Type[] result = instance.getAudioFileTypes(stream);
        assertEquals(new Type("OGG", "ogg"), result[0]);
    }

    /**
     * Test of write method, of class VorbisAudioFileWriter.
     */
    @Test
    public void testWrite_3args_1() throws Exception {
        System.out.println("write");
        final int writeCount[] = {0};
        InputStream simpleSineInput = new InputStream() {

            @Override
            public int read() throws IOException {
                if (writeCount[0] % 2 == 0) {
                    writeCount[0]++;
                    return 0;
                } else {
                    return writeCount[0]++ % (44100 / 220) > 50 ? 100 : -100;
                }
            }

        };
        AudioInputStream stream = new AudioInputStream(simpleSineInput, new AudioFormat(44100, 16, 2, true, false), 88200);

        Type fileType = new Type("WAV", "wav");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        VorbisAudioFileWriter instance = new VorbisAudioFileWriter();
        try {
            instance.write(stream, fileType, out);
            fail("filetype shouldn't be accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not supported"));
        }
        fileType = new Type("OGG", "ogg");

        int written = AudioSystem.write(stream, fileType, out);
        assertTrue(written > 1);

        byte[] bytes = out.toByteArray();

        AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));

        assertEquals("VORBISENC", ais.getFormat().getEncoding().toString());
        AudioInputStream aisConverted = AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais);
        System.out.println(aisConverted.getFormat());

        int c = aisConverted.read();
        int readCount = 0;
        while (c != -1) {
            c = aisConverted.read();
            readCount++;
        }
        System.out.println("Original stream was " + writeCount[0] + " bytes, new stream is " + readCount + " bytes");
        assertTrue(writeCount[0] < readCount);
    }

    /**
     * Test of write method, of class VorbisAudioFileWriter.
     */
    @Test
    public void testWrite_3args_2() throws Exception {
        System.out.println("write");

        final int writeCount[] = {0};
        InputStream simpleSineInput = new InputStream() {

            @Override
            public int read() throws IOException {
                if (writeCount[0] % 2 == 0) {
                    writeCount[0]++;
                    return 0;
                } else {
                    return writeCount[0]++ % (44100 / 220) > 50 ? 100 : -100;
                }
            }

        };
        AudioInputStream stream = new AudioInputStream(simpleSineInput, new AudioFormat(44100, 16, 2, true, false), 88200);

        Type fileType = new Type("OGG", "ogg");
        File out = new File("test.ogg");
        int result = AudioSystem.write(stream, fileType, out);
        assertTrue(result > 1);

        AudioInputStream ais = AudioSystem.getAudioInputStream(out);
        assertEquals("VORBISENC", ais.getFormat().getEncoding().toString());

        AudioInputStream aisConverted = AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais);

        int c = aisConverted.read();
        int readCount = 0;
        while (c != -1) {
            c = aisConverted.read();
            readCount++;
        }
        System.out.println("Original stream was " + writeCount[0] + " bytes, new stream is " + readCount + " bytes");
        assertTrue(writeCount[0] < readCount);

    }

    @Test
    public void testWriteFromConstantStream() throws Exception {
        final int[] tdlReadCount = new int[]{0};
        TargetDataLine tdl = new TargetDataLine() {

            public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {

            }

            public void open(AudioFormat format) throws LineUnavailableException {

            }

            public byte read() {
                if (tdlReadCount[0] % 2 == 0) {
                    tdlReadCount[0]++;
                    return (byte) 0;
                } else {
                    return tdlReadCount[0]++ % (44100 / 220) > 50 ? (byte) 100 : (byte) -100;
                }
            }

            public int read(byte[] b, int off, int len) {
                for (int n = off; n < off + len; n++) {
                    b[n] = read();
                }
                return len;
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
                return new AudioFormat(44100, 16, 2, true, false);
            }

            public int getBufferSize() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int available() {
                throw new UnsupportedOperationException("Not supported yet.");
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
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void close() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isOpen() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Control[] getControls() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isControlSupported(Control.Type control) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Control getControl(Control.Type control) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addLineListener(LineListener listener) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void removeLineListener(LineListener listener) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        PipedInputStream snk = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(snk);

        final AudioInputStream ais = new AudioInputStream(tdl);
        new Thread() {

            @Override
            public void run() {
                try {
                    Type fileType = new Type("OGG", "ogg");

                    AudioSystem.write(ais, fileType, out);
                } catch (IOException ex) {
                    Logger.getLogger(VorbisAudioFileWriterTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }.start();

        AudioInputStream oggStream = AudioSystem.getAudioInputStream(snk);
        assertEquals("VORBISENC", oggStream.getFormat().getEncoding().toString());

        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), oggStream);

//        SourceDataLine sdl = AudioSystem.getSourceDataLine(pcmStream.getFormat());
//        sdl.open();
//        sdl.start();
        int readCount = 0;
        while (readCount < 1024 * 500) {
            byte[] frame = new byte[4];
            frame[0] = (byte) pcmStream.read();
            frame[1] = (byte) pcmStream.read();
            frame[2] = (byte) pcmStream.read();
            frame[3] = (byte) pcmStream.read();

//            sdl.write(frame, 0, 4);
            readCount += 4;
        }

        double secsBehind = (tdlReadCount[0] - readCount) / (4 * 44100d);
        System.out.println("Decoded stream is " + secsBehind + " seconds behind the original");
        assertTrue(secsBehind < 1.0);
    }
}
