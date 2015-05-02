// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class AudioServerUIButton extends JButton implements ActionListener
{
    private JFrame serverFrame;

    public AudioServerUIButton(JComponent serverUI) {
        super("Server");
		if ( serverUI == null ) {
            setEnabled(false);
            return;
        }

		serverFrame = new JFrame("Audio Server");
		serverFrame.setAlwaysOnTop(true);
		serverFrame.setContentPane(serverUI);
		serverFrame.pack();
		addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
		if ( serverFrame != null) {
			serverFrame.setVisible(true);
		}
    }
}
