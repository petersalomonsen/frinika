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

package com.frinika.project.scripting;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.scripting.gui.ScriptingDialog;

/**
 * Thread for running a script. This thread will mainly live inside the 
 * Rhino-JavaScript engine.
 * 
 * @author Jens Gulden
 */
class ScriptThread extends Thread {
	
	private FrinikaScript script;
	private ProjectFrame frame;
	private ScriptingDialog dialog;

	ScriptThread(FrinikaScript script, ProjectFrame frame, ScriptingDialog dialog) {
		super();
		this.script = script;
		this.frame = frame;
		this.dialog = dialog;
	}
		
	public void run() {
		FrinikaScriptingEngine.runningScripts.put(script, this);
		FrinikaScriptingEngine.notifyScriptListeners(script, script);
		
		Object result = FrinikaScriptingEngine.runScript(script, frame, dialog);
		
		FrinikaScriptingEngine.notifyScriptListeners(script, result);
		FrinikaScriptingEngine.runningScripts.remove(script);
	}
}