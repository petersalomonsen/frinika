/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.swingui.miscui;

import java.awt.Component;
import javax.swing.JPopupMenu;

abstract public class DynamicPopup extends JPopupMenu
{
    public DynamicPopup() {
    }

    public DynamicPopup(String label) {
        super(label);
    }

    public void show(Component invoker, int x, int y) {
        refreshMenu();
        super.show(invoker, x, y);
    }

	abstract protected void refreshMenu();
}
