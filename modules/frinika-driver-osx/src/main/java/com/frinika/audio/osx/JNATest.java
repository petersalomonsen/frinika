/*
 * Created on May 13, 2016
 *
 * Copyright (c) 2005-2016 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.audio.osx;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author Peter Salomonsen ( petersalomonsen.com )
 */
public class JNATest {

    public static interface CLibrary extends Library {

        static interface FrinikaAudioCallback extends Callback {

            void invoke(int inNumberFrames, int inBusNumber, Pointer bufferLeft, Pointer bufferRight);
        }

        void startAudioWithCallback(FrinikaAudioCallback fn);

    }
    static Thread currentThread;

    static double frequency = 500.0;
    static double phaseStep = (frequency / 44100.) * (Math.PI * 2.);
    static double currentPhase = 0;
    static long lastt = 0;

    public static void main(String[] args) throws Exception {
        String nativeLibName = "libfrinikaosxaudio.dylib";
        InputStream is = OSXAudioServer.class.getResourceAsStream(nativeLibName);

        File tmpLibFile = new File(System.getProperty("java.io.tmpdir"), nativeLibName);
        System.out.println("Extracting libfrinikaosxaudio.dylib native library to " + tmpLibFile.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(tmpLibFile);
        byte[] buf = new byte[1024];
        int len = is.read(buf);
        while (len > -1) {
            fos.write(buf, 0, len);
            len = is.read(buf);
        }
        fos.close();

        CLibrary lib = (CLibrary) Native.loadLibrary(tmpLibFile.getAbsolutePath(), CLibrary.class);

        final CLibrary.FrinikaAudioCallback fn = (int inNumberFrames, int inBusNumber, Pointer bufferLeft, Pointer bufferRight) -> {
            if (currentThread != Thread.currentThread()) {
                currentThread = Thread.currentThread();
                Native.detach(false);
            }
            for (int i = 0; i < inNumberFrames; i++) {
                bufferLeft.setFloat(i * Native.getNativeSize(Float.TYPE), (float) Math.sin(currentPhase));
                bufferRight.setFloat(i * Native.getNativeSize(Float.TYPE), (float) Math.sin(currentPhase));
                currentPhase += phaseStep;
            }
        };
        lib.startAudioWithCallback(fn);

        System.in.read();

    }

}
