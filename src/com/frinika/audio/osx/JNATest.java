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
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 *
 * @author Peter Salomonsen ( petersalomonsen.com )
 */
public class JNATest {
    

    public interface CLibrary extends Library {
	
	interface FrinikaAudioCallback extends Callback {
	    void invoke(int inNumberFrames,int inBusNumber,Pointer bufferLeft,Pointer bufferRight);
	}
	void startAudioWithCallback(FrinikaAudioCallback fn);

    }
    
    static double frequency = 500.0;
    static double phaseStep = (frequency / 44100.) * (Math.PI * 2.);
    static double currentPhase = 0;
    static long lastt = 0;
    public static void main(String[] args) throws Exception {
	CLibrary lib = (CLibrary)Native.loadLibrary("/Users/peter/Library/Developer/Xcode/DerivedData/frinikaosxaudio-bqwtuyohscfhsrgdhmbfrlhgprbm/Build/Products/Debug/libfrinikaosxaudio.dylib", CLibrary.class);

	
	CLibrary.FrinikaAudioCallback fn = new CLibrary.FrinikaAudioCallback() {
	    public final void invoke(int inNumberFrames,int inBusNumber,Pointer bufferLeft ,Pointer bufferRight) {
		System.out.println(inNumberFrames);
		for(int i = 0; i < inNumberFrames; i++) {
		    bufferLeft.setFloat(i*Native.getNativeSize(Float.TYPE), (float) Math.sin(currentPhase));		    
		    bufferRight.setFloat(i*Native.getNativeSize(Float.TYPE), (float) Math.sin(currentPhase));		    
		    currentPhase += phaseStep;
		}
		//System.out.println((System.currentTimeMillis()-lastt)+"");
		lastt = System.currentTimeMillis();
		//System.out.println(inBusNumber+" "+inBusNumber+" "+buffer);
	    }
	};  
	lib.startAudioWithCallback(fn);

	//lib.DoSomething();
	
	System.in.read();
	
	
    }
    
    
}
