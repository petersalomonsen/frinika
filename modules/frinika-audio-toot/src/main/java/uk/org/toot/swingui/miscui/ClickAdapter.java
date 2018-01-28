package uk.org.toot.swingui.miscui;

import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClickAdapter extends MouseAdapter {

    protected JPopupMenu popup;
    protected boolean both = false;

    public ClickAdapter(JPopupMenu popup) {
        this.popup = popup;
    }

    public ClickAdapter(JPopupMenu popup, boolean both) {
        this(popup);
        this.both = both;
    }

    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }

    public void mousePressed(MouseEvent e) {
	    maybeShowPopup(e);
    }

	public void mouseReleased(MouseEvent e) {
    	maybeShowPopup(e);
    }

    public void maybeShowPopup(MouseEvent e) {
    	if ( popup == null ) return;
        if ( e.isPopupTrigger() || both ) {
             if (!popup.isVisible()) {
                showPopup(e);
             }
        } else {
            // left click selections, process how?
            select(e);
        }
    }

    public void showPopup(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(),  e.getY());
    }

    /*
     * null implementation, override if required
     */
    public void select(MouseEvent e) {
    }
}


