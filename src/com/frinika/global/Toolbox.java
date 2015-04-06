/*
 * Created on Jul 6, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

package com.frinika.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Global collection of static tool methods.
 * 
 * @author Jens Gulden
 */
public class Toolbox {	
	
	/**
	 * Private constructor to avoid instantiaton.
	 */
	private Toolbox() {
		// nop
	}

	public static String joinStrings(Collection<String> ss, String delim) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : ss) {
			if (!first) {
				sb.append(delim);
			}
			sb.append(s);
			first = false;
		}
		return sb.toString();
	}

	public static String joinStrings(String[] ss, String delim) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : ss) {
			if (!first) {
				sb.append(delim);
			}
			sb.append(s);
			first = false;
		}
		return sb.toString();
	}

	public static List<String> splitString(String s, String delim) {
		ArrayList<String> l = new ArrayList<String>();
		int start = 0;
		int d = delim.length();
		int pos;
		do {
			pos = s.indexOf(delim, start);
			String ss = s.substring(start, (pos != -1) ? pos : s.length());
			if ((pos != -1) || (ss.length() > 0)) { // ignore last one if blank only
				l.add(ss.trim());
			}
			start = pos + d;
		} while ((pos != -1) && (start < s.length()));
		return l;
	}

	public static String firstWord(String s) {
		s = s.trim();
		if (s.length() == 0) return "";
		StringTokenizer st = new StringTokenizer(s, " \t\n\r", false);
		String w = st.nextToken();
		return w;
	}

	public static String capitalize(String s) {
		if (s.length() == 0) return "";
		return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
	}

	public static String[] splitWords(String s) {
		StringTokenizer st = new StringTokenizer(s, " \t\r\n", false);
		int size = st.countTokens();
		String[] args = new String[size];
		for (int i = 0; i < size; i++) {
			args[i] = st.nextToken();
		}
		return args;
	}
	
	public static void extractFromJar(File jarfile, String prefix, File targetDir) throws IOException {
		JarInputStream jar = new JarInputStream(new FileInputStream(jarfile));
		JarEntry entry = jar.getNextJarEntry();
		while (entry != null) {
			if ( ! entry.isDirectory() ) {
				String name = entry.getName();
				if ((prefix == null) || (name.startsWith(prefix))) { // entry to decopompress found
					String n;
					int pos = name.lastIndexOf(File.separator);
					if (pos != -1) {
						n = name.substring(pos + File.separator.length()); // name without preceeding path
					} else {
						n = name;
					}
					File outFile = new File(targetDir, n);
					FileOutputStream out = new FileOutputStream(outFile);
					
					System.out.print(name+" -> "+outFile.getAbsolutePath()+", ");			
					byte[] b = new byte[10 * 1024];
					int total = 0;
					int hasRead;
					do {
						hasRead = jar.read(b);
						if (hasRead  > 0) {
							out.write(b, 0, hasRead);
						}
						total += hasRead;
					} while (hasRead > 0);
					System.out.println(total+" bytes");
					out.close();
				}
			}
			entry = jar.getNextJarEntry();
		}
		jar.close();
	}
}
