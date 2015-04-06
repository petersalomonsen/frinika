/*
 * Created on Mar 1, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.tootX;

import com.frinika.project.FrinikaAudioSystem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LatencyTesterPanel extends JPanel {

	private LatencyTester tester;

	private JTextField latencyLabel;
	private JLabel currentVal;
	
	private JFrame frame;
	
	public LatencyTesterPanel(JFrame frame1) {
		this.frame=frame1;
		tester = new LatencyTester();
		add(latencyLabel = new JTextField(10));
		int lat=FrinikaAudioSystem.getAudioServer().getTotalLatencyFrames();
		add(new JLabel("samples latency. (current value="+lat+")"));
		tester.addObserver(new Observer() {

			public void update(Observable o, Object arg) {
				String str;
				int latency = tester.getLatencyInSamples();
				if (latency < 0) {
					str = "No signal";
				} else {
					str = ""+latency;
				}
				latencyLabel.setText(str);
			}

		});
		
	JButton set=new JButton("Apply");
		
		add(set);
	
		set.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
			
				int lat=Integer.parseInt(latencyLabel.getText());
				//FrinikaAudioSystem.setTotalLatency(tester.getLatencyInSamples());
				FrinikaAudioSystem.setTotalLatency(lat);
				// dispose();				
			}
			
			
		});

		
	JButton reset=new JButton("Reset");
		
		add(reset);
	
		set.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tester.reset();
			}	
		});

		
		JButton abort=new JButton("Quit");
		
		add(abort);
	
		abort.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();				
			}
			
			
		});

//		setPreferredSize(new Dimension(200, 50));

		if (frame != null) {
			frame.addWindowListener(new WindowListener(){

				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void windowClosed(WindowEvent e) {
				}

				public void windowClosing(WindowEvent e) {
					if (tester!=null) tester.stop();
					tester=null;
					if (frame!= null) frame.dispose();
					frame=null;

					// TODO Auto-generated method stub
					
				}

				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void windowIconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void windowOpened(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			});
			tester.start(frame);
		}
		

	}
	
	void dispose() {
		tester.stop();
		tester=null;
		frame.dispose();		
		frame=null;
	}

}
