// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Component;
import javax.swing.*;
import uk.org.toot.control.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LawControlPresetMenu extends JPopupMenu implements ActionListener
{
    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
    private static LawControlPresetMenu instance;
	private LawControl control;

    protected LawControlPresetMenu() {
        // prevent instantiation
    }

    public static JPopupMenu getInstance() {
        if ( instance == null ) {
            instance = new LawControlPresetMenu();
        }
        return instance;
    }

    // removeAll on hide!!!
    public void show(Component invoker, int x, int y) {
        ControlPanel panel = (ControlPanel)invoker.getParent();
        control = (LawControl)panel.getControl();
        removeAll();
        String[] names = control.getPresetNames();
        if ( names == null ) return;
        for ( int i = 0; i < names.length; i++ ) {
            JMenuItem item = new JMenuItem(names[i]);
            add(item);
            item.addActionListener(this);
        }
        // create dynamic menu here
        super.show(invoker, x, y);
    }

    public void actionPerformed(ActionEvent event) {
        control.applyPreset(event.getActionCommand());
    }
}


