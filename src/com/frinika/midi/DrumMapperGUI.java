package com.frinika.midi;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.ListProvider;
import com.frinika.sequencer.gui.PopupClient;
import com.frinika.sequencer.gui.PopupSelectorButton;
import com.frinika.sequencer.gui.pianoroll.VirtualPianoVert;
import com.frinika.sequencer.model.MidiLane;

public class DrumMapperGUI  extends JPanel  {

	DrumMapper dm;
	ProjectContainer proj;
	
	VirtualPianoVert inPiano;
	VirtualPianoVert outPiano;
	VirtualPianoVert.Config configIn=null;
	VirtualPianoVert.Config configOut=null;
	JButton  setMap;
	MidiLane lane;
	
	
	DrumMapperGUI(final DrumMapper dm,ProjectContainer proj,MidiLane lane) {
		this.dm=dm;
		this.lane=lane;
		this.proj=proj;
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.anchor=GridBagConstraints.WEST;
 	//	add(createDeviceSelector(),gc);
 
 	
 		try {
			configIn = new VirtualPianoVert.Config(40,8,dm.getReceiver(),lane);
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		inPiano=new VirtualPianoVert(configIn);

		try {
			MidiDevice dev=dm.getDefaultMidiDevice();
			Receiver recv=null;
			if (dev != null ) recv= dev.getReceiver();
			configOut = new VirtualPianoVert.Config(50,8,recv,lane);
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outPiano=new VirtualPianoVert(configOut);
		
		
		setMap = new JButton(" -- assign key --> ");
		setMap.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				int in=inPiano.getLastKeytPress();
				int out=outPiano.getLastKeytPress();
				dm.setMapping(in,out);
				repaint();
			}
			
		});
		
	//	gc.gridy++;
		gc.fill=GridBagConstraints.BOTH;

		add(setMap,gc);
		JPanel pianoPanel=new DrumMapperPanel(inPiano,outPiano,dm);
		
		gc.gridy++;
		 
		JScrollPane scroll=new JScrollPane(pianoPanel);
		gc.fill=GridBagConstraints.BOTH;
		gc.weighty=1;
		gc.weightx=1;
		add(scroll,gc);
 		
	}
	
	void init() {
		MidiDevice dev=dm.getDefaultMidiDevice();
		Receiver recv=null;
		if (dev != null )
			try {
				recv= dev.getReceiver();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		configOut.setReceiver(recv);
		
		
		
		
	}
	
	PopupSelectorButton createDeviceSelector() {
		 MidiDevice midiDev = dm.getDefaultMidiDevice();

		// Device selector
		// ------------------------------------------------------------------------------------

		ListProvider resource = new ListProvider() {
			public Object[] getList() {
				return proj.getMidiDeviceDescriptors().toArray();// midiResource.getMidiOutList();
			}
		};

		PopupClient client = new PopupClient() {
			public void fireSelected(PopupSelectorButton but, Object o, int cnt) {
				dm.setDefaultMidiDevice(
						((MidiDeviceDescriptor) o).getMidiDevice());
			//	if (o != midiDev)
				init();
			}
		};

		String name;

		if (midiDev != null)
			name = midiDev.toString(); 
		else
			name = "null";

		return new PopupSelectorButton(resource, client, name);

	}


	
}
