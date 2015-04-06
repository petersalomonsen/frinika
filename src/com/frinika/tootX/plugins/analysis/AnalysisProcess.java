/*
 * Created on 21 Aug 2007
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

package com.frinika.tootX.plugins.analysis;

import java.util.Observable;
import java.util.Observer;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.AudioProcess;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.frinika.audio.analysis.CycliclyBufferedAudio;
import com.frinika.audio.analysis.gui.CyclicBufferFFTAnalysisPanel;
import com.frinika.global.FrinikaConfig;



public class AnalysisProcess implements AudioProcess {
	private AnalysisProcessVariables vars;

//	Freeverb freeverb = null;

//	double reverbBufferIn[] = null;

//	double reverbBufferOut[] = null;
    private JFrame frame;
    private CycliclyBufferedAudio buferedAudioProcess;

	public AnalysisProcess(AnalysisProcessVariables variables) {
		vars = variables;
		((AudioControls)vars).addObserver(new Observer() {

			public void update(Observable arg0, Object arg1) {
				AnalysisProcess.this.update();
			}

		});
		
	}

	public void update() {
//
//		float wet=vars.getMix();
//		float dry=1.0f-wet;
//		float level=vars.getLevel();
//		freeverb.setwet(wet*level);
//		freeverb.setdry(dry*level);
//		freeverb.setroomsize(vars.getRoomSize());
//		freeverb.setwidth(vars.getWidth());
//		freeverb.setdamp(vars.getDamp());
		
	}

	public void open() {
        frame= new JFrame();

        buferedAudioProcess=new CycliclyBufferedAudio(100000,FrinikaConfig.sampleRate);
        JPanel panel=new CyclicBufferFFTAnalysisPanel(buferedAudioProcess);
        frame.setContentPane(panel);
        frame.setSize(new Dimension(500,400));
        frame.pack();
        frame.setVisible(true);
	//	freeverb = new Freeverb(FrinikaConfig.sampleRate, 1.0);
	//	update();

	}

	float mix1 = -1;

	public int processAudio(AudioBuffer buffer) {

		if (((AudioControls)vars).isBypassed() ) return AUDIO_OK;
        buferedAudioProcess.in.processAudio(buffer);
		return AUDIO_OK;
	}

	public void close() {

    }
}

