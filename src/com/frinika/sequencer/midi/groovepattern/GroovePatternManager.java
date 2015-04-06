/*
 * Created on Mar 6, 2007
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

package com.frinika.sequencer.midi.groovepattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.frinika.global.FrinikaConfig;
import com.frinika.sequencer.model.MidiPart;

/**
 * <<singleton>>
 * Globally handles groove-patterns for the groove-quantization feature.   
 * 
 * TODO Currently stores user-defined or imported patterns as .mid files in a directory
 * .frinika-groove-patterns/. This is very simple, persistent storage of patterns should 
 * be configurable via user-options.
 * 
 * @see com.frinika.sequencer.gui.menu.midi.MidiQuantizeAction
 * @author Jens Gulden
 */
public class GroovePatternManager {
	
	public final static String PRESETS_PACKAGE = "groovepatterns";
	public final static String[] PRESETS = new String[] { "test1" }; // TODO
	
	// --- static ---
	
	private static GroovePatternManager instance; // singleton instance
	private static Map<String, GroovePattern> presetPatterns;
	private static Map<String, GroovePatternFromSequence> userPatterns;

	public static GroovePatternManager getInstance() {
		if (instance == null) {
			initPresets();
			loadUserPatterns();
			instance = new GroovePatternManager();
		}
		return instance;
	}
	
	private static void initPresets() {
		presetPatterns = new HashMap<String, GroovePattern>();
		for (int i = 0; i < PRESETS.length; i++) {
			String name = PRESETS[i];
			InputStream res = ClassLoader.getSystemResourceAsStream(PRESETS_PACKAGE + "/" + name + ".mid");
			try {
				loadPresetPattern(presetPatterns, name, res);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	private static void loadUserPatterns() {
		userPatterns = new HashMap<String, GroovePatternFromSequence>();
		File dir = FrinikaConfig.GROOVE_PATTERN_DIRECTORY; //new File( FrinikaConfig.GROOVE_PATTERN_DIRECTORY );
		if ( dir.exists() ) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				if (name.endsWith(".mid")) {
					name = name.substring(0, name.length() - 4);
					try {
						FileInputStream in = new FileInputStream(file);
						loadPresetPattern(userPatterns, name, in);
						in.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
	}
	
	private static void loadPresetPattern(Map map, String name, InputStream in) throws IOException {
		GroovePatternFromSequence gp = new GroovePatternFromSequence();
		gp.importFromMidiFile(name, in);
		map.put(name, gp);
	}
	
	private static void addUserPattern(GroovePatternFromSequence gp) {
		userPatterns.put(gp.getName(), gp);
	}
	
	private static void removeUserPattern(GroovePatternFromSequence gp) {
		userPatterns.remove(gp.getName());
	}
	
	
	private static boolean hasUserPattern(String name) {
		return (userPatterns != null) && (userPatterns.get(name) != null);
	}
	
	private static void storeUserPattern(GroovePatternFromSequence groovePattern) throws IOException {
		// ensure storage dir exists
		File dir = FrinikaConfig.GROOVE_PATTERN_DIRECTORY; //new File(STORAGE_DIRECTORY);
		dir.mkdir();
		// save
		groovePattern.saveAsMidiFile( storageFile(groovePattern) );
	}
	
	public static String normalizeName(String name) {
		// replace all blanks and non-filename-like characters
		return name.replace(' ', '_').replace('\t','_').replace('.','_').replace(':','_').replace('/','_').replace('\\','_').replace('?','_').replace('*','_');
	}
	
	// --- instance ---
	
	private GroovePatternManager() {
		// singleton constructor
	}
	
	public Collection<GroovePattern> getGroovePatterns() {
		ArrayList<GroovePattern> l = new ArrayList<GroovePattern>();
		l.addAll( getPresetGroovePatterns() );
		l.addAll( getUserGroovePatterns() );
		return l;
	}
	
	/**
	 * Finds a groove pattern by name.
	 * 
	 * @param name
	 * @return a groove pattern with the requested unique name, or null if ot found
	 */
	public GroovePattern getGroovePattern(String name) {
		for (GroovePattern gp : getGroovePatterns()) {
			if (gp.getName().equals(name)) {
				return gp;
			}
		}
		return null;
	}

	public Collection<GroovePattern> getPresetGroovePatterns() {
		return presetPatterns.values();
	}
	
	public Collection<GroovePatternFromSequence> getUserGroovePatterns() {
		return userPatterns.values();
	}
	
	public GroovePatternFromSequence importUserGroovePattern(File midiFile) throws IOException {
		GroovePatternFromSequence gp = new GroovePatternFromSequence();
		gp.importFromMidiFile(midiFile);
		ensureUniqueName(gp.getName());
		addUserPattern(gp);
		storeUserPattern(gp);
		return gp;
	}
	
	public GroovePatternFromSequence importUserGroovePattern(String name, MidiPart part) throws IOException {
		ensureUniqueName(name);
		GroovePatternFromSequence gp = new GroovePatternFromSequence();
		gp.importFromMidiPart(name, part);
		addUserPattern(gp);
		storeUserPattern(gp);
		return gp;
	}
	
	public void removeUserGroovePattern(GroovePatternFromSequence gp) throws IOException {
		if (hasUserPattern(gp.getName())) {
			removeUserPattern(gp);
			// delete file
			File file = storageFile(gp);
			if ( ! file.delete() ) {
				throw new IOException("unable to delete file " + file.getAbsolutePath());
			}
		}
	}
	
	private static File storageFile(GroovePatternFromSequence gp) {
		return new File(FrinikaConfig.GROOVE_PATTERN_DIRECTORY, normalizeName(gp.getName()) + ".mid" );
	}
	
	private static void ensureUniqueName(String name) throws IOException {
		if ( hasUserPattern( name ) ) {
			throw new IOException("a groove pattern named '"+name+"' already exists");
		}
	}
}
