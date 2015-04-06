/*
 * Created on Apr 24, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.sound.midi.Sequencer;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.model.util.TimeUtils;

import static com.frinika.localization.CurrentLocale.getMessage;



/**
 * Panel for setting edit/playing section range, and control looping
 * 
 * @author Peter Johan Salomonsen
 * @author Jens Gulden
 * 
 */
/*
public class LoopPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	FrinikaSequencer sequencer;

	JTextField sectionStartTextField;

	JTextField sectionEndTextField;

	TimeUtils timeUtil;
	
	public LoopPanel(ProjectContainer project) {
		this.sequencer = project.getSequencer();
		timeUtil=new TimeUtils(project);

		initComponents();
		setText();

		sequencer.addSongPositionListener(new SongPositionListener() {

			public void notifyTickPosition(long tick) {
				// TODO Auto-generated method stub
				setText();
			}

			public boolean requiresNotificationOnEachTick() {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
	}

	void setText() {
		sectionStartTextField.setText(timeUtil.tickToBarBeatTick(sequencer.getLoopStartPoint()));
		sectionEndTextField.setText(timeUtil.tickToBarBeatTick(sequencer.getLoopEndPoint()));
	}
	
	void initComponents() {
		add(new JLabel(" Start "));
		sectionStartTextField = new JTextField("              ");
		sectionStartTextField.setColumns(6);
		add(sectionStartTextField);

		sectionStartTextField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {

					sequencer.setLoopStartPoint(timeUtil.barBeatTickToTick(sectionStartTextField
							.getText()));

				} catch (Exception ex) {

				}
			}
		});

		
		sectionStartTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
			
					sequencer.setLoopStartPoint(timeUtil.barBeatTickToTick(sectionStartTextField
							.getText()));
	
				} catch (Exception ex) {
		
				}
			}
		});

		
		
		add(new JLabel(" End "));

		sectionEndTextField = new JTextField("                      ");
		sectionEndTextField.setColumns(6);
		sectionEndTextField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					sequencer.setLoopEndPoint(timeUtil.barBeatTickToTick(sectionEndTextField
							.getText()));
				} catch (Exception ex) {
				}
			}
		});
		
		sectionEndTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sequencer.setLoopEndPoint(timeUtil.barBeatTickToTick(sectionEndTextField
							.getText()));
				} catch (Exception ex) {
				}
			}
		});

		
		add(sectionEndTextField);

		
		
		final JToggleButton loopButton = new JToggleButton(new ImageIcon(
				ClassLoader.getSystemResource("icons/loop.png")));
		loopButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// tracker.updateLoopPoints();
				//	sequencer.setLoopStartPoint(sectionStart * ticksPerBeat);
			//		sequencer.setLoopEndPoint((sectionStart+sectionLength) * ticksPerBeat);

					sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
				} else {
					sequencer.setLoopCount(0);
				}
			}
		});
		add(loopButton);
	}
*/
	/**
	 * @return Returns the sectionLength.
	 */
	/*public int getSectionLength() {
		return sectionLength;
	}
*/
	/**
	 * @return Returns the sectionStart.
	 */
/*	public int getSectionStart() {
		return sectionStart;
	}
*/
/*	public void previousPage() {
		sectionStart -= sectionLength;
		if (sectionStart < 0)
			sectionStart = 0;
		sectionStartTextField.setText(sectionStart + "");
		// tracker.refreshTrackView();
	}

	public void nextPage() {
		sectionStart += sectionLength;
		sectionStartTextField.setText(sectionStart + "");
		// tracker.refreshTrackView();
	}*/
/*
}
*/

// Alternative implementation using 2 TimeSelectors, Jens:

public class LoopPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	ProjectContainer project;
	FrinikaSequencer sequencer;

	TimeSelector sectionStartTimeSelector;
	TimeSelector sectionEndTimeSelector;

	TimeUtils timeUtil;
	
	public LoopPanel(ProjectContainer project) {
		this.project = project;
		this.sequencer = project.getSequencer();
		timeUtil=new TimeUtils(project);

		initComponents();
		setText();

		sequencer.addSongPositionListener(new SwingSongPositionListenerWrapper(new SongPositionListener() {

			public void notifyTickPosition(long tick) {
				setText();
			}

			public boolean requiresNotificationOnEachTick() {
				return false;
			}
		}));
	}

	void setText() {
		sectionStartTimeSelector.setTicks(sequencer.getLoopStartPoint());
		sectionEndTimeSelector.setTicks(sequencer.getLoopEndPoint());
	}
	
	void initComponents() {		
		sectionStartTimeSelector = new TimeSelector(getMessage("globaltoolbar.loop.start"), 0l, project, TimeFormat.BAR_BEAT_TICK);
		sectionStartTimeSelector.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sequencer.setLoopStartPoint(sectionStartTimeSelector.getTicks());
			}
		});
		add(sectionStartTimeSelector);

		sectionEndTimeSelector = new TimeSelector(getMessage("globaltoolbar.loop.end"), 0l, project, TimeFormat.BAR_BEAT_TICK);
		sectionEndTimeSelector.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sequencer.setLoopEndPoint(sectionEndTimeSelector.getTicks());
			}
		});
		add(sectionEndTimeSelector);
		
		final JToggleButton loopButton = new JToggleButton(new ImageIcon(
				ClassLoader.getSystemResource("icons/loop.png")));
                
                loopButton.setSelected(sequencer.getLoopCount() != 0);
                
		loopButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
				} else {
					sequencer.setLoopCount(0);
				}
			}
		});
		add(loopButton);
	}
}

