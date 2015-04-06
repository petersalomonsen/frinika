/*
 * Created on Sep 7, 2007
 *
 * Copyright (c) 2004-2007 Peter Johan Salomonsen
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

package com.frinika;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.Vector;

public class VersionProperties {
	static ResourceBundle versionProps = ResourceBundle.getBundle("version");
	
	public static String getVersion()
	{
		return versionProps.getString("version");
	}
	
	public static String getBuildDate()
	{
		return versionProps.getString("build-date");
	}
	
	public static String getCopyrightStart()
	{
		return versionProps.getString("copyrightStart");
	}
	
	public static String getCopyrightEnd()
	{
		return versionProps.getString("copyrightEnd");
	}
	
	public static void main(String[] args) throws Exception
	{
		File versionPropsFile = new File("src/version.properties");
		BufferedReader rd = new BufferedReader(new FileReader(versionPropsFile));
		Vector<String> lines = new Vector<String>();
		String line = rd.readLine();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		while(line!=null)
		{
			

			if(line.indexOf("copyrightEnd")==0)
				line="copyrightEnd			= "+new GregorianCalendar().get(GregorianCalendar.YEAR);
			else if(line.indexOf("build-date")==0)
				line="build-date				= "+dateFormat.format(new Date());
			
			System.out.println(line);
			lines.add(line);
			line = rd.readLine();
		}
		
		rd.close();
		versionPropsFile.delete();
		versionPropsFile.createNewFile();
		BufferedWriter wr = new BufferedWriter(new FileWriter(versionPropsFile));
		for(String ln : lines)
		{
			wr.write(ln);
			wr.newLine();
		}
		wr.close();
	}
}
