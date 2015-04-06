/*
 * Created on Feb 16, 2007
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

package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.scripting.FrinikaScript;
import com.frinika.project.scripting.gui.ScriptingDialog;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

/**
 * 
 * This class will be instantiated multiple times, using
 * the two different constructors with different semantics: constructor
 * ScriptingAction(frame) opens the project-wide scripting-dialog, while
 * constructor ScriptingAction(frame, FrinikaScript) will provide an action that
 * immediately executed the specified script.
 * This action is used to build the "Tools/Scripting" sub-menu.
 * 
 * Although this inherits AbstractMidiAction for implementation reasons, the
 * use of scripts generally is not restricted to modifying midi events, so
 * this class is located in package com.frinika.sequencer.gui.menu.
 * 
 * @author Jens Gulden
 */
public class ScriptingAction extends AbstractAction {

	public final static String actionId = "sequencer.project.scripting"; // also accessed by ScriptingDialog

	private ProjectFrame frame;
	private FrinikaScript script; 
	private ScriptingDialog scriptingDialog;

	
	public ScriptingAction(ProjectFrame frame) {
		super(getMessage(actionId));
		this.frame = frame;
		script = null;
	}
	
	public ScriptingAction(ProjectFrame frame, FrinikaScript script) {
		super(script.getName());
		this.frame = frame;
		this.script = script;
		scriptingDialog = null;
	}

	public void initDialog(JMenu scriptingSubmenu) { // must be called extra, after menu-item as been added to submenu
		scriptingDialog = new ScriptingDialog(frame, scriptingSubmenu);
	}

	public void actionPerformed(ActionEvent e) {
		if ( script == null ) {
			if(JOptionPane.showConfirmDialog(scriptingDialog, "WARNING: Scripts may damage your "
                            + "computer and abuse your private data.\r\nDo not run a script if you don't know what "
                            + "the script is doing.","Script warning",JOptionPane.OK_CANCEL_OPTION)
                            == JOptionPane.OK_OPTION) {
                        scriptingDialog.setVisible(true);
                    }
		} else {
			frame.getProjectContainer().getScriptingEngine().executeScript(script, frame, null);
		}
	}
}
