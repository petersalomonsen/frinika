/*
 * Created on Jan 5, 2014
 *
 * Copyright (c) 2004-2014 Peter Johan Salomonsen (www.petersalomonsen.com)
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
package com.frinika.audio.frogdisco;

import com.synthbot.frogdisco.CoreAudioRenderListener;
import com.synthbot.frogdisco.FrogDisco;
import com.synthbot.frogdisco.SampleFormat;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AbstractAudioServer;
import uk.org.toot.audio.server.AudioLine;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FrogDiscoAudioServer extends AbstractAudioServer implements CoreAudioRenderListener, ExtendedAudioServer {

    private FrogDisco frogDisco;
    boolean running = false;
    
    AudioBuffer audioOut;
    
    ExecutorService underrunExec = Executors.newSingleThreadExecutor();
    ExecutorService workExec = Executors.newSingleThreadExecutor();
    
    boolean isUnderrun = false;
    
    public FrogDiscoAudioServer() {
        bufferFrames = 128;
        
        try {
            FrogDiscoNativeLibInstaller.loadNativeLibs();
            createFrogDisco();
        } catch (Exception ex) {
            Logger.getLogger(FrogDiscoAudioServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createFrogDisco() {
        frogDisco = new FrogDisco(2, bufferFrames, 44100.0, SampleFormat.UNINTERLEAVED_FLOAT, 4, this);
    }
    
    @Override
    protected void startImpl() {
        running = true;
        frogDisco.play();
    }

    @Override
    protected void stopImpl() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public List<String> getAvailableOutputNames() {
        return Arrays.asList(new String[] {"CoreAudio"});
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
    public AudioBuffer createAudioBuffer(String name) {
        return super.createAudioBuffer(name); //To change body of generated methods, choose Tools | Templates.
    }

    
    @Override
    public IOAudioProcess openAudioInput(String string, String string1) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeAudioOutput(IOAudioProcess ioap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeAudioInput(IOAudioProcess ioap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getSampleRate() {
        return (float)frogDisco.getSampleRate();
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
    public void onCoreAudioShortRenderCallback(ShortBuffer sb) {

    }

    long expectedTime = 0;
    long callbackCount = 0;
    long startTime = 0;
    
    private void handleUnderrun(final long underrun) {
        isUnderrun = true;
        running = false;
        underrunExec.submit(new Runnable() {

            @Override
            public void run() {                        
                System.out.println(underrun+" ms underrun. Restart audio."); 

                workExec.shutdownNow();
                workExec = Executors.newSingleThreadExecutor();

                expectedTime=0;

                isUnderrun = false;

                frogDisco.pause();

                frogDisco = null;
                //System.runFinalization();
                System.gc();
                createFrogDisco();
                startImpl();
            }
        });
    }
    @Override
    public void onCoreAudioFloatRenderCallback(FloatBuffer buffer) {
        try {
            if(running && !isUnderrun) {
                if(expectedTime==0) {
                    startTime = System.currentTimeMillis();
                    expectedTime = startTime;
                }

                expectedTime = (long) (startTime+(callbackCount++)*
                        1000.0*(frogDisco.getBlockSize()/frogDisco.getSampleRate()));

                final long underrun = System.currentTimeMillis()-expectedTime;

                if(underrun>5) {                                    
                    int len = buffer.capacity();
                    for (int i = 0; i < len; i++) {
                        buffer.put(0);                        
                    }      
                    
                    handleUnderrun(underrun);
                    return;
                } else {       
                    Future f = workExec.submit(new Runnable() {

                        @Override
                        public void run() {
                            work();
                        }
                    });
                    try {
                        f.get((long)(1000000.0*(frogDisco.getBlockSize()/frogDisco.getSampleRate())), TimeUnit.MICROSECONDS);
                    } catch (Exception ex) {
                        f.cancel(true);
                        handleUnderrun(-1);
                    }
                }

                final int length = buffer.capacity();
                final int channels = frogDisco.getNumOutputChannels();

                if(audioOut!=null && audioOut.getSampleCount()==length/channels) {
                    final int sampleCount = audioOut.getSampleCount();
                    for(int c=0;c<channels;c++) {
                        for (int i = 0; i < sampleCount; i++) {
                            buffer.put(audioOut.getChannel(c)[i]);                        
                        }
                    }
                } else {
                    int len = buffer.capacity();
                    for (int i = 0; i < len; i++) {
                        buffer.put(0);                        
                    }                                
                }
            } else {
                int len = buffer.capacity();
                for (int i = 0; i < len; i++) {
                    buffer.put(0);                        
                }       
            }
        } catch(Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"",e);
        }
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
    public void setLatencyMilliseconds(float f) {
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
    public void setBufferMilliseconds(float f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetMetrics(boolean bln) {
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
        return "FrogDisco";
    }
    
}
