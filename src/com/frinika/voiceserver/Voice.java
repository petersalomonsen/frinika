/*
 * Created on Sep 11, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.voiceserver;

import java.util.Vector;

/**
 * Voice base class. A voice is producing raw audio data by filling floats into the buffer
 * passed each time fillBuffer is called. 
 *  
 * @author Peter Johan Salomonsen
 *
 */
public abstract class Voice {
	public long startFramePos = 0;
	
	/**
	 * For process ordering you'll set nextVoice to be the voice to be processed after this voice. If you want
	 * the next voice to do effect processing on this voice only, you should use a separate buffer, and let nextVoice
	 * add this buffer to the main buffer when processed.
	 */
	public Voice nextVoice = null;
	public Vector<VoiceInterrupt> interrupts = new Vector<VoiceInterrupt>();

    /**
     * This is where the raw audio data should be produced. The passed in buffer contains the
     * audio data from the previous voice in the chain, thus new data should just be added 
     * to the buffer. It's important to just fill within the start and stop positions, because
     * this is how the interrupt functions control that parameter modification occur at the
     * right place.
     * @param startBufferPos
     * @param endBufferPos
     * @param buffer
     */
    public abstract void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer);
}