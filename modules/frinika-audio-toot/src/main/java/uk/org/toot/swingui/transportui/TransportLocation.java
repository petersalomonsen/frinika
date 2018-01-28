// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.transportui;

import uk.org.toot.transport.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TransportLocation extends JLabel
{
    private Timer timer = null;
    private String seperator1; // avoid continuous instantiation
    public TransportLocation(final Transport transport) {
        ActionListener actionListener = new ActionListener() {
            private long prevus = -1;
            public void actionPerformed(ActionEvent ae) {
                long us = transport.getMicrosecondLocation();
                if ( us != prevus ) {
                	setText(TransportLocation.this.location(us));
                    prevus = us;
                }
            }
        };
        timer = new Timer(242, actionListener);
		setFont(getFont().deriveFont(18f));
    }

    /** Return position in form mm:ss.* */
    public String location(long us) {
        float sec = (float)us / 1000000L;
        int s = (int)sec;
        int mm = (int)(s / 60);
        float ss = sec - 60 * mm;
        seperator1 = ss < 10 ? ":0" : ":";
        return mm + seperator1 + ss;
    }

    public void addNotify() {
        super.addNotify();
        if ( timer != null ) {
            timer.start();
//            System.out.println("Timer started for Transport Location");
        }
    }

    public void removeNotify() {
        if ( timer != null ) {
            timer.stop();
//            System.out.println("Timer stopped for Transport Location");
        }
        super.addNotify();
    }
}
