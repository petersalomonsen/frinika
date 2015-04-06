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

/**
 * Interface allowing an object to act as a script.
 * Declaring an interface for this allows any data source to act as scripts,
 * the implementating class used here is DefaultFrinikaScript.
 *
 * @see DefaultFrinikaScript
 * @author Jens Gulden
 */
public interface FrinikaScript {

	public static final int LANGUAGE_JAVASCRIPT = 1; // only one language (might remain like this, but the model is open to more languages)
	public static final int LANGUAGE_GROOVY = 2;
        
	public String getSource();
	
	public int getLanguage();
	
	public String getName();
	
}
