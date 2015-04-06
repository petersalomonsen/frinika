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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Stores a script which can be executed by the FrinikaScriptEngine and 
 * loaded/saved by ScriptingDialog.
 * 
 * @see com.frinika.project.scripting.FrinikaScriptEngine
 * @see com.frinika.project.scripting.gui.ScriptingDialog
 * @author Jens Gulden
 */
public class DefaultFrinikaScript implements FrinikaScript, Serializable, Comparable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String INITIAL_NAME = "untitled";
	
	int language = LANGUAGE_JAVASCRIPT;
	//String name;
	String source;
	String filename; // additionally to interface FrinikaScript
	
	public int getLanguage() {
		return language;
	}
	public void setLanguage(int language) {
		this.language = language;
	}
	public String getName() {
		//return name;
		String filename = getFilename();
		if (filename != null) {
			int slashpos = filename.lastIndexOf(File.separatorChar);
			return filename.substring(slashpos + 1);
		} else {
			return INITIAL_NAME;
		}
	}
	/*public void setName(String name) {
		this.name = name;
	}*/
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		// read source from file (if it's still there, otherwise keep serialized string as source and mark dirty)
		if (filename != null) {
			File f = new File(filename);
			if (f.exists()) {
				try {
					String s = FrinikaScriptingEngine.loadString(f);
					this.source = s; // freshly read from file
				} catch (IOException ioe) {
					// nop
				}
			}
		}
	}
	
	public int compareTo(Object o) {
		if (! (o instanceof FrinikaScript)) {
			return 1;
		} else {
			return this.getName().compareTo(((FrinikaScript)o).getName());
		}
	}
		
	private void writeObject(ObjectOutputStream out) throws ClassNotFoundException, IOException {
		out.defaultWriteObject();
	}
}
