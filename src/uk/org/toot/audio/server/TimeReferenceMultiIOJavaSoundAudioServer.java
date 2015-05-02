/*
 * Created on May 1, 2009
 *
 * Copyright (c) 2004-2009 Peter J. Salomonsen
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


package uk.org.toot.audio.server;

import java.util.logging.Logger;

/**
 * Alternative to the TimedAudioServer where block time is calculated based on how much time left to buffer expires -
 * relying on the blocking to return precisely on time. This is another approach that might work better on some systems
 * (at least on my Linux system it does) - and the technique is to note down the start time of the audio server and
 * only block if the audio produced is more than time elapsed should indicate. This requires less precision in respect to
 * return of the blocking method.
 *
 * NOTE: Not all of the metrics and controls of the TimedAudioServer are implemented (yet).
 * 
 * @author Peter Johan Salomonsen
 */
public class TimeReferenceMultiIOJavaSoundAudioServer extends MultiIOJavaSoundAudioServer {
    private long jitterReferenceTime;
    private long workCount;
    private double bufferMillis; // Use local buffer millis as double for better precision

    @Override
    public void run() {
        try {
            hasStopped = false;
            isRunning = true;

            resetReference();
            
            while (isRunning) {
                sync(); // e.g. resize buffers if requested                
                work();

                while(isRunning && ((System.currentTimeMillis()-jitterReferenceTime)/bufferMillis) < workCount)
                {
                    try{ Thread.sleep(1); } catch(InterruptedException e) {}
                }
                workCount++;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        hasStopped = true;
    }

    void resetReference()
    {
        Logger.getLogger(this.getClass().getName()).info("Resetting jitter reference time and work count");
        jitterReferenceTime = System.currentTimeMillis();
        workCount = 0;
        bufferMillis = ( (double)calculateBufferFrames() / (double)getSampleRate() ) * 1000d;
    }

        /**
     * Called synchronously with the server to simplify concurrency issues.
     */
    @Override
    protected void sync() {
        float currentBufferMilliSeconds = getBufferMilliseconds();
        super.sync();
        if(currentBufferMilliSeconds != getBufferMilliseconds())
        {
            resetReference();
        }
    }

}
