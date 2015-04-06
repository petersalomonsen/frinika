/*
 * Created on Dec 15, 2004
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
package com.frinika.synth.synths.sampler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.sampled.TargetDataLine;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


import com.frinika.voiceserver.JavaSoundVoiceServer;
import com.frinika.sequencer.gui.AudioDeviceHandle;

import com.frinika.synth.InstrumentNameListener;
import com.frinika.synth.Synth;
import com.frinika.synth.importers.wav.WavImporter;
import com.frinika.synth.synths.MySampler;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import com.frinika.tools.MyFileFilter;

import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SamplerGUI extends JFrame implements InstrumentNameListener {
	private static final long serialVersionUID = 1L;

	TableModel sampleMapTableModel;
	JTable sampleMapTable;
    RecorderGUI recorderGUI;
	SampleMapTableRenderer sampleMapTableRenderer = new SampleMapTableRenderer();
	MySampler sampler;
    
	static boolean stereo = false;
	static TargetDataLine lineIn = null;
	
	static AudioDeviceHandle audioInDevice = null;
	
	public SamplerGUI(final MySampler sampler)
	{		        
	    this.sampler = sampler;
        setLayout(new BorderLayout());

        JPanel layersPanel = new JPanel();
        final JComboBox layerComboBox = new JComboBox();
        for(int n=1;n<=6;n++)
        {
            layerComboBox.addItem(new Integer(n));
        }
        layerComboBox.setSelectedIndex(sampler.getSamplerSettings().getLayers()-1);
        layerComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                sampler.getSamplerSettings().setLayers(layerComboBox.getSelectedIndex()+1);
            }});
        layersPanel.add(new JLabel("Layers: "));
        layersPanel.add(layerComboBox);

        final JSlider freqSpreadSlider = new JSlider(JSlider.HORIZONTAL,0,1000,
            (int)(sampler.getSamplerSettings().getFreqSpread()*80f));
        
        freqSpreadSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sampler.getSamplerSettings().setFreqSpread((freqSpreadSlider.getValue() / 80f));
            }});
        layersPanel.add(new JLabel("Frequency spread:"));
        layersPanel.add(freqSpreadSlider);

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BorderLayout());
        midPanel.add(layersPanel,BorderLayout.NORTH);
        
		sampleMapTableModel = new SampleMapTableModel(sampler);
		sampleMapTable = new JTable(sampleMapTableModel) {
				private static final long serialVersionUID = 1L;

				public Component prepareRenderer(TableCellRenderer renderer,
						int row, int column) {
					Component c = super.prepareRenderer(renderer, row, column);
					if(column == 0)
					{
						switch(row%12)
						{
							case 0:
								// C
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 1:
								// B
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 2:
								// A#
								c.setBackground(Color.BLACK);
								c.setForeground(Color.WHITE);
								break;
							case 3:
								// A
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 4:
								// G#
								c.setBackground(Color.BLACK);
								c.setForeground(Color.WHITE);
								break;
							case 5:
								// G
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 6:
								// F#
								c.setBackground(Color.BLACK);
								c.setForeground(Color.WHITE);
								break;
							case 7:
								// F
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 8:
								// E
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 9:
								// D#
								c.setBackground(Color.BLACK);
								c.setForeground(Color.WHITE);
								break;
							case 10:
								// D
								c.setBackground(Color.WHITE);
								c.setForeground(Color.BLACK);
								break;
							case 11:
								// C#
								c.setBackground(Color.BLACK);
								c.setForeground(Color.WHITE);
								break;
						}
					}	
					else
					{
						
						if(!sampleMapTable.isCellSelected(row,column))
						{
							c.setBackground(null);
							c.setForeground(null);
						}
					}
					return c;
				}
			};


		sampleMapTable.getColumnModel().getColumn(0).setHeaderValue("Note/Vel");
		sampleMapTable.setIntercellSpacing(new Dimension(0,0));
		sampleMapTable.setShowGrid(false);
		sampleMapTable.setFont(new Font(sampleMapTable.getFont().getFontName(),
				sampleMapTable.getFont().getStyle(), 9));
		for(int n = 1;n<sampleMapTable.getColumnCount();n++)
		{
			TableColumn col = sampleMapTable.getColumnModel().getColumn(n); 
			col.setHeaderValue(128-n);
			col.setCellRenderer(sampleMapTableRenderer );
			col.setMinWidth(5);
			col.setPreferredWidth(5);
			//col.setHeaderRenderer(new SampleMapTableRenderer());
		}
			
		sampleMapTable.getTableHeader().setReorderingAllowed(false);
		sampleMapTable.getTableHeader().setResizingAllowed(false);
		
		sampleMapTable.setColumnSelectionAllowed(true);
		sampleMapTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane sampleMapPane = new JScrollPane(sampleMapTable);
		midPanel.add(sampleMapPane,BorderLayout.CENTER);
        add(midPanel,BorderLayout.CENTER);
        
		sampleMapTable.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					new SampleEditor((SampledSoundSettings)sampleMapTableModel.getValueAt(sampleMapTable.getSelectedRow(),sampleMapTable.getSelectedColumn()));
			}
		});
	
		JMenuBar menuBar = new JMenuBar();
		add(menuBar,BorderLayout.NORTH);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
                
        final JMenuItem renameMenuItem = new JMenuItem("Rename");
        renameMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampler.setInstrumentName(
                        JOptionPane.showInputDialog("Enter new name",
                        sampler.getInstrumentName()));
            }});
        fileMenu.add(renameMenuItem);
        
		final JMenuItem importSFMenuItem = new JMenuItem("Import SoundFont V2");
		importSFMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sampler.sfi.showGUI();
			}});
		fileMenu.add(importSFMenuItem);
		
        final JMenuItem importWavMenuItem = new JMenuItem("Import Wav");
        importWavMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try
                {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Import wav");
                    chooser.setFileFilter(new MyFileFilter(".wav","Wav files"));
                    if(chooser.showOpenDialog(SamplerGUI.this)==
                        JFileChooser.APPROVE_OPTION) {
                        SampledSoundSettings sampledSound = new WavImporter().importWav(chooser.getSelectedFile());
                        sampledSound.setRootKey(getLowestNotenumberInSelection());
                        insertSampleToSelection(sampledSound);
                    };
                } catch(Exception ex) { ex.printStackTrace(); }

            }});
        fileMenu.add(importWavMenuItem);
        
        
		JMenu recordMenu = new JMenu("Record");
		menuBar.add(recordMenu);
		
		if(sampler.getAudioOutput() instanceof JavaSoundVoiceServer)
		{
			JMenu audioInputMenu = new JMenu(getMessage("sampler.menu.audio_inputs"));
			recordMenu.add(audioInputMenu);
			try {
				throw new Exception("FIXME");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			if(audioInDevice==null)
//			{
//				audioInDevice = AudioHub.getAudioInHandles().get(0);
//				lineIn = audioInDevice.getLine();
//				stereo = audioInDevice.getFormat().getChannels() == 2 ? true : false;
//			}
//			
//			ButtonGroup audioInGroup = new ButtonGroup();
//			for(final AudioDeviceHandle device : AudioHub.getAudioInHandles())
//		    {
//				JRadioButtonMenuItem audioInItem = new JRadioButtonMenuItem(device.toString());
//				
//				if(audioInItem.getText().equals(audioInDevice.toString()))
//						audioInItem.setSelected(true);
//				
//				audioInputMenu.add(audioInItem);
//				audioInGroup.add(audioInItem);
//				audioInItem.addActionListener(new ActionListener() {
//
//					public void actionPerformed(ActionEvent event) {
//						audioInDevice = device;
//					}});
//		    }  
		}
		else
		{
			JMenu audioInputMenu = new JMenu(getMessage("sampler.menu.audio_inputs"));
			recordMenu.add(audioInputMenu);
			ButtonGroup audioInGroup = new ButtonGroup();
			JMenuItem item = new JRadioButtonMenuItem("Mono (left)");
			item.setSelected(!stereo);
			audioInGroup.add(item);
			audioInputMenu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					stereo = false;
				}});
			item = new JRadioButtonMenuItem("Stereo");
			item.setSelected(stereo);
			audioInGroup.add(item);
			audioInputMenu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					stereo = true;
				}});
		}
        
		final JMenuItem recordMenuItem = new JMenuItem("Record to key");
		recordMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sampler.recordMode = MySampler.RECORDMODE_SINGLE_KEY;
				startSampler();
			}});
		recordMenu.add(recordMenuItem);

		final JMenuItem recordAllKeysMenuItem = new JMenuItem("Record to all keys");
		recordAllKeysMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sampler.recordMode = MySampler.RECORDMODE_ALL_KEYS;
				startSampler();
				
			}});
		recordMenu.add(recordAllKeysMenuItem);

		final JMenuItem recordToSelectionMenuItem = new JMenuItem("Record to selection");
		recordToSelectionMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sampler.recordMode = MySampler.RECORDMODE_SELECTION;
				startSampler();
			}});
		recordMenu.add(recordToSelectionMenuItem);

		setSize(700,500);
		setVisible(true);
                
        setTitle(sampler.getInstrumentName());
        sampler.addInstrumentNameListener(this);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosed(WindowEvent e) {
                sampler.removeInstrumentNameListener(SamplerGUI.this);
            }
        });
	}
	
	void startSampler()
	{
		try {
			sampler.samplerOscillator.startMonitor(lineIn,stereo);
	        openRecorderGUI();
			JOptionPane.showMessageDialog(SamplerGUI.this,"Your input is now being monitored. Recording will start on MIDI Note ON and stop on MIDI Note OFF.");

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(SamplerGUI.this,getMessage("sampler.menu.audio_input_error"));
		}
	}
	
    void openRecorderGUI()
    {
        if(recorderGUI!=null)
            recorderGUI.dispose();
        
        recorderGUI = new RecorderGUI(sampler.samplerOscillator);
    }
    
	public void insertSampleToSelection(SampledSoundSettings sampledSound)
	{
		for(int x : sampleMapTable.getSelectedColumns())
			for(int y : sampleMapTable.getSelectedRows())
				sampleMapTable.setValueAt(sampledSound,y,x);
	}
		
	public static void main(String[] args)
	{
		new SamplerGUI(null);
	}

    public int getLowestNotenumberInSelection()
    {
        int row = 0;
        for(int n : sampleMapTable.getSelectedRows())
            if(n>row)
                row = n;
        
        return 96-row;
    }
    
	/**
	 * Check if the given note with the specified velocity is selected
	 * @param noteNumber
	 * @param velocity
	 * @return
	 */
	public boolean isNoteInSelection(int noteNumber, int velocity) {
		return sampleMapTable.isCellSelected(96-noteNumber,128-velocity);
	}

    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.InstrumentNameListener#instrumentNameChange(java.lang.String)
     */
    public void instrumentNameChange(Synth synth, String instrumentName) {
        setTitle(instrumentName);
    }
}
