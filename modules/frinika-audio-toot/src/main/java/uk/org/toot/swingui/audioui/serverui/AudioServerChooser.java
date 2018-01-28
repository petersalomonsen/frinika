// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import uk.org.toot.audio.server.AudioServerServices;
 
public class AudioServerChooser extends JDialog implements ActionListener 
{ 
    private static String chosenServerName;
    
    private JButton     okButton;
    private JButton     cancelButton;

    static {
		AudioServerServices.scan(); // early tickle	
    }
    
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private AudioServerCombo serverCombo;

    private AudioServerChooser(String serverName) {
        setTitle("Audio Server Selection");
        setModal(true);
        
        getContentPane().add(new ServerSelector(serverName), BorderLayout.CENTER);
        
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
 
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == okButton ) {
        	chosenServerName = (String)serverCombo.getSelectedItem();
        }
        dispose();
    }

    protected class ServerSelector extends JPanel
    {
        public ServerSelector(String serverName) {
            JLabel serverLabel = new JLabel("Audio Server");
            add(serverLabel);
            add(serverCombo = new AudioServerCombo(serverName));
            serverLabel.setLabelFor(serverCombo);
        }
    }

    public static String showDialog(final String serverName) {
    	chosenServerName = null;
    	try {
    		SwingUtilities.invokeAndWait(new Runnable() {
    			public void run() {
    	    		new AudioServerChooser(serverName);    		
    			}
    		});
    	} catch ( Exception e ) {
    		// empty 
    	}
    	return chosenServerName;
    }

}