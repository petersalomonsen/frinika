// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import uk.org.toot.swingui.miscui.VstSetupUI;

/**
 * This class is responsible for setup of VST plugin paths.
 * @author st
 *
 */
public class VstSetup
{
	private static File pathFile;	
	private static List<File> pluginPaths;
	private static final String VST_PATHS = "vst.paths";	
	private static boolean available = false;
	
	static {
		File tootdir = new File(System.getProperty("user.home"), "toot");
		pathFile = new File(tootdir, VST_PATHS);
		try {
			Class.forName("uk.org.toot.misc.Vst");
			available = true;
		} catch ( ClassNotFoundException e ) {
			// empty
		}
	}																																		

	/**
	 * @return whether VST Plugin support is available from the SPI
	 */
	public static boolean isVstAvailable() {
		return available;
	}
	
	/**
	 * @return a List of the VST plugin paths.
	 */
	public static List<File> readPaths() {
		if ( !pathFile.exists() ) return Collections.<File>emptyList();
		try {
			List<File> paths = new java.util.ArrayList<File>();
			BufferedReader br = new BufferedReader(new FileReader(pathFile));
			String line;
			while ((line = br.readLine()) != null) {
				File file = new File(line);
				if ( !file.exists() ) continue;
				paths.add(file);
			}
			br.close();
			return paths;
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return Collections.<File>emptyList();
	}
	
	/**
	 * Note that the written file will not have any effect until the next time
	 * the application is started if called arbitrarily.
	 * @param paths the List of VST plugin paths to write
	 */
	public static void writePaths(List<File> paths) {
		try {
			PrintStream ps = new PrintStream(pathFile);
			for ( File path : paths ) {
				ps.println(path.getPath());
			}
			ps.println();
			ps.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * If the file containing the VST plugin paths doesn't exist
	 * we show a user dialog to set these paths and write them to the file.
	 * The file should therefore exist after the dialog is closed.
	 * @return a List of the VST plugin paths.
	 */
	public static List<File> getPaths() {
		if ( pluginPaths == null ) {
			if ( !pathFile.exists() ) {
				VstSetupUI.showDialog(Collections.<File>emptyList());
			}
			pluginPaths = readPaths();
		}
		return pluginPaths;
	}
}
