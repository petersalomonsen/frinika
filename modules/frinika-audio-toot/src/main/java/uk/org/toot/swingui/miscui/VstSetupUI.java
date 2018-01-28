// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.miscui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import uk.org.toot.misc.VstSetup;

public class VstSetupUI
{
    // edit existing paths
    public static void showDialog() {
        showDialog(VstSetup.readPaths());
    }
    
	public static void showDialog(final List<File> paths) {
        if ( SwingUtilities.isEventDispatchThread() ) {
            new PathSetupDialog(paths);
            return;
        }
    	try {
    		SwingUtilities.invokeAndWait(new Runnable() {
    			public void run() {
    	    		new PathSetupDialog(paths);    		
    			}
    		});
    	} catch ( Exception e ) {
    		// empty 
    	}		
	}
	
	private static class PathSetupDialog extends JDialog implements ActionListener
	{
		private JButton okButton;
		private PathSetupPanel panel;
		
		public PathSetupDialog(List<File> paths) {
			setTitle("VST Plugin Path Setup");
			setModal(true);

			panel = new PathSetupPanel(paths);
			getContentPane().add(panel, BorderLayout.CENTER);

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

		public void actionPerformed(ActionEvent arg0) {
			VstSetup.writePaths(panel.getPaths());
			dispose();
		}

	}
	
	/**
	 * This class provides a list of paths which may be set using file selectors.
	 * @author st
	 *
	 */
	private static class PathSetupPanel extends JPanel
	{
		private JTextArea textArea;
		
		public PathSetupPanel(List<File> paths) {
			setLayout(new BorderLayout());
			textArea = new JTextArea(10, 50);
			textArea.setEditable(false);
			textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
			for ( File path : paths) {
				textArea.append(path.getPath()+"\n");
			}
			add(textArea, BorderLayout.CENTER);
			add(new AddButton(), BorderLayout.EAST);
			
		}
		
		public List<File> getPaths() {
			List<File> paths = new java.util.ArrayList<File>();
			String[] strings = textArea.getText().split("\n");
			for ( int i = 0; i < strings.length; i++ ) {
				paths.add(new File(strings[i]));
			}
			return paths;
		}
		
		private class AddButton extends JButton implements ActionListener
		{
			private JFileChooser fileChooser;
			
			public AddButton() {
				super("Add");
				addActionListener(this);
				fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}

			public void actionPerformed(ActionEvent arg0) {
				int ret = fileChooser.showOpenDialog(getParent());
				if ( ret == JFileChooser.APPROVE_OPTION ) {
					textArea.append(fileChooser.getSelectedFile().getPath()+"\n");
				}
			}
		}
	}
    
    public static class Button extends JButton implements ActionListener
    {
        public Button() {
            super("VST");
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent arg0) {
            showDialog();
        }        
    }
}
