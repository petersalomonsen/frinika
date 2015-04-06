/*
 * Created on Dec 12, 2004
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


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import com.frinika.global.FrinikaConfig;

/**
 * @author peter
 *
 * PJL: Does not seem to be used  ?
 *
 *@deprecated
 */
public class MonoAudioInput {
	TargetDataLine lineIn;
	AudioFormat format = new AudioFormat((float) FrinikaConfig.sampleRate,16,1,true,true);
	
	public MonoAudioInput(TargetDataLine in) throws Exception
	{	/*
		if(AudioContext.getDefaultAudioContext().getVoiceServer() instanceof AudioOutput)
		{
			lineIn = com.frinika.audio.drivers.jack.AudioInput.getInstance(); 
		} else {
			DataLine.Info infoIn = new DataLine.Info(TargetDataLine.class, format);		
			lineIn = in.getLine(infoIn);
		}*/
		lineIn=in;
		
	}
	
	public void start() throws Exception
	{
		lineIn.open(format);
	}
	
	public void stop()
	{
		lineIn.close();
	}
	
	public TargetDataLine getLine()
	{
		return(lineIn);
	}
}
