/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.audio.osx;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 *
 * @author peter
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
