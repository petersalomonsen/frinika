/*
 * Created on Mar 7, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard, Karl Helgason
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
 * 
 * 
 * PJL 2007/10/20    added FrinikaConfig.SOUNDFONT_DIRECTORY
 */

package com.frinika.sequencer.gui.partview;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.frinika.global.FrinikaConfig;
import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.project.SoundBankNameHolder;
import com.frinika.project.SynthesizerDescriptor;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.SynthLane;
import com.frinika.soundbank.JARSoundbankLoader;
import com.frinika.synth.importers.soundfont.SoundFontFileFilter;
import java.awt.event.ActionListener;

public class SynthLaneView extends LaneView {

	private static final long serialVersionUID = 1L;

	SynthLane synthlane;
	MidiDeviceDescriptor midiDescriptor;
	MidiDevice midiDevice;
	MidiDevice realDevice;
	SynthWrapper synthWrapper = null;
	Info deviceInfo;
    private JLabel soundBankNamelabel;
	
	public SynthLaneView(Lane lane) {
		super(lane);
		
		synthlane = (SynthLane)lane;
		midiDescriptor = synthlane.getMidiDescriptor();
		midiDevice = midiDescriptor.getMidiDevice();
		if(midiDevice instanceof SynthWrapper)
		{
			synthWrapper = (SynthWrapper)midiDevice;
			realDevice = synthWrapper.getRealDevice();
		}
		else
			realDevice = midiDevice;
		
		deviceInfo = realDevice.getDeviceInfo();
		
		init();
				
	}


	
	void makeHeader()
	{
		GridBagConstraints gc2 = (GridBagConstraints)gc.clone();
		gc2.gridwidth = GridBagConstraints.REMAINDER;
		gc2.insets = new Insets(0,0,0,0);
		gc2.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel panel = new JPanel();
		JLabel label = new JLabel(midiDescriptor.getIcon());
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 7));
		panel.add(label);
		
		panel.setBorder(BorderFactory.createEmptyBorder(7,5,7,5));
	//	panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS)); // truncate right (FlowLayout wraps to new line)
		panel.setBackground(Color.WHITE);
		label = new JLabel("<html><body><b>" + 
				deviceInfo.getName() + "</b><br>" + 
				deviceInfo.getVendor() + " <font color='#A0A0A0'><i>" + 
				deviceInfo.getVersion() + "</i></font></body></html>");
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		panel.add(label);
		add(panel,gc2);		
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);		
		sep.setMinimumSize(new Dimension(5,5));		
		add(sep, gc2);		
	}

	protected void makeButtons() {
		
		makeHeader();
		
		gc.insets = new Insets(5,5,5,5);
		if(synthWrapper != null)
		{
			if(realDevice instanceof Synthesizer)
	        {
				
	        	JButton loadSoundbankButton = new JButton(getMessage("mididevices.loadsoundbank"));
	        	loadSoundbankButton.addMouseListener(new MouseAdapter() {
	                /* (non-Javadoc)
	                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	                 */
	                public void mouseClicked(MouseEvent e) {
						try
						{
							JFileChooser chooser = new JFileChooser(FrinikaConfig.SOUNDFONT_DIRECTORY);
							chooser.setDialogTitle("Open soundfont");
							chooser.setFileFilter(new SoundFontFileFilter());
							if(chooser.showOpenDialog(null)==
								JFileChooser.APPROVE_OPTION) {
								File soundFontFile = chooser.getSelectedFile();
								Soundbank soundbank = synthWrapper.getSoundbank(soundFontFile);
								synthWrapper.loadAllInstruments(soundbank);
								System.out.println("Soundbank loaded");
								((SynthesizerDescriptor)midiDescriptor).setSoundBankFileName(soundFontFile.getAbsolutePath());
                                soundBankNamelabel.setText(soundFontFile.getAbsolutePath());
                            };
						} catch(Exception ex) { ex.printStackTrace(); }
	                }
	            } );
	            add(loadSoundbankButton,gc);

                    JButton reloadSoundbankButton = new JButton(getMessage("mididevices.reloadsoundbank"));
                    reloadSoundbankButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            try {
                                String soundbankFilename = ((SynthesizerDescriptor)midiDescriptor).getSoundBankFileName();
                                synthWrapper.loadAllInstruments(synthWrapper.getSoundbank(new File(soundbankFilename)));
                            } catch (InvalidMidiDataException ex) {
                                Logger.getLogger(SynthLaneView.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(SynthLaneView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    add(reloadSoundbankButton,gc);

                    MidiDevice dev = synthWrapper.getRealDevice();
	            try {
					Method method = dev.getClass().getMethod("show");
					
		        	JButton showSettingsButton = new JButton(getMessage("mididevices.show"));
		        	showSettingsButton.addMouseListener(new MouseAdapter() {
		                public void mouseClicked(MouseEvent e) {
		                	
		                	MidiDevice dev = synthWrapper.getRealDevice();
		                	Method method;
							try {
								method = dev.getClass().getMethod("show");
			                	method.invoke(dev);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
		                }
		            } );
		            add(showSettingsButton,gc);				
				} catch (SecurityException e1) {
				} catch (NoSuchMethodException e1) {
				}
			
	        }
		}		
		
		if (midiDescriptor instanceof SoundBankNameHolder) {
            String name=((SoundBankNameHolder)midiDescriptor).getSoundBankFileName();
            if (name == null) name="sound bank not saved";
            soundBankNamelabel=new JLabel(name);
            add(soundBankNamelabel,gc);
        }

		gc.weighty = 1.0;
		add(new Box.Filler(new Dimension(0, 0),
				new Dimension(10000, 10000), new Dimension(10000, 10000)),
				gc);

	}

}
