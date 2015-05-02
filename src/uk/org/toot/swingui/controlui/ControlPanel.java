// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Font;
import java.util.Observer;
import java.util.Observable;
import javax.swing.JPanel;
import javax.swing.Timer;
import uk.org.toot.control.Control;

public abstract class ControlPanel extends JPanel implements Observer
{
    private Control control;
    private Timer timer = null;
    protected static Font font = new Font("Arial", Font.PLAIN, 10);

    public ControlPanel(Control c) {
        control = c;
    }

    public Control getControl() {
        return control;
    }

    protected void setTimer(Timer t) {
        timer = t;
    }

    public void addNotify() {
        super.addNotify();
       	control.addObserver(this);
        if ( timer != null ) {
            timer.start();
//            System.out.println("Timer started for "+control.getName());
        }
    }

    public void removeNotify() {
        super.removeNotify();
	    control.deleteObserver(this);
        if ( timer != null ) {
            timer.stop();
//            System.out.println("Timer stopped for "+control.getName());
        }
    }

    protected String abbreviate(String string) {
       	if ( string.length() > 5 ) {
        	int trunc = 4; // :( but works well for mixer !!!
            final int last = string.charAt(trunc-1);
            if ( "aeiou".indexOf(last) >= 0 ) trunc--; // don't end with vowel
           	return string.substring(0, trunc);
   		}
        return string;
    }

    public String getName() { return control.getName(); }

   	public void update(Observable obs, Object obj) { }
}
