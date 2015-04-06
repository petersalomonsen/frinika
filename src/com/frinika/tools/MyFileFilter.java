/*
 * Created on Nov 6, 2004
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
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.tools;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class MyFileFilter extends FileFilter {
		String extension;
		String description;
		
		public MyFileFilter(String extension, String description)
		{
			this.extension = extension;
			this.description = description;
		}
			
		public boolean accept(File f) {
				
			if(f.getName().toLowerCase().indexOf(extension)>0 || f.isDirectory())
				return true;
			else
				return false;
		}

		public String getDescription() {
			return description;
		}
}
