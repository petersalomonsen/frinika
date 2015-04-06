import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;

import com.frinika.project.ProjectContainer;

import junit.framework.TestCase;
/*
 * Created on Jun 10, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

/**
 * @author Peter Johan Salomonsen
 */
public class TempoAfterImportSaveLoadTest extends TestCase {

	Sequence seq;
	float tempo = 114;
	int resolution = 480;
	
    protected void setUp() throws Exception {
        super.setUp();	
        seq = new Sequence(Sequence.PPQ, resolution,1);
		

		
		int mpq = (int)(60000000 / tempo);
		try
		{
			MetaMessage tempoMsg = new MetaMessage();
			tempoMsg.setMessage(0x51,new byte[] {
				(byte)(mpq>>16 & 0xff),
				(byte)(mpq>>8 & 0xff),
				(byte)(mpq & 0xff)
			},3);
			MidiEvent tempoEvent = new MidiEvent(tempoMsg,0);
            seq.getTracks()[0].add(tempoEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void testImportMidi()
    {
    		try {
				ProjectContainer proj = new ProjectContainer(seq);
                                proj.getSequencer().start();
                                Thread.sleep(100);
                                proj.getSequencer().stop();
				assertEquals((int)tempo,(int)proj.getSequencer().getTempoInBPM());
			// 	assertEquals(tempo,proj.getTempo());
				assertEquals(resolution,proj.getSequence().getResolution());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
}
