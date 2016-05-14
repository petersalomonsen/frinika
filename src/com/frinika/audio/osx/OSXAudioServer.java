/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.audio.osx;



import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.org.toot.audio.core.AudioBuffer;
import static uk.org.toot.audio.core.AudioProcess.AUDIO_OK;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AbstractAudioServer;
import uk.org.toot.audio.server.AudioLine;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 *
 * @author peter
 */
public class OSXAudioServer extends AbstractAudioServer implements ExtendedAudioServer{
    AudioBuffer audioOut;
    boolean running = false;
      
    public interface CLibrary extends Library {
	
	interface FrinikaAudioCallback extends Callback {
	    void invoke(int inNumberFrames,int inBusNumber,Pointer bufferLeft,Pointer bufferRight);
	}
	void startAudioWithCallback(FrinikaAudioCallback fn);

    }

    final CLibrary lib;
    final CLibrary.FrinikaAudioCallback fn;
    
    public OSXAudioServer() throws Exception  {   
	
	String nativeLibName = "libfrinikaosxaudio.dylib";
	InputStream is = OSXAudioServer.class.getResourceAsStream(nativeLibName);
	
	File tmpLibFile = new File(System.getProperty("java.io.tmpdir"),nativeLibName);
	System.out.println("Extracting libfrinikaosxaudio.dylib native library to "+tmpLibFile.getAbsolutePath());
	FileOutputStream fos = new FileOutputStream(tmpLibFile);
	byte[] buf = new byte[1024];
	int len = is.read(buf);
	while(len>-1)
	{
	    fos.write(buf,0,len);
	    len = is.read(buf);
	}
	fos.close();

	lib = (CLibrary)Native.loadLibrary(tmpLibFile.getAbsolutePath(), CLibrary.class);
	fn = new CLibrary.FrinikaAudioCallback() {
	    public final void invoke(int inNumberFrames,int inBusNumber,Pointer bufferLeft,Pointer bufferRight) {		
		if(bufferFrames==0) {
		    bufferFrames = inNumberFrames;	
		    for(int i = 0; i < inNumberFrames; i++) {
			bufferLeft.setFloat(i*Native.getNativeSize(Float.TYPE), 0);		    		    
			bufferRight.setFloat(i*Native.getNativeSize(Float.TYPE), 0);		    		    
		    }
		} else {
		    try {
			work();		
			for(int i = 0; i < inNumberFrames; i++) {
			    bufferLeft.setFloat(i*Native.getNativeSize(Float.TYPE), (float) audioOut.getChannel(0)[i]);		    		    
			    bufferRight.setFloat(i*Native.getNativeSize(Float.TYPE), (float) audioOut.getChannel(1)[i]);		    		    
			}		
		    } catch(Exception e) {}
		}	    
	    }
	};  
	
	lib.startAudioWithCallback(fn);
	
	// Wait for number of bufferframes to be set
	while(bufferFrames==0) {
	    try {
		Thread.sleep(10);
	    } catch (InterruptedException ex) {
		Logger.getLogger(OSXAudioServer.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }
    
    @Override
    protected void startImpl() {
	if(running) {
	    return;
	} 
	
	System.out.println("OSX Audio started");
	running = true;
    }

    @Override
    protected void stopImpl() {

    }

    @Override
    public boolean isRunning() {
	return running;
    }

    @Override
    public List<String> getAvailableOutputNames() {
	return Arrays.asList(new String[] {"AUGraph"});
    }

    @Override
    public List<String> getAvailableInputNames() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    		
		
    @Override
    public IOAudioProcess openAudioOutput(String name, String label) throws Exception {  
        AudioLine line = new AudioLine() {

            @Override
            public ChannelFormat getChannelFormat() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void open() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int processAudio(AudioBuffer ab) {
		
                if ( !ab.isRealTime() ) {
                    return AUDIO_OK;
                }
                
                audioOut = ab;
		//System.out.println(ab.getChannelCount()+" "+ab.getSampleCount()+" "+ab.getSampleRate());
                return AUDIO_OK;
            }

            @Override
            public void close() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getLatencyFrames() {
                return bufferFrames;
            }
        };
        return line;
    }

    @Override
    public IOAudioProcess openAudioInput(String name, String label) throws Exception {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeAudioOutput(IOAudioProcess output) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeAudioInput(IOAudioProcess input) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getSampleRate() {
	return 44100;
    }

    @Override
    public int getInputLatencyFrames() {
        return bufferFrames;
    }

    @Override
    public int getOutputLatencyFrames() {
        return bufferFrames;
    }

  @Override
    public int getTotalLatencyFrames() {
        return getInputLatencyFrames()+getOutputLatencyFrames();
    }
    
    @Override
    public int getSampleSizeInBits() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBufferUnderRuns() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getLowestLatencyMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getActualLatencyMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLatencyMilliseconds(float latencyMilliseconds) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getMinimumLatencyMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getMaximumLatencyMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getBufferMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBufferMilliseconds(float bufferMilliseconds) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getLatencyMilliseconds() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetMetrics(boolean resetUnderruns) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<AudioLine> getOutputs() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<AudioLine> getInputs() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getConfigKey() {
	return "AUGraph";
    }
    
    public static void main(String[] args) throws Exception {
	new OSXAudioServer().startImpl();
	
	System.in.read();
    }
}
