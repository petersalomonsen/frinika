/*
 * Created on Feb 17, 2007
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

/**
 * Allows an object to get notified about start of scripts and exits of scripts.
 * 
 * @author Jens Gulden
 */
public interface ScriptListener {

	/**
	 * Invoked when the specified script has started.
	 * 
	 * @param script script which just started to be executed by the script engine
	 */
	public void scriptStarted(FrinikaScript script);
	
	/**
	 * Invoked when the specified script has exited.
	 * 
	 * @param script script which just exited from execution by the script engine
	 * @param returnValue a returnValue of null indicates that the script exited with an error 
	 */
	public void scriptExited(FrinikaScript script, Object returnValue);
	
}
