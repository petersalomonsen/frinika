// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import javax.swing.*;
import uk.org.toot.service.*;
import uk.org.toot.audio.server.*;

public class AudioServerCombo extends JComboBox
{
	public AudioServerCombo(final String serverName) {
        AudioServerServices.accept(
            new ServiceVisitor() {
            	public void visitDescriptor(ServiceDescriptor d) {
               		addItem(d.getName());
            	}
        	}, AudioServer.class
        );
        if ( serverName != null ) {
        	setSelectedItem(serverName);
        }
    }
}
