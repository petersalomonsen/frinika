// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import uk.org.toot.service.*;
import uk.org.toot.audio.server.*;
import uk.org.toot.swingui.audioui.serverui.spi.AudioServerUIServiceProvider;
import javax.swing.*;

public class AudioServerUIServices extends Services
{
    protected AudioServerUIServices() {
    }

    public static JComponent createServerUI(AudioServer server, AudioServerConfiguration p) {
        JComponent ui;
        Iterator<AudioServerUIServiceProvider> it = providers();
        while ( it.hasNext() ) {
            ui = it.next().createServerUI(server, p);
            if ( ui != null ) return ui;
        }
        return null;
    }

    public static void showSetupDialog(AudioServer server, AudioServerConfiguration p) {
        JComponent ui;
        Iterator<AudioServerUIServiceProvider> it = providers();
        while ( it.hasNext() ) {
            ui = it.next().createSetupUI(server, p);
            if ( ui != null ) {
            	final JComponent theUi = ui;
            	try {
            		SwingUtilities.invokeAndWait(new Runnable() {
            			public void run() {
            				new SetupDialog(theUi);    		
            			}
            		});
            	} catch ( Exception e ) {
            		// empty 
            	}
        		return;
            }
        }    	
    }
    
    public static Iterator<AudioServerUIServiceProvider> providers() {
        return lookup(AudioServerUIServiceProvider.class);
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
        Iterator<AudioServerUIServiceProvider> pit = providers();
        while ( pit.hasNext() ) {
            AudioServerUIServiceProvider asp = pit.next();
            asp.accept(v, clazz);
        }
	}

	public static void printServiceDescriptors(Class<?> clazz) {
        accept(new ServicePrinter(), clazz);
    }

	public static class SetupDialog extends JDialog implements ActionListener 
	{ 
	    private JButton     okButton;

	    private SetupDialog(JComponent ui) {
	        setTitle("Audio Server Setup");
	        setModal(true);
	        
	        getContentPane().add(ui, BorderLayout.CENTER);
	        
	        okButton = new JButton("OK");
	        okButton.addActionListener(this);
	        
	        JPanel buttonPanel = new JPanel(
	                new FlowLayout(FlowLayout.CENTER, 10, 10));
	        buttonPanel.add(okButton);
	        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	        
	        pack();
	        setLocationRelativeTo(null);
	        setVisible(true);
	    }
	 
	    public void actionPerformed(ActionEvent e) {
	        dispose();
	    }
	}

    public static void main(String[] args) {
        try {
	        printServiceDescriptors(null);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        try {
            System.in.read();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

