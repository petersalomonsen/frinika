/*
 * Created on Feb 13, 2007
 *
 * Copyright (c) 2007 Jens Gulden
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.gui;

import com.frinika.project.gui.ProjectFrame;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * A simple extension to JDialog, as base class for other dialogs.
 * Additionally to JDialog, instances of this class keep a type-safe reference to the current
 * ProjectFrame, and provide a cancel() method that can either be called programmatically, or
 * will ce called when the user pressed the esc-key.
 *  
 * @author Jens Gulden
 */
public class AbstractDialog extends JDialog {
	
	protected ProjectFrame frame;
    protected boolean canceled;

	public AbstractDialog() throws HeadlessException {
		super();
		init(null);
	}

	public AbstractDialog(ProjectFrame owner) throws HeadlessException {
		super(owner);
		init(owner);
	}

	public AbstractDialog(AbstractDialog owner) throws HeadlessException {
		super(owner);
		init(owner.getProjectFrame());
	}

	public AbstractDialog(ProjectFrame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		init(owner);
	}

	public AbstractDialog(ProjectFrame owner, String title) throws HeadlessException {
		super(owner, title);
		init(owner);
	}

	public AbstractDialog(AbstractDialog owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		init(owner.getProjectFrame());
	}

	public AbstractDialog(AbstractDialog owner, String title) throws HeadlessException {
		super(owner, title);
		init(owner.getProjectFrame());
	}

	public AbstractDialog(ProjectFrame owner, String title, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		init(owner);
	}

	public AbstractDialog(AbstractDialog owner, String title, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		init(owner.getProjectFrame());
	}

	public AbstractDialog(ProjectFrame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		init(owner);
	}

	public AbstractDialog(AbstractDialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException {
		super(owner, title, modal, gc);
		init(owner.getProjectFrame());
	}

	private void init(ProjectFrame frame) {
		this.frame = frame;
        // close on esc:
        final String ESC_CANCEL = "esc-cancel";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC_CANCEL);
        getRootPane().getActionMap().put(ESC_CANCEL, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                cancel();
            }
        });
	}
	
	public MoreLessButtonListener registerMoreLessButtonPanel(JButton button, JPanel panel) {
		return new MoreLessButtonListener(button, panel);
	}
	
    public boolean isCanceled() {
        return canceled;
    }
    
/**
	 * Called when Ok is chosen.
	 * To be optionally overloaded by subclasses.
	 */
	public void ok() {
		this.hide();
	}
	
	/**
	 * Called when the dialog is canceled, e.g. by pressing esc.
	 * To be overloaded by subclasses.
	 */
	public void cancel() {
        canceled = true;
		this.hide();
	}
	
	public ProjectFrame getProjectFrame() {
		return frame;
	}

    public static void centerOnScreen(JDialog d) {
        Dimension screenSize = d.getToolkit().getScreenSize();
        Dimension size = d.getSize();
        int x = (screenSize.width - size.width) / 2;
        int y = (screenSize.height - size.height) / 2;
        d.setLocation(x, y);
    }
    
    // --- inner class ---
    
    public class MoreLessButtonListener implements ActionListener {
    	
    	protected JButton button;
    	protected JComponent panel;
    	protected int panelHeight;
    	
    	public MoreLessButtonListener(JButton button, JComponent panel, boolean initiallyShowMore) {
			this.button = button;
			this.panel = panel;
			this.panelHeight = panel.getPreferredSize().height + panel.getInsets().top + panel.getInsets().bottom;
			button.addActionListener(this);
			setOpen(initiallyShowMore);
		}

    	public MoreLessButtonListener(JButton button, JComponent panel) {
    		this(button, panel, false);
    	}
    	
		public void actionPerformed(ActionEvent e) {
			setOpen( ! isOpen() );
		}
		
		protected boolean isOpen() {
			//return panel.getParent() != null;
			return panel.isVisible();
		}
		
		protected void setOpen(boolean open) {
			panel.setVisible(open);
			Dimension size = AbstractDialog.this.getSize();
			int newH;
			if (open) {
				newH = size.height + panelHeight;
				button.setText("<< Less");
			} else {
				newH = size.height - panelHeight;
				button.setText("More >>");
			}
			AbstractDialog.this.setSize(size.width, newH);
		}
    	
    }
}
