// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.mixerui;

import java.awt.Component;
import javax.swing.*;

import uk.org.toot.control.automation.SnapshotAutomation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static uk.org.toot.misc.Localisation.*;

public class SnapshotAutomationPopupMenu extends JPopupMenu implements ActionListener
{
    private SnapshotAutomation automation;

    public SnapshotAutomationPopupMenu(SnapshotAutomation automation) {
        this.automation = automation;
    }

    public void show(Component invoker, int x, int y) {
        removeAll();
        String[] names = automation.list();
        if ( names != null ) {
    	    JMenu recallMenu = new JMenu(getString("Recall"));
	        add(recallMenu);
            for ( int i = 0; i < names.length; i++ ) {
            	if ( !names[i].endsWith("mixer-snapshot") ) continue;
                String name = names[i].substring(0, names[i].lastIndexOf("."));
                JMenuItem item = new JMenuItem(name);
                recallMenu.add(item);
                item.addActionListener(this);
            }
        }
        JMenuItem storeItem = new JMenuItem(new StoreAction());
        add(storeItem);
        super.show(invoker, x, y);
    }

    public void actionPerformed(ActionEvent event) {
        automation.recall(event.getActionCommand());
    }

    protected class StoreAction extends AbstractAction
    {
        public StoreAction() {
            super(getString("Store.As"+"..."));
        }

    	public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(
                						getString("Store.As"+"..."));
            if ( name != null ) {
            	automation.store(name);
            }
        }
    }
}
