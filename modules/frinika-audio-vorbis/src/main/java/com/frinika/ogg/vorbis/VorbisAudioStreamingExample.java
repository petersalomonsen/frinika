/*
 * Created on Dec 3, 2010
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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class VorbisAudioStreamingExample {

    public static void main(String[] args) throws Exception {
        AudioFormat orgFormat = new AudioFormat(44100, 16, 2, true, false);

        //final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("/home/peter/nydalstyggis.wav"));
        Mixer.Info info = AudioSystem.getMixerInfo()[0];
        System.out.println("Using " + info + " for input");
        TargetDataLine tdl = AudioSystem.getTargetDataLine(orgFormat, info);
        tdl.open();
        tdl.start();
        final AudioInputStream audioInputStream = new AudioInputStream(tdl);

        PipedInputStream snk = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(snk);

        new Thread() {

            @Override
            public void run() {
                try {
                    System.out.println("Written " + AudioSystem.write(audioInputStream, new Type("OGG", "ogg"), out) + " bytes");
                } catch (IOException ex) {
                    Logger.getLogger(VorbisAudioStreamingExample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }.start();

        AudioInputStream oggStream = AudioSystem.getAudioInputStream(snk);
        System.out.println(oggStream.getFormat());
        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(orgFormat, oggStream);

        SourceDataLine sdl = AudioSystem.getSourceDataLine(orgFormat);
        sdl.open();
        sdl.start();
        System.out.println("Audiooutput started");
        byte[] buf = new byte[1024];

        int len = pcmStream.read(buf, 0, buf.length);
        while (len != -1) {
            sdl.write(buf, 0, len);
            len = pcmStream.read(buf, 0, buf.length);
        }
    }
}
