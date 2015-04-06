/*
 * Created on Aug 11, 2007
 *
 * Copyright (c) 2004-2007 Peter Johan Salomonsen
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

package com.frinika.synth.soundbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.spi.SoundbankReader;

import com.frinika.synth.Synth;
import com.frinika.synth.settings.SynthSettings;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public class SynthRackSoundbankReader extends SoundbankReader {

	@Override
	public Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
		return getSoundbank(url.openStream());
	}

	@Override
	public Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
		ObjectInputStream in;
		
		try
		{
			in = new ObjectInputStream(stream);
		}
		catch(StreamCorruptedException e)
		{
			// Return null if stream isn't object stream.
			return null;
		}
		
		try {
			SynthSettings setup = (SynthSettings)in.readObject();
			SynthRackSoundbank soundbank = new SynthRackSoundbank();
			Serializable[] settings = setup.getSynthSettings();
			for(int index = 0;index<settings.length;index++)
			{
				if(settings[index]!=null)
				{
					String synthName = setup.getSynthClassNames()[index];

	                if(synthName==null)
	                	break;
	                
	                // Handle older fileformats
	                if(
	                    synthName.equals("com.petersalomonsen.mystudio.mysynth.synths.SoundFont") ||
	                    synthName.equals("com.petersalomonsen.mystudio.mysynth.synths.MySampler")
	                        )
	                    synthName = com.frinika.synth.synths.MySampler.class.getName();
	                
	                Synth synth;
					try {
						synth = (Synth)Class.forName(synthName).getConstructors()[0].newInstance(new Object[]{this});
						synth.loadSettings(setup.getSynthSettings()[index]);
						Patch patch = new Patch(0,index);
						soundbank.createAndRegisterInstrument(patch, synth);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return soundbank;
		} catch (ClassNotFoundException e) {
			throw new InvalidMidiDataException(e.getMessage());
		}
		
	}

	@Override
	public Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException {
		FileInputStream fis = new FileInputStream(file);
		Soundbank sbk = null;
		try
		{
			sbk = getSoundbank(fis);
		}
		finally
		{
			fis.close();
		}
		return sbk;
		
	}

}
