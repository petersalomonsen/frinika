/*
 * Created on Jun 22, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

package com.frinika.sequencer.gui.partview;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;

import uk.org.toot.audio.core.AudioProcess;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.sequencer.gui.ListProvider;
import com.frinika.sequencer.gui.PopupClient;
import com.frinika.sequencer.gui.PopupSelectorButton;
import com.frinika.sequencer.model.AudioLane;

public class AudioLaneView extends LaneView {

	
	AudioProcess audioIn;
	String name="null";
	
	public AudioLaneView(AudioLane lane) {
		super(lane);
		init();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

	protected void makeButtons() {

		JComponent but = createDeviceSelector();
		add(but, gc);
			gc.weighty = 1.0;
			add(new Box.Filler(new Dimension(0, 0),
					new Dimension(10000, 10000), new Dimension(10000, 10000)),
					gc);


	}



	PopupSelectorButton createDeviceSelector() {
		audioIn = ((AudioLane) lane).getAudioInDevice();

		// Device selector
		// ------------------------------------------------------------------------------------

		ListProvider resource = new ListProvider() {
			public Object[] getList() {
				// TODO connections setup
//				Vector<AudioDeviceHandle> vec = AudioHub.getAudioInHandles();
//				AudioDeviceHandle list[] = new AudioDeviceHandle[vec.size()];
//				list=vec.toArray(list);
				List<String> vec=FrinikaAudioSystem.getAudioServer().getAvailableInputNames();
				String list[]=new String[vec.size()];
				list=vec.toArray(list);
				
				
			//	int ii=0;
			//	for (AudioDeviceHandle h:vec) {
			//		list[ii++]=h;
			//	}
				return list;
			}
		};

	
		
		PopupClient client = new PopupClient() {
			public void fireSelected(PopupSelectorButton but, Object o, int cnt) {
				AudioProcess in;
				try {
					in=FrinikaAudioSystem.getAudioServer().openAudioInput((String)o, null);
					((AudioLane)lane).setAudioInDevice(in);
					name=(String)o;
					if (in != audioIn)
						init();
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};


//		if (audioIn != null)
//			name = audioIn.toString();
//		else
//			name = "null";

		return new PopupSelectorButton(resource, client, name);

	}

}
