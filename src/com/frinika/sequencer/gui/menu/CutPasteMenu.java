/*
 * Created on Feb 25, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.sequencer.gui.menu;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;

import static com.frinika.localization.CurrentLocale.getMessage;	
/**
 * Standard menu items for use in MultiEvent editors such as PianoRoll or
 * EventList
 * 
 * @author Peter Johan Salomonsen
 */
public class CutPasteMenu extends JMenu {
	private static final long serialVersionUID = 1L;

	JMenuItem undoMenuItem = new JMenuItem();

	JMenuItem redoMenuItem = new JMenuItem();

	JMenuItem cutMenuItem;

	JMenuItem copyMenuItem;

	JMenuItem pasteMenuItem;

	JMenuItem deleteMenuItem;
	
	public static Icon getIconResource(String name)
	{
		return  new javax.swing.ImageIcon(ProjectFrame.class.getResource("/icons/" + name));
	}	

	public CutPasteMenu(final ProjectContainer project) {

		setText(getMessage("project.menu.edit"));

		setMnemonic(KeyEvent.VK_E);

		JMenuItem item;
		add(item=project.getEditHistoryContainer().getUndoMenuItem());
		item.setIcon(getIconResource("undo.gif"));
		add(item=project.getEditHistoryContainer().getRedoMenuItem());
		item.setIcon(getIconResource("redo.gif"));
		addSeparator();

		cutMenuItem = new JMenuItem(new CutAction(project));
		cutMenuItem.setIcon(getIconResource("cut.gif"));
		cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		add(cutMenuItem);

		copyMenuItem = new JMenuItem(new CopyAction(project));
		copyMenuItem.setIcon(getIconResource("copy.gif"));
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		add(copyMenuItem);
		pasteMenuItem = new JMenuItem(new PasteAction(project));
		pasteMenuItem.setIcon(getIconResource("paste.gif"));
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		add(pasteMenuItem);
		
		addSeparator();		
		
		deleteMenuItem = new JMenuItem(new DeleteAction(project));
		deleteMenuItem.setIcon(getIconResource("delete.gif"));
		deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0));
		add(deleteMenuItem);
		

	}

	/**
	 * 
	 * @return Returns deleteMenuItem
	 */
	public JMenuItem getDeleteMenuItem() {
		return deleteMenuItem;
	}

	/**
	 * @return Returns the copyMenuItem.
	 */
	public JMenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	/**
	 * @return Returns the cutMenuItem.
	 */
	public JMenuItem getCutMenuItem() {
		return cutMenuItem;
	}

	/**
	 * @return Returns the pasteMenuItem.
	 */
	public JMenuItem getPasteMenuItem() {
		return pasteMenuItem;
	}

	/**
	 * 
	 * Keyboard events not to be handled by the table component - since they are
	 * menu accelerators
	 * 
	 * @param ks
	 * @return
	 */
	public static boolean isAccelerator(KeyStroke ks) {
		return (ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				Toolkit .getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_C,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_V,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
				||

				ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0))
				|| ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0)) || ks
				.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0))

		);
	}
}
