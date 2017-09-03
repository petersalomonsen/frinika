/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.audio.asio;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.org.toot.audio.core.AudioBuffer;
import static uk.org.toot.audio.core.AudioProcess.AUDIO_OK;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AbstractAudioServer;
import uk.org.toot.audio.server.AudioLine;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 *
 * @author Peter Salomonsen
 */
public class AsioAudioServer extends AbstractAudioServer implements ExtendedAudioServer, AsioDriverListener{
    AudioBuffer audioOut;
    AsioDriver asioDriver;
    private Set<AsioChannel> activeChannels;
    
    boolean running = false;

    @Override
    public void sampleRateDidChange(double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resyncRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bufferSizeChanged(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void latenciesChanged(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   /* The next block of samples is ready. Input buffers are filled with new input,
   * and output buffers should be filled at the end of this method.
   * @param sampleTime  System time related to sample position, in nanoseconds.
   * @param samplePosition  Sample position since <code>start()</code> was called.
   * @param activeChannels  The set of channels which are active and have allocated buffers. Retrieve
   * the buffers with <code>AsioChannel.getBuffer()</code>, or use <code>AsioChannel.read()</code>
   * and <code>AsioDriver.write()</code> in order to easily work with <code>float</code> arrays.
   */
    @Override
    public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels) {
        
        try {
            work();	
            activeChannels.forEach(channel -> {
                channel.write(audioOut.getChannel(channel.getChannelIndex()));
            });

        } catch(Exception e) {}
        
        
    }
    
    public AsioAudioServer() throws Exception  {   
        activeChannels = new HashSet<AsioChannel>();
        asioDriver = AsioDriver.getDriver(getAvailableOutputNames().get(0));
        asioDriver.addAsioDriverListener(this);
        activeChannels.add(asioDriver.getChannelOutput(0));
        activeChannels.add(asioDriver.getChannelOutput(1));
        
        bufferFrames = asioDriver.getBufferPreferredSize();
        
        asioDriver.createBuffers(activeChannels);
        asioDriver.start();

    }
    
    @Override
    protected void startImpl() {
	if(running) {
	    return;
	} 
        
	System.out.println("Asio Audio started");
	running = true;
    }

    @Override
    protected void stopImpl() {
        System.out.println("Stopping ASIO driver");
        asioDriver.shutdownAndUnloadDriver();
    }

    @Override
    public boolean isRunning() {
	return running;
    }

    @Override
    public List<String> getAvailableOutputNames() {
	return AsioDriver.getDriverNames();
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
	return "ASIO";
    }
    
    public static void main(String[] args) throws Exception {
	new AsioAudioServer().startImpl();
	
	System.in.read();
    }
}
