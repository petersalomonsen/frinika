// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.util.Iterator;
import uk.org.toot.service.*;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.swingui.controlui.spi.ControlPanelServiceProvider;
import javax.swing.JComponent;
import uk.org.toot.control.ControlSelector;

public class ControlPanelServices extends Services
{
    protected ControlPanelServices() {
    }

    public static JComponent createControlPanel(CompoundControl controls, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
        JComponent panel;
        Iterator<ControlPanelServiceProvider> it = providers();
        while ( it.hasNext() ) {
            panel = it.next().createControlPanel(controls, axis, s, f, hasBorder, hasHeader);
            if ( panel != null ) return panel;
        }
        return null;
    }

    public static Iterator<ControlPanelServiceProvider> providers() {
        return lookup(ControlPanelServiceProvider.class);
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
        Iterator<ControlPanelServiceProvider> pit = providers();
        while ( pit.hasNext() ) {
            ControlPanelServiceProvider asp = pit.next();
            asp.accept(v, clazz);
        }
	}

	public static void printServiceDescriptors(Class<?> clazz) {
        accept(new ServicePrinter(), clazz);
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

