import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.JFrame;

import junit.framework.TestCase;

import com.frinika.project.ProjectContainer;
import com.frinika.project.settings.projectsettingsversions.Project20050227;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.MySampler;
import com.frinika.voiceserver.VoiceServer;

/*
 * Created on Jun 17, 2006
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
public class ProjectCompatibilityTest  extends TestCase {
	ProjectContainer proj;

	float tempo = 120f;
	
	protected void setUp() throws Exception
	{
		FrinikaSequence sequence = new FrinikaSequence(Sequence.PPQ, 128, 1);
		int mpq = (int)(60000000 / tempo);

		MetaMessage tempoMsg = new MetaMessage();
		tempoMsg.setMessage(0x51,new byte[] {
			(byte)(mpq>>16 & 0xff),
			(byte)(mpq>>8 & 0xff),
			(byte)(mpq & 0xff)
		},3);
		MidiEvent tempoEvent = new MidiEvent(tempoMsg,0);
                sequence.getFrinikaTrackWrappers().get(0).add(tempoEvent);
            
		ByteArrayOutputStream sequenceOutputStream = new ByteArrayOutputStream();
		MidiSystem.write(sequence, 1, sequenceOutputStream);
		Project20050227 project = new Project20050227();
		project.setSequence(sequenceOutputStream.toByteArray()); 
		
		SynthRack synthRack = new SynthRack(new VoiceServer() {

                @Override
                    public void configureAudioOutput(JFrame frame) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            });
            MySampler sampler = new MySampler(synthRack);
            synthRack.setSynth(1,sampler);
        
            project.setSynthSettings(synthRack.getSynthSetup());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(project);

            proj = ProjectContainer.loadProject(new ByteArrayInputStream(baos.toByteArray()));
	}
	
	
	// PJL 
	
	public void testTempo() throws Exception
	{
            proj.getSequencer().start();
            Thread.sleep(100);
            proj.getSequencer().stop();
            assertEquals((int)tempo,(int)proj.getSequencer().getTempoInBPM());
	}
}
