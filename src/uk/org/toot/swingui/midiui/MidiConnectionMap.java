// Copyright (C) 2005 - 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.midiui;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import uk.org.toot.midi.core.*;

public class MidiConnectionMap extends JPanel implements Observer
{
    private JList outPorts;
    private JList inPorts;
    private ConnectedMidiSystem system;

    public MidiConnectionMap(ConnectedMidiSystem system) {
        super(new BorderLayout());
        this.system = system;
        update(null, null);
        system.addObserver(this);
    }

    public void update(Observable obs, Object arg) {
        removeAll();
        outPorts = createJList(true);
        outPorts.setCellRenderer(new RightAlignedListCellRenderer());
        add(outPorts, BorderLayout.WEST);
        inPorts = createJList(false);
        add(inPorts, BorderLayout.EAST);
        add(createWiring(), BorderLayout.CENTER);
        add(createControls(), BorderLayout.SOUTH);
        validate(); // !!! why is this necessary all of a sudden !!!
        repaint();
    }

    protected <T extends MidiPort> JList createJList(boolean isOut) {
        List<? extends MidiPort> ports = isOut ? system.getMidiOutputs() : system.getMidiInputs();
        JList list = new JList(ports.toArray());
        list.setFixedCellHeight(16);
        return list;
	}

    protected JComponent createWiring() {
        return new Wiring();
    }

    protected class Wiring extends JComponent
    {
    	private Dimension prefSize = new Dimension(128, 64);
    	
        public void paint(Graphics g) {
        	for ( MidiConnection connection : system.getMidiConnections() ) {
            	MidiPort txPort = connection.getMidiOutput();
	            MidiPort rxPort = connection.getMidiInput();
	            //System.out.println("wiring for Connection from "+txName+" to "+rxName);
    	        int txpos = -1, rxpos = -1;
        	    for ( int i = 0 ; i < outPorts.getModel().getSize(); i++ ) {
            	    if ( txPort.equals((MidiPort)outPorts.getModel().getElementAt(i)) ) {
	                    txpos = i;
    	                break;
        	        }
            	}
        	    for ( int i = 0 ; i < inPorts.getModel().getSize(); i++ ) {
            	    if ( rxPort.equals((MidiPort)inPorts.getModel().getElementAt(i)) ) {
	                    rxpos = i;
    	                break;
        	        }
            	}
        		if ( txpos < 0 || rxpos < 0 ) {
            		System.err.println("Can't paint Wiring for Connection from "+txPort+"("+txpos+") to "+rxPort+"("+rxpos+")");
            		continue; // can't draw
        		}
                Point txpoint = outPorts.indexToLocation(txpos);
                Point rxpoint = inPorts.indexToLocation(rxpos);
                txpoint.x = 0;
                txpoint.y += outPorts.getFixedCellHeight()/2;
               	rxpoint.x = getWidth()-1;
                rxpoint.y += inPorts.getFixedCellHeight()/2;
                Color color = !connection.isPlayback() ?
                    (connection.isSystem() ? Color.orange.darker() : Color.red) :
                    (connection.isSystem() ? Color.blue : Color.black);
				g.setColor(color);
                g.drawLine(txpoint.x, txpoint.y, rxpoint.x, rxpoint.y);
	        }
        }
        
        public Dimension getPreferredSize() {
        	return prefSize;
        }
    }

    protected JComponent createControls() {
        JPanel panel = new JPanel();
        panel.add(new MapButton("Connect"));
        panel.add(new MapButton("Disconnect"));
        return panel;
    }

    /**
     * Create a new MidiConnection from the currently selected MidiSource
     * to the currently selected MidiSink.
     */
    protected void connect() {
        String txName = outPorts.getSelectedValue().toString();
        String rxName = inPorts.getSelectedValue().toString();
        if ( txName == null || rxName == null ) return; // should be disabled at control too
        try {
        	system.createMidiConnection(txName, rxName, 0);
            //System.out.println("created Connection from "+txName+" to "+rxName);
        } catch ( Exception e) {
            System.err.println("Failed to create Connection from "+txName+" to "+rxName);
        }
    }

    protected void disconnect() {
        String txName = outPorts.getSelectedValue().toString();
        String rxName = inPorts.getSelectedValue().toString();
        if ( txName == null || rxName == null ) return; // should be disabled at control too
        try {
            system.closeMidiConnection(txName, rxName);
            //System.out.println("closed Connection from "+txName+" to "+rxName);
        } catch ( Exception e) {
            System.err.println("Failed to close Connection from "+txName+" to "+rxName);
        }
    }

    protected class MapButton extends JButton implements ActionListener
    {
        public MapButton(String text) {
            super(text);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            if ( "Connect".equals(cmd) ) {
	            connect();
            } else if ( "Disconnect".equals(cmd) ) {
                disconnect();
            }
        }
    }

    private class RightAlignedListCellRenderer extends DefaultListCellRenderer implements ListCellRenderer<Object> {
	    // This is the only method defined by ListCellRenderer.
    	// We just reconfigure the JLabel each time we're called.
	    public Component getListCellRendererComponent(
    	   	JList list,
       		Object value,            // value to display
       		int index,               // cell index
       		boolean isSelected,      // is the cell selected
       		boolean cellHasFocus)    // the list and the cell have the focus
     	{
         	JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         	label.setHorizontalAlignment(RIGHT);
            return this;
     	}
 }
}
