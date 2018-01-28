/*
 * Created on Feb 16, 2007
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

package uk.org.toot.audio.server;

import java.util.Hashtable;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 * 
 * Provides a layer which allows multiple IO open/close. 
 * real IO is closed when open+closed count =0
 * 
 * @author pjl
 *
 */
public class MultiIOJavaSoundAudioServer extends JavaSoundAudioServer  {

	Hashtable<String,IOAudioProcess> outputMap=new Hashtable<String,IOAudioProcess>(); 
	Hashtable<String,IOAudioProcess> inputMap=new Hashtable<String,IOAudioProcess>(); 
	
	public MultiIOJavaSoundAudioServer() {
	}
	
	public IOAudioProcess openAudioOutput(String name,String label)  throws Exception {
        if ( name == null ) {
            // use the first available output if null is passed
            name = getAvailableOutputNames().get(0);
            System.out.println(label+" null name specified, using "+name);
        }
		IOAudioProcess p=outputMap.get(name);
		System.out.println(name + "   " + p );
		if (p == null )
				outputMap.put(name,p=new AudioProcessWrapper(super.openAudioOutput(name, label)));
		return p;
	}

	
	public IOAudioProcess openAudioInput(String name,String label) throws Exception {
        if ( name == null ) {
            // use the first available output if null is passed
            name = getAvailableInputNames().get(0);
            System.out.println(label+" null name specified, using "+name);
        }
		IOAudioProcess p;
		if ((p=inputMap.get(name)) == null) 
			inputMap.put(name,p=new AudioProcessWrapper(super.openAudioInput(name, label)));
		return p;
	}

	class AudioProcessWrapper implements IOAudioProcess {

		IOAudioProcess process;
		int openCount=0;
		
		public AudioProcessWrapper(IOAudioProcess process) {
			this.process=process;
		}

		public void open() throws Exception {
			if (openCount== 0 ) process.open();
			openCount++;
		}

		public int processAudio(AudioBuffer buffer) {
			return process.processAudio(buffer);
		}

		public void close() throws Exception {
			openCount--;
			if (openCount==0) process.close();
			
		}

		public ChannelFormat getChannelFormat() {
			return process.getChannelFormat();
		}

		public String getName() {
			// TODO Auto-generated method stub
			return process.getName();
		}
				
	}

	public void closeAudioInput(IOAudioProcess input) {
		// TODO Auto-generated method stub
		
	}

	public void closeAudioOutput(IOAudioProcess output) {
		// TODO Auto-generated method stub
		
	}
}
