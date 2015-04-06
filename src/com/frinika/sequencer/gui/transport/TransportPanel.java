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
package com.frinika.sequencer.gui.transport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.global.FrinikaConfig;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.model.util.TimeUtils;

/**
 * Transport panel component. Shows button for transport controls: play, stop,
 * record, forward, backward.
 * 
 * @author Peter Johan Salomonsen
 * 
 */
public class TransportPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	FrinikaSequencer sequencer;

	ProjectFrame projectFrame;

	StartStopAction startStopAction;

	RecordAction recordAction;

	RewindAction rewindAction;

	JLabel timeDisplay;


	private final static int BAR_BEAT_TICK=1;
	private final static int FRAME=2;
	private final static int TIME=3;
	int displayMode=BAR_BEAT_TICK;

	private TimeUtils timeUtils;

	public TransportPanel(ProjectFrame projectFrame) {
		timeUtils= new TimeUtils(projectFrame.getProjectContainer());
		this.projectFrame = projectFrame;
		this.sequencer = projectFrame.getProjectContainer().getSequencer();
		startStopAction = new StartStopAction(projectFrame);
		rewindAction = new RewindAction(projectFrame);
		recordAction = new RecordAction(projectFrame);
		initComponents();

		projectFrame.getProjectContainer().getSequencer()
				.addSongPositionListener(new SwingSongPositionListenerWrapper(new SongPositionListener() {

					public void notifyTickPosition(long tick) {
						setTime();
					}

					public boolean requiresNotificationOnEachTick() {
						// TODO Auto-generated method stub
						return false;
					}

				}));
	}

	/**
	 * Initialize graphical components
	 * 
	 */
	void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		final JButton previousButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/previous.png")));
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				System.out
//						.println("Not implemented yet! Setting tickposition to 0");
				sequencer.setTickPosition(0);
			}
		});
		add(previousButton, gc);

		final JButton stopButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/stop.png")));
		stopButton.addActionListener(startStopAction.stopAction);
		add(stopButton, gc);

		final JButton recordButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/record.png")));
		recordButton.addActionListener(recordAction);

		add(recordButton, gc);

		final JButton playButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/play.png")));
		playButton.addActionListener(startStopAction.startAction);

		add(playButton, gc);

		final JButton nextButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/next.png")));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Not implemented yet!");
			}
		});
		add(nextButton, gc);

		timeDisplay = new JLabel();
		Font font = timeDisplay.getFont();
		Font newFont = font.deriveFont(Font.BOLD, font.getSize() + 5);
		timeDisplay.setFont(newFont);

		timeDisplay.setOpaque(true);
		timeDisplay.setBackground(Color.BLACK);
		timeDisplay.setForeground(Color.GREEN);
		timeDisplay.setBorder(
				BorderFactory.createMatteBorder(2,16,2,16, Color.BLACK));
		// gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;
		timeDisplay.setHorizontalAlignment(JLabel.CENTER);
		final JPanel timeDisplayPanel = new JPanel();
		timeDisplayPanel.setLayout(new BorderLayout());
		timeDisplayPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		timeDisplayPanel.add(timeDisplay);
		add(timeDisplayPanel, gc);
		timeDisplayPanel.setToolTipText(getMessage("transport.time.format.tip"));
		timeDisplayPanel.addMouseListener(new MouseListener() {

			JPopupMenu menu=new JPopupMenu();
			{
				JMenuItem item=new JMenuItem("bar.beat.tick");
				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						displayMode=BAR_BEAT_TICK;
						setTime();
					}
				});
				menu.add(item);
			
				item=new JMenuItem("frames");
				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						displayMode=FRAME;
						setTime();
					}
				});
				
				menu.add(item);
					
				item=new JMenuItem("time");
				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						displayMode=TIME;
						setTime();
					}
				});
				
				menu.add(item);
				
			}
			
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
				menu.show(timeDisplayPanel, 0, 0);
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			
		});
		setTime();
	}

	void setTime() {
	
		switch(displayMode) {
		case FRAME:
			int frames = (int) ((sequencer.getMicrosecondPosition() / 1000000.0) * FrinikaConfig.sampleRate);
			timeDisplay.setText(String.format("%07d", frames));
			break;
			
		case TIME:
			long time = sequencer.getMicrosecondPosition() / 1000;
			
			int secs = (int) (time / 1000);
			int mins= secs/60;
			secs=secs%60;
			int millis = (int) (time % 1000);
			timeDisplay.setText(String.format("%02d.%02d.%03d", mins,secs, millis));
			break;
			
		case BAR_BEAT_TICK:
			long tick=sequencer.getTickPosition();
			timeDisplay.setText(timeUtils.tickToBarBeatTick(tick));
			break;
			
			
		}
	}
}
