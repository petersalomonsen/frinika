// Thanks to DrLaszloJamf for the original source
// http://forum.java.sun.com/thread.jspa?threadID=481481&messageID=2244706

package uk.org.toot.swingui;

import java.awt.event.*;
import javax.swing.*;

abstract public class DisposablePanel extends JPanel
{
    // should be private with a protected accessor !!! !!!
    protected boolean doDispose = false;

    private WindowListener wl = new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
            dispose();
            doDispose = true;
        }
    };
 
    public void addNotify() {
        super.addNotify();
        SwingUtilities.windowForComponent(this).addWindowListener(wl);
    }
 
    public void removeNotify() {
        SwingUtilities.windowForComponent(this).removeWindowListener(wl);
        if ( doDispose ) wl = null;
        super.removeNotify();
    }

 	/**
     * Implement this method to remove references that prevent this panel
     * from being garbage collected.
     **/
    abstract protected void dispose();
}

