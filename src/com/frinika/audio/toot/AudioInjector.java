/*
 * Created on 23-Feb-2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.audio.toot;

import java.util.Vector;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/**
 *
 * AudioProcess that allows the preprocessing of the audio buffer by a set of clients.
 * Finally processed by the output.
 *
 * @author pjl
 */
public class AudioInjector implements AudioProcess {

	Vector<AudioProcess> clients = new Vector<AudioProcess>();
	AudioProcess output;

	public AudioInjector(AudioProcess process) {
		output=process;		
	}
	
	synchronized public AudioProcess getOutputProcess() {
		return output;
	}

	
	
	synchronized public void  setOutputProcess(AudioProcess output) {
		this.output=output;
	}

	public void open() {
		// TODO Auto-generated method stub

	}

	/**
	 * add a process to mix with the buffer
	 * @param process
	 */
	synchronized public void add(AudioProcess process) {
		clients.add(process);
	}


	synchronized public void remove(AudioProcess process) {
		clients.remove(process);
	}

	synchronized public int processAudio(AudioBuffer buffer) {
		for (AudioProcess client:clients) {
			client.processAudio(buffer);
		}
		return output.processAudio(buffer);
	}

	public void close() {
		// TODO Auto-generated method stub

	}

}
