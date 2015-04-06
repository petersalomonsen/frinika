/*
 * Created on 27.2.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.frinika.project.gui.ProjectFrame;

public class AboutDialog extends JDialog {
	
	
	public static final char C = (char)169; // The (C) Symbol
	
	public static final String MAIN_TITLE = 
		 "<html><center>" +
		 "<b>Frinika version "+VersionProperties.getVersion()+" </b><br>"+
		 "http://frinika.sourceforge.net<br><font color='#A0A0A0'><i>Build date: "+VersionProperties.getBuildDate()+"</i></font>" +
		 "</html>";
	
	public static final String COPYRIGHT_NOTICE = 
		"<html><center>"+
		"Copyright "+C+" "+VersionProperties.getCopyrightStart()+"-"+VersionProperties.getCopyrightEnd()+" The Frinika developers. All rights reserved<br>"+
		"This software is licensed under the GNU General Public License (GPL) version 2<br>"+
		"http://www.gnu.org/licenses/gpl.htm"+
		"</html>";
	
	public static final String CREDITS = 
    "<html>"+
    "<h2>The team behind Frinika:</h2>"+
    "Peter Johan Salomonsen - Initiative, sequencer, audiodriver, soft synths, tracker, maintenance and more<br>"+
    "Jon Aakerstrom - Audiodriver, JACK integration<br>" +
    "P.J. Leonard - Pianoroll, partview, overall GUI and sequence objects design and more<br>"+
    "Karl Helgason - RasmusDSP, flexdock, jmod integration with Frinika and more<br>"+
    "Toni (oc2pus@arcor.de) - Ant build scripts and Linux RPMs<br>"+
    "Steve Taylor - Toot integration<br>"+
    "Jens Gulden - Ghosts parts, Midi Tools menu, step recording, ctrl tools, scripting and more<br>"+
    "<br>"+
    "<b>Libraries:</b><br>"+
    "JJack Copyright "+C+" Jens Gulden<br>"+
    "RasmusDSP Copyright "+C+" Karl Helgason<br>"+
    "Toot audio foundation - Steve Taylor<br>"+
    "Tritonus Copyright "+C+" by Florian Bomers and Matthias Pfisterer<br>"+
    "launch4j - Cross-platform Java executable wrapper - http://launch4j.sourceforge.net/<br>"+
    "jgoodies - Look and feel - https://looks.dev.java.net/<br>"+
    "flexdock - Floating and dockable windows - https://flexdock.dev.java.net/<br>"+
    "Java Sound MODules Library - http://jmod.dev.java.net<br>"+
    "Rhino JavaScript engine - http://www.mozilla.org/rhino/<br>"+
    "LZMA SDK - http://www.7-zip.org/sdk.html<br>"+
    "jVorbisEnc - Zbigniew Sudnik - XIPHOPHORUS, http://www.xiph.org/<br>"+
    "MRJ Adapter - http://homepage.mac.com/sroy/mrjadapter/<br>" +
    "JVSTHost - http://github.com/mhroth/jvsthost<br>" +
    "<br>"+
    "<b>Other contributors:</b><br>"+
    "Bob Lang - Bezier synth (http://www.cems.uwe.ac.uk/~lrlang/BezierSynth/index.html)<br>"+
    "Edward H - GUI decoration patches<br>"+
    "Artur Rataj (arturrataj@gmail.com) - Pianoroll patches<br>"+
    "Thibault Aspe - French locale (http://thibault.aspe.free.fr/)<br>"+
    "<br></html>";

	private static final long serialVersionUID = 1L;
	
	int sel = 0;
	int ix = 0;
	
	public void showLicense()
	{
		
		JPanel panel = new JPanel();
		
        JTextArea licenseAgreement = null;
        try
        {
            InputStream is = ClassLoader.getSystemResource("COPYING").openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while(is.available()>0)
            {
            	bos.write(is.read());
            }
            licenseAgreement = new JTextArea(new String(bos.toByteArray()));
        } catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Couldn't find license agreement.. Exiting.");
            System.exit(0);
        }
         
        licenseAgreement.setEditable(false);
        licenseAgreement.setRows(20);
        JScrollPane licenseScrollPane = new JScrollPane(licenseAgreement,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                );
        
        panel.add(licenseScrollPane);
		
        JOptionPane.showMessageDialog(this, panel, "License",
        		JOptionPane.INFORMATION_MESSAGE, 
        		new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)));		
	}
	
	public void whitening(Container co)
	{
        Component[] comps = co.getComponents();
        for (int i = 0; i < comps.length; i++) {
        	if(comps[i] instanceof JOptionPane)
        		comps[i].setBackground(Color.WHITE);
        	if(comps[i] instanceof JPanel)
        		comps[i].setBackground(Color.WHITE);
			if(comps[i] instanceof Container)
				whitening((Container)comps[i]);
		}		
	}
	
	public void showCredits()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
        JLabel label = new JLabel(CREDITS);        
        JScrollPane scrollPane = new JScrollPane(label,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                );
		label.setFont(label.getFont().deriveFont(11f).deriveFont(Font.PLAIN));		
        
        panel.add(label);
        
        
        JOptionPane op = new JOptionPane(panel,
        		JOptionPane.INFORMATION_MESSAGE);                
        
        op.setIcon(new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)));
        JDialog dialog = op.createDialog(this, "Credits");
        dialog.setBackground(Color.WHITE);
        whitening(dialog);
        
        dialog.setVisible(true);
        
		
	}

	public void showSystemInfo()
	{
		
        // Jens:
        Properties p = System.getProperties();
        String[][] ss = new String[p.size()][2];
        int i = 0;
        for (Object o : (new TreeSet(p.keySet()))) {
        	String s = (String)o;
        	String value = p.getProperty(s);
        	ss[i][0] = s;
        	ss[i][1] = value;
        	i++;
        }
        JTable systemInfo = new JTable(ss, new String[] { "Entry", "Value" });
        systemInfo.setEnabled(false);
        
        JOptionPane.showMessageDialog(this, new JScrollPane(systemInfo), "System Info",
        		JOptionPane.INFORMATION_MESSAGE, 
        		new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)));		
        
	}
	
	public AboutDialog(JFrame parent)
	{		
		super(parent);
		
		setUndecorated(true);
		setModal(true);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.WHITE);
		Icon welcome = new javax.swing.ImageIcon(ProjectFrame.class.getResource("/frinika.png"));		
		JLabel label = new JLabel(welcome);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		label.setText(MAIN_TITLE);
		label.setBorder(BorderFactory.createEmptyBorder(25,5,5,5));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
		
		JPanel contentpane = new JPanel();
		contentpane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		contentpane.setLayout(new BorderLayout());
		contentpane.add(panel);
		setContentPane(contentpane);
		
		JPanel buttonpanel = new JPanel();
		buttonpanel.setOpaque(false);


		{
			JButton button = new JButton("License");
			button.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e) {
							showLicense();
						}
					});
			buttonpanel.add(button);			
		}				
		
		{
			JButton button = new JButton("Credits");
			button.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e) {
							showCredits();
						}
					});
			buttonpanel.add(button);			
		}		
		
		{
			JButton button = new JButton("System Info");
			button.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e) {
							showSystemInfo();
						}
					});
			buttonpanel.add(button);			
		}				
		
		{
			JButton button = new JButton("OK");
			button.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e) {
							setVisible(false);
						}
					});
			button.setDefaultCapable(true);
			getRootPane().setDefaultButton(button);			
			buttonpanel.add(button);			
		}
		
		
		panel.add(buttonpanel, BorderLayout.CENTER);
		
		JPanel copyrightpanel = new JPanel();
		copyrightpanel.setOpaque(false);

		JLabel line = new JLabel(COPYRIGHT_NOTICE);
		line.setHorizontalTextPosition(SwingConstants.CENTER);
		line.setFont(line.getFont().deriveFont(10f).deriveFont(Font.PLAIN));		
		
		copyrightpanel.add(line);

		panel.add(copyrightpanel, BorderLayout.SOUTH);
		
		setTitle("Welcome");
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		panel.registerKeyboardAction(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				}
				, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
				
		
		pack();
		
	    Rectangle windowSize ;
	    Insets windowInsets;		
		
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();	    
	    if(gc == null) 
	        gc = getGraphicsConfiguration();	    
	    
	    if(gc != null) {
	    	windowSize = gc.getBounds();
	    } else {
	    	windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
	    }	    						
		
		Dimension size = getSize();		
		Point parent_loc = getLocation();			
		setLocation(parent_loc.x + windowSize.width/2 - (size.width/2),
				    parent_loc.y + windowSize.height/2 - (size.height/2));
				
	}
	
}
