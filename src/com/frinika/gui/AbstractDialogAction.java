/*
 * Created on Feb 10, 2007
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

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;

/**
 * Abstract superclass for menu-actions that will pop-up a dialog (usually to
 * prompt options from the user) before actually performing the action.
 *  
 * Note: The implementation is non-reentrant, because action-specific
 * parameters are to be stored in instance-variables to make them accessible
 * accross the different performXxx() methods. However, this is not considered
 * to be harmful as a menu-action is supposed to be invoked from a single-user
 * GUI, thus only one thread at a time runs through actionPerformed().
 * 
 * @see OptionsDialog
 * @see OptionsEditor
 * 
 * @author Jens Gulden
 */
public abstract class AbstractDialogAction extends AbstractAction {
	
	protected ProjectFrame frame;
	protected String actionId;
	protected OptionsDialog dialog;
	protected boolean canceled;

	public AbstractDialogAction(ProjectFrame frame, String actionId) {
		super(getMessage(actionId)+"...");
		this.frame = frame;		
		this.actionId = actionId;
	}
	
	public void actionPerformed(ActionEvent e) {
		canceled = false;
		perform(); // /might use clone().perform() to make thread-safe, but not necessary)
	}
	
	public void cancel() {
		canceled = true;
	}
	
	public void perform() {
		performPrepare();
		if (!canceled) {
			performDialog();
			if (!canceled) {
				performUndoable();
			}
		}
	}
	
	protected void performPrepare() {
		// nop, may be overwritten by subclasses
	}
		
	/**
	 * Ask user for options, usually via modal GUI-dialog.
	 * If cancel is set to true after this method returns,
	 * no action will be taken.
	 * The default implementation shows the modal options dialog, if
	 * one is available, otherwise it does nothing (for commands that are
	 * directly executed without asking options.)
	 */
	protected void performDialog() {
		// usually not overwritten by subclasses
		OptionsDialog dialog = getDialog();
		if (dialog != null) {
			dialog.show();
		}
		if (dialog.isCanceled()) {
			this.cancel();
		}
	}
		
	protected void performUndoable() {
		// usually not overwritten by subclasses
		ProjectContainer project = frame.getProjectContainer();
		project.getEditHistoryContainer().mark(getMessage(actionId));
		performAction();
		project.getEditHistoryContainer().notifyEditHistoryListeners();
	}
	
	abstract protected void performAction();
	
	public OptionsDialog getDialog() {
		if (this.dialog == null) {
			this.dialog = createDialog(); // auto-create on first get (avoids confusion with constructor-execution-order)
		}
		return this.dialog;
	}
	
	/**
	 * Creates a dialog to ask for options.
	 * Typically not overwritten by subclasses that use a gui-editor,
	 * they provide an inner editor-component to the dialog instead via
	 * createDialogContent(). Overwrite this method here if you need other kinds
	 * of dialogs than one with OK/Cancel buttons. 
	 * 
	 * @return
	 */
	protected OptionsDialog createDialog() {
		JComponent content = createGUI();
		if (content != null) {
			OptionsDialog d = new OptionsDialog(frame, content, getMessage(actionId));
			return d;
		} else {
			return null;
		}
	}
	
	public ProjectFrame getProjectFrame() {
		return frame;
	}
		
	/**
	 * Creates inner gui-editor of a dialog to show to the user.
	 * The implementation may just return null, which makes
	 * the class effectually behave as an AbstractAction without user-interaction.
	 * 
	 * @return
	 */
	abstract protected JComponent createGUI();
	
}
