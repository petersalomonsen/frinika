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

package com.frinika.project.scripting.javascript;

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.scripting.FrinikaScriptingEngine;
import com.frinika.project.scripting.gui.ScriptingDialog;
import com.frinika.project.scripting.javascript.JavascriptScope;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.gui.transport.StartAction;
import com.frinika.sequencer.gui.transport.StopAction;
import com.frinika.sequencer.gui.transport.RecordAction;
import com.frinika.sequencer.gui.transport.RewindAction;
import com.frinika.sequencer.gui.menu.DeleteAction;
import com.frinika.sequencer.gui.menu.midi.MidiStepRecordAction;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.TextLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.AudioPart;
import com.frinika.sequencer.model.TextPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.SysexEvent;
import com.frinika.sequencer.model.Ghost;
import com.frinika.sequencer.model.util.TimeUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptableObject;

import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class provides the bridge between Frinika and the Rhino Javascript
 * engine. It maps important parts of Frinika's object structure and features to
 * Javascript variables and functions.
 * 
 * <h1>JavaScript Reference</h1>
 * 
 * The Frinika scripting engine uses a JavaScript engine which implements the
 * ECMAScript for XML (E4X). For detailed information on the
 * ECMA-script standard and provided language features, refer to the documentation
 * available at <a href="http://developer.mozilla.org/en/docs/JavaScript_Language_Resources">JavaScript Language Resources</a>:
 * <ul>
 * <li><a href="http://www.mozilla.org/js/language/E262-3.pdf">Third revision of the ECMAScript standard, corresponds to JavaScript 1.5</a></li>
 * <li><a href="http://www.mozilla.org/js/language/ECMA-357.pdf">ECMAScript for XML (E4X)</a></li>
 * </ul>
 * 
 * As addition to the core language capabilities provided by the ECMA-script standard,
 * the following functions, objects and variables are provided as specific
 * additions to interface between Frinika and JavaScript.
 * 
 * 
 * <h2>Global Functions</h2>
 * 
 * Some built-in top-level functions specific to Frinika are available within
 * JavaScript. Top-level funcitons are not bound to any object, which means they
 * can be invoked directly using statement as "myfunction(args1, args2)",
 * without any priop object reference (such as "myObject.myfunction(arg)").
 * 
 * <h4><code>print(string)</code></h4>
 * 
 * Prints a string to the scripting console.
 * 
 * <h4><code>println(string)</code></h4>
 * 
 * Prints a string to the scripting console, and issues a line-feed afterwards.
 * Note: due to internal limitations it is not possible to print the string
 * "undefined" using println(). If you expect a variable x to carry the value
 * undefined, use print(x); println(); instead.
 * 
 * <h4><code>println()</code></h4>
 * 
 * Issues a line-feed to the scripting console.
 * 
 * <h4><code>message(string)</code></h4>
 * 
 * Displays a message in a pop-up-dialog.
 * 
 * <h4><code>error(string)</code></h4>
 * 
 * Displays an error-message in a pop-up-dialog.
 * 
 * <h4><code>confirm(string)</code> (= <code>boolean</code>)</h4>
 * 
 * Displays a pop-up-dialog and asks the user to click either "Ok" or "Cancel".
 * If Ok is clicked, the return value will be true.
 * 
 * <h4><code>prompt(string)</code> (= <code>string</code>)</h4>
 * 
 * Displays a pop-up-dialog and asks the user to enter a string.
 * 
 * <h4><code>promptFile(defaultFilename, suffices, saveMode)</code> (= <code>string</code>)</h4>
 * 
 * Asks the user to select a filename or directory.<br>
 * defaultFilename may be undefined.<br>
 * suffices (optional) is a semicolon-seperated string containing possible file suffices to select plus optional textual description (e.g. "jpg JPEG Picture,png Portable Network Graphics,gif,svg Scalable Vector Graphics").
 * If saveMode is <code>true</code> and the user chooses a non-existing file without any of these suffices, the first one will automatically be added.
 * In a special mode, suffices can be set to "&lt;dir&gt;" to indicate the selection of a directory instead of a file.<br>
 * saveMode (boolean, optional) specifies wether the file requester should be opened for loading or for saving a file, <code>false</code> for loading, <code>true</code> for saving.  
 * 
 * <h4><code>formatTime(ticks)</code> (= <code>string</code>)</h4>
 * 
 * Returns the a string representing the number of ticks in a human-readable
 * manner, e.g. "4:02.064".
 * 
 * <h4><code>parseTime(barBeatsTicksString)</code> (= <code>int</code>)</h4>
 * 
 * Returns the number of ticks specified by the formatted string. The string's
 * format is e.g. "4:02.064".
 * 
 * <h4><code>wait(ms)</code></h4>
 * 
 * Waits the specified amount of milliseconds.
 * 
 * <h4><code>waitTicks(ticks)</code></h4>
 * 
 * Waits the specified amount of ticks.
 * 
 * <h4><code>type(string)</code> (= <code>int</code>)</h4>
 * 
 * Returns the type number for a corresponding type name, 1 for "Midi", 2 for
 * "Audio", 4 for "Text".
 * 
 * <h4><code>typeName(int)</code> (= <code>string</code>)</h4>
 * 
 * Returns the type name of a type number, "Midi" for 1, "Audio" for 2, "Text"
 * for 4.
 * 
 * <h4><code>note(string)</code> (= <code>int</code>)</h4>
 * 
 * Returns the number of a note as derived from the specified name.
 * Example names are: c3, f#5, Gb2, D3
 * 
 * <h4><code>noteName(int)</code> (= <code>string</code>)</h4>
 * 
 * Returns the name of a note.
 * 
 * <h4><code>fileRead(filename)</code> (= <code>string</code>)</h4>
 * 
 * Reads a whole file as string. If the file doesn't exist something goes wrong reading, the result is "undefined". 
 * 
 * <h4><code>fileWrite(filename, string) (= <code>string</code>)</code></h4>
 * 
 * Writes a whole string to a file. If the file previously exists, it will be overwritten.<br>
 * Returns <code>true</code> if writing was successful, otherwise <code>false</code>.
 * 
 * <h4><code>fileLen(filename) (= <code>int</code>)</code></h4>
 * 
 * Returns the length of a file in bytes.
 * 
 * <h4><code>fileExists(filename) (= <code>boolean</code>)</code></h4>
 * 
 * Tests whether a file exists.
 * 
 * <h4><code>fileDelete(filename) (= <code>boolean</code>)</code></h4>
 * 
 * Deletes a file.<br>
 * Returns <code>true</code> if deleting was successful, otherwise <code>false</code>.
 * 
 * <h4><code>shellExecute(cmd[, fork]) (= <code>int</code>)</code></h4>
 * 
 * Executes a shell command. If <code>fork</code> remains unspecified or is false, this waits until
 * the onvoked application exits and the corresponding return-code is delivered as result.
 * If <code>fork</code> is true, this returns immediately after spawning the shell-command  and returns 0.
 * 
 * <h4><code>panic()</code></h4>
 * 
 * Send full range of notes-off events to all channels on all devices.
 * 
 * 
 * 
 * <h2>Objects</h2>
 * 
 * An interface to Frinika's currently loaded project is given some top-level
 * objects.
 * 
 * 
 * <h3>Song</h3>
 * 
 * The <code>song</code> object represents the current project from which the
 * script is run (called "song", although not every piece of acoustic material
 * created with a Frinika will of course necessarily be song). Type values are:
 * MIDI = 1, AUDIO = 2, TEXT = 4.
 * 
 * 
 * <h4>Object Variables</h4>
 * 
 * <h4><code>song.filename</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.beatsPerMinute</code></h4>
 * 
 * <h4><code>song.ticksPerBeat <emp>(read-only)</emp></code></h4>
 * 
 * <h4><code>song.lanes[]</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].name</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].type</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[]</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].name</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].type</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].events[]</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].notes[]</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].controllers[]</code> <emp>(read-only)</emp></h4>
 * 
 * <h4><code>song.lanes[x].parts[x].sysex[]</code> <emp>(read-only)</emp></h4>
 *
 * <h4><code>song.lanes[x].parts[x].ghost</code> <emp>(read-only)</emp></h4>
 * 

 * 
 * <h4>Object Functions</h4>
 * 
 * <h4><code>song.play()</code></h4>
 * 
 * 
 * <h4><code>song.playUntil(tick)</code></h4>
 * 
 * 
 * <h4><code>song.stop()</code></h4>
 * 
 * 
 * <h4><code>song.save()</code></h4>
 * 
 * 
 * <h4><code>song.newLane(name, type)</code></h4>
 * 
 * 
 * <h4><code>song.getLane(name) (= lane)</code></h4>
 * 
 * 
 * <h4><code>song.lane[x].remove()</code></h4>
 * 
 * 
 * <h4><code>song.lane[x].newPart(tick, duration)</code></h4>
 * 
 * 
 * <h4><code>song.lane[x].newPartOfType(tick, duration, type)</code></h4>
 * 
 * 
 * <h4><code>song.lane[x].part[y].remove()</code></h4>
 * 
 * 
 * <h4><code>song.createNew()</code></h4>
 *
 *
 * 
 * <h3>Selection</h3>
 * 
 * The <code>selection</code> object represents the current selection inside
 * Frinika's project at the time when the script is run.
 * 
 * <h4>Object Variables</h4>
 * 
 * <h4><code>selection.events[]</code></h4>
 * 
 * Array of all currently selected events. Usually, a script will not use this
 * because it will be interested i events of a certain type only.
 * 
 * <h4><code>selection.notes[]</code></h4>
 * 
 * Array of currently selected Midi notes. If no notes are selected, the array
 * has 0 entries. Typically, a set of selected notes can be changed in a loop
 * like this: <code>
 * for (i = 0; i < selection.notes.length; i++) {
 *   selection.notes[i].note = ...
 *   selection.notes[i].velocity = ...
 *   selection.notes[i].duration = ...
 *   selection.notes[i].startTick = ...
 * }
 * </code>
 * Note that notes from the selection-object may be changed by a script as in
 * the above example (while those accessed via song.lane[x].part[y].note[z] are
 * read-only).
 * 
 * <h4><code>selection.controllers[]</code></h4>
 * 
 * Array of currently selected Midi controllers. If no controllers are selected,
 * the array has 0 entries.
 * 
 * 
 * <h4><code>selection.sysex[]</code></h4>
 * 
 * Array of currently selected Midi system exclusive events. If no system
 * exclusive events are selected, the array has 0 entries.
 * 
 * 
 * <h4>Object Functions</h4>
 * 
 * <h4><code>selection.clear()</code></h4>
 * 
 * Clears the current selections so that no elements remain selected afterwards.
 * 
 * 
 * <h3>Menu</h3>
 * 
 * The <code>menu</code> object represents the menu-bar of Frinika's project-
 * window.
 * 
 * <h4>Object Variables</h4>
 * 
 * <h4><code>menu[menuIndex][itemIndex]</code></h4>
 * 
 * <h4><code>menu[a][b].label</code> <emp>(read-only)</emp></h4>
 * 
 * <h4>Object Functions</h4>
 * 
 * <h4><code>menu[x][y].execute()</code></h4>
 *
 * 
 * <h3>Persistent</h3>
 * 
 * The <code>persistent</code> object allows to store values that will be saved together with the project.
 *
 * <h4>Object Functions</h4>
 * 
 * <h4><code>persistent.put(key, value)</code></h4>
 * 
 * <h4><code>persistent.get(key)</code></h4>
 * 
 * 
 * <h3>Global</h3>
 * 
 * The <code>global</code> object allows to store values that will be saved in the user's homedir as file .frinika-script-global.properties.
 * These values can be shared across project, i.e. one project can set a value, another project can read it.<br>
 * Note that setting a value is a costy operation (will cause file-write with each put-operation).
 *
 * <h4>Object Functions</h4>
 * 
 * <h4><code>global.put(key, value)</code></h4>
 * 
 * <h4><code>global.get(key)</code></h4>
 * 
 * 
 * @author Jens Gulden
 */
public class JavascriptScope extends ScriptableObject {

	public static final int TYPE_UNKNOWN = 1;

	public static final int TYPE_MIDI = 1;

	public static final int TYPE_AUDIO = 2;

	public static final int TYPE_TEXT = 4;

	private static final String TEXT_DELIM = "\n---\n";

	private Context context;

	private ProjectFrame frame;

	private ScriptingDialog dialog;

	private TimeUtils timeUtils;
	
	private Map<Object, Object> wrapperCache;

	/**
	 * Createsa a JavascriptContext. This is a bridge between JavaScript and
	 * Java, application-specific to Frinika.
	 * 
	 * @param frame
	 * @param events
	 */
	public JavascriptScope(Context context, ProjectFrame frame, SortedSet<MultiEvent> events, ScriptingDialog dialog) {
		super();
		this.context = context;
		this.frame = frame;
		this.dialog = dialog;
		ProjectContainer p = frame.getProjectContainer();
		timeUtils = new TimeUtils(p);
		wrapperCache = new HashMap<Object, Object>();
		
		song = new Song(p);
		selection = new Selection(events);
		initMenu();
		this.persistent = new PropertiesWrapper( p.getScriptingEngine().getPersistentProperties() );
		this.global = new PropertiesWrapper() {
			@Override
			public void set(String variable, String value) {
				FrinikaScriptingEngine.globalPut(variable, value);
			}
			@Override
			public String get(String variable) {
				return FrinikaScriptingEngine.globalGet(variable);
			}
		};

		// init Javascript standard objects
		context.initStandardObjects(this);

		// init Frinka-specific objects
		exportField("song", this.song);
		exportField("selection", this.selection);
		exportField("menu", this.menu);
		exportField("persistent", this.persistent);
		exportField("global", this.global);
		exportField("MIDI", TYPE_MIDI);
		exportField("AUDIO", TYPE_AUDIO);
		exportField("TEXT", TYPE_TEXT);

		exportMethod("print", new Class[] { String.class });
		exportMethod("println", new Class[] { String.class });
		// exportMethod( "println", new Class[] { } );
		exportMethod("message", new Class[] { String.class });
		exportMethod("error", new Class[] { String.class });
		exportMethod("confirm", new Class[] { String.class });
		exportMethod("prompt", new Class[] { String.class });
		exportMethod("promptFile", new Class[] { String.class, String.class, boolean.class });
		exportMethod("time", new Class[] { String.class });
		exportMethod("formatTime", new Class[] { int.class });
		exportMethod("_wait", new Class[] { int.class });
		exportMethod("waitTicks", new Class[] { int.class });
		exportMethod("type", new Class[] { String.class });
		exportMethod("typeName", new Class[] { int.class });
		exportMethod("note", new Class[] { String.class });
		exportMethod("noteName", new Class[] { int.class });
		exportMethod("fileRead", new Class[] { String.class });
		exportMethod("fileWrite", new Class[] { String.class, String.class });
		exportMethod("fileLen", new Class[] { String.class });
		exportMethod("fileExists", new Class[] { String.class });
		exportMethod("fileDelete", new Class[] { String.class });
		exportMethod("panic", new Class[] { });
		exportMethod("shellExecute", new Class[] { String.class, boolean.class });
	}

	@Override
	public String getClassName() {
		return "JavascriptScope";
	}

	/**
	 * Exports a Java object as a Javascript variable.
	 * 
	 * @param fieldName
	 * @param value
	 */
	private void exportField(String fieldName, Object value) {
		Object variableJS = Context.javaToJS(value, this);
		ScriptableObject.putProperty(this, fieldName, variableJS);
	}

	/**
	 * Exports a Java method as a Javascript (top-level-)function. Only works
	 * with methods in this class (because scope is used as value for 'this'
	 * when invoking methods).
	 * 
	 * @param methodName
	 * @param parameterSignature
	 */
	private void exportMethod(String methodName, Class[] parameterSignature) {
		try {
			Method m = JavascriptScope.class.getMethod(methodName,
					parameterSignature);
			while (methodName.charAt(0) == '_') {
				methodName = methodName.substring(1);
			} // remove leading _ for JS name
			FunctionObject functionJS = new FunctionObject(methodName, m, this);
			ScriptableObject.putProperty(this, methodName, functionJS);
		} catch (NoSuchMethodException nse) {
			nse.printStackTrace();
		}
	}

	// --- fields exposed to JavaScript ---

	public Object song;

	public Object selection;

	public Object[][] menu;
	
	public Object persistent;

	public Object global;

	// --- methods exposed to JavaScript ---

	public void print(String s) {
		System.out.print(s);
		if (dialog != null) {
			dialog.print(s);
		}
	}

	public void println(String s) {
		if (s.equals("undefined"))
			s = ""; // hack to allow 'null' input - makes of course String
					// "undefined" unprintable, use "undefined " etc. instead
		System.out.println(s);
		if (dialog != null) {
			dialog.println(s);
		}
	}

	public void message(String s) {
		System.out.println(s);
		if (frame != null) {
			frame.message(s);
		}
	}

	public void error(String s) {
		System.err.println(s);
		if (frame != null) {
			frame.error(s);
		}
	}

	public boolean confirm(String s) {
		System.out.print(s);
		if (frame != null) {
			System.out.println("... Ok.");
			return frame.confirm(s);
		} else {
			System.out.println("... Cancel.");
			return false;
		}
	}

	public String prompt(String s) {
		System.out.print(s);
		String r;
		if (frame != null) {
			r = frame.prompt(s);
		} else {
			r = null;
		}
		System.out.println(" " + r);
		return r;
	}

	public String promptFile(String defaultFilename, String suffices, boolean saveMode) {
		System.out.print("prompting for " + (saveMode ? "saving" : "loading") + ", default " + defaultFilename + ":");
		String r;
		if (frame != null) {
			String[][] s = null;
			boolean directoryMode = false;
			if (suffices != null) {
				directoryMode = suffices.equals("<dir>");
				if ( ! directoryMode ) {
					StringTokenizer st = new StringTokenizer(suffices, ";", false);
					int count = st.countTokens();
					s = new String[count][2];
					for (int i = 0; i < s.length; i++) {
						String suf = st.nextToken();
						int space = suf.indexOf(' ');
						if (space == -1) {
							s[i][0] = suf;
							s[i][1] = "";
						} else {
							s[i][0] = suf.substring(0, space);
							s[i][1] = suf.substring(space+1);
						}
					}
				}
			} else {
				s = new String[0][0];
			}
			r = frame.promptFile(defaultFilename, s, saveMode, directoryMode);
		} else {
			r = null;
		}
		System.out.println(" " + r);
		return r;
	}
	
	public int time(String s) { // parses a time string
		if ( s.indexOf('.') == -1 ) { // allow missing "0 bars"
			s = "0." + s;
		}
		if ( s.indexOf(':') == -1 ) { // allow missing "0 ticks"
			s = s + ":000";
		}
		return (int)timeUtils.barBeatTickToTick(s); // int, long not supported by Rhino
	}

	public String formatTime(int ticks) { // int, long not supported by Rhino
		return timeUtils.tickToBarBeatTick(ticks);
	}

	public void _wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			// nop
		}
	}

	public void waitTicks(int ticks) {
		long ms = tick2ms(ticks);
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			// nop
		}
	}

	private int tick2ms(int tick) {
		float bpm = ((Song) song).getBeatsPerMinute();
		int ppq = ((Song) song).getTicksPerBeat();
		int ms = Math.round(((float) tick / (float) ppq / (float) bpm)
				* (60 * 1000));
		return ms;
	}

	public int type(String name) {
		if (name.equalsIgnoreCase("Midi")) {
			return TYPE_MIDI;
		} else if (name.equalsIgnoreCase("Audio")) {
			return TYPE_AUDIO;
		} else if (name.equalsIgnoreCase("Text")) {
			return TYPE_TEXT;
		} else {
			return TYPE_UNKNOWN;
		}
	}

	public String typeName(int type) {
		switch (type) {
		case TYPE_MIDI:
			return "Midi";
		case TYPE_AUDIO:
			return "Audio";
		case TYPE_TEXT:
			return "Text";
		default:
			return null;
		}
	}

	public int note(String name) {
		return MidiStepRecordAction.parseNote(name);
	}

	public String noteName(int note) {
		return MidiStepRecordAction.formatNote(note);
	}

	public String fileRead(String filename) {
		try {
			FileReader in = new FileReader(filename);
			StringBuffer sb = new StringBuffer();
			char[] c = new char[1024];
			int hasRead;
			do {
				hasRead = in.read(c);
				if (hasRead > 0) {
					sb.append(c, 0, hasRead);
				}
			} while (hasRead == c.length);
			in.close();
			return sb.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}
	
	public boolean fileWrite(String filename, String data) {
		try {
			FileWriter out = new FileWriter(filename);
			out.write(data);
			out.close();
			return true;
		} catch (IOException ioe) {
			return false;
		}
	}
	
	public int fileLen(String filename) {
		File file = new File(filename);
		return (int)file.length();
	}
	
	public boolean fileExists(String filename) {
		File file = new File(filename);
		return file.exists();
	}
	
	public boolean fileDelete(String filename) {
		File file = new File(filename);
		return file.delete();
	}
	
	/*public int shellExecute(String command) {
		
	}*/

	public void panic() {
		frame.getProjectContainer().getSequencer().panic();
	}
	
	public int shellExecute(String cmd, boolean fork) {
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			if (fork) {
				return 0;
			} else {
				return p.waitFor();
			}
		} catch (Exception e) {
			frame.error(e);
			return -1;
		}
	}
	
	// --- inner classes (for exposed variables "selection" etc.) ---

	public class Song {

		private ProjectContainer p;

		Song(ProjectContainer p) {
			this.p = p;
		}

		public Object[] getSystemLanes() {
			return (new Converter(p.getProjectLane().getFamilyLanes()) {
				protected Object createWrapper(Object o) {
					return new Lane((com.frinika.sequencer.model.Lane) o);
				}
			}).toArray();
		}

		public Object[] getLanes() {
			return (new Converter(p.getProjectLane().getChildren()) {
				protected Object createWrapper(Object o) {
					return new Lane((com.frinika.sequencer.model.Lane) o);
				}
			}).toArray();
		}

		public Object[] getMidiLanes() {
			return (new Converter(p.getLanes(), MidiLane.class) {
				protected Object createWrapper(Object o) {
					return new Lane((com.frinika.sequencer.model.Lane) o);
				}
			}).toArray();
		}

		public Object[] getAudioLanes() {
			return (new Converter(p.getLanes(), AudioLane.class) {
				protected Object createWrapper(Object o) {
					return new Lane((com.frinika.sequencer.model.Lane) o);
				}
			}).toArray();
		}

		public Object[] getTextLanes() {
			return (new Converter(p.getLanes(), TextLane.class) {
				protected Object createWrapper(Object o) {
					return new Lane((com.frinika.sequencer.model.Lane) o);
				}
			}).toArray();
		}

		public String getFilename() {
			File file = p.getProjectFile();
			if ((file != null) && (file.isFile())) {
				return file.getAbsolutePath();
			} else {
				return null;
			}
		}

		public int getTicksPerBeat() {
			return p.getSequence().getResolution();
		}

		public float getBeatsPerMinute() {
			return p.getSequencer().getTempoInBPM();
		}
		
		public void setPosition(int tick) {
			frame.getProjectContainer().getSequencer().setTickPosition(tick);
		}
		
		public int getPosition() {
			return (int)frame.getProjectContainer().getSequencer().getTickPosition();
		}

		public void play() {
			(new StartAction(frame)).actionPerformed(null);
			/*FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
			if ( ! sequencer.isRunning() ) {
				sequencer.start();
			}*/
		}

		public void playUntil(final int tick) {
			FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
			long current = sequencer.getTickPosition();
			if (tick <= current) return;
			
			//final boolean playing = true;
			final Object lock = new Object();
			SongPositionListener spl = new SongPositionListener() {
				public void notifyTickPosition(long t) {
					FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
					if (t >= tick) {
						sequencer.stop();
						synchronized (lock) {
							lock.notify();
						}
						//sequencer.removeSongPositionListener(this); // would lead to ConcurrentModificationException, so see below
					}
				}
				public boolean requiresNotificationOnEachTick() {
					return true; // but we're not costy
				}
			};
			
			sequencer.addSongPositionListener(spl);
			
			play();
			
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException ie) {
				// nop
			}
			
			synchronized (sequencer) { // sync. because sequencer might otherwise still iterate over listeners, leading to CocurrentModExc
				sequencer.removeSongPositionListener(spl);
			}
		}

		public void stop() {
			(new StopAction(frame)).actionPerformed(null);
			//FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
			//sequencer.stop();
		}

		public void rewind() {
			(new RewindAction(frame)).actionPerformed(null);
			//FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
			//sequencer.setTickPosition(0);
		}

		public void record() {
			(new RecordAction(frame)).actionPerformed(null);
			//FrinikaSequencer sequencer = frame.getProjectContainer().getSequencer();
			//sequencer.startRecording();
		}

		public void save() {
			File file = p.getProjectFile();
			if (file != null) {
				try {
					p.saveProject(file);
				} catch (Throwable t) {
					frame.error(t);
				}
			} else {
				// nop (script must test whether filename valid)
			}
		}

		public void saveAs(String filename) {
			File file = new File(filename);
			if ((file != null) && (file.isFile())) {
				try {
					p.saveProject(file); // itentionally does not set
											// lastSaved... value of project
											// like manual saving
				} catch (Throwable t) {
					frame.error(t);
				}
			} else {
				frame.error("Invalid filename for saving '" + filename + "'.");
			}
		}

		public void open(String filename) {
			// opens a new project, but we don't have a reference to it, so no
			// further automatic scripting from here on the new project
			try {
				new ProjectFrame(ProjectContainer
						.loadProject(new File(filename)));
			} catch (Throwable t) {
				frame.error(t);
			}
		}

		public void createNew() {
			// new project, but same restrictions as with open apply
			try {
				new ProjectFrame(new ProjectContainer());
			} catch (Exception e) {
				frame.error(e);
			}
		}

		public Object newLane(String name, int type) {
			com.frinika.sequencer.model.Lane lane;
			ProjectContainer project = frame.getProjectContainer();
			switch (type) {
			case TYPE_MIDI:
				//(new CreateMidiLaneAction(frame)).actionPerformed(null);
				project.getEditHistoryContainer().mark(getMessage("sequencer.project.add_midi_lane"));
				lane = project.createMidiLane();
				break;
			case TYPE_AUDIO:
				//(new CreateAudioLaneAction(frame)).actionPerformed(null);
				project.getEditHistoryContainer().mark(getMessage("sequencer.project.add_audio_lane"));
				lane = project.createAudioLane();
				break;
			case TYPE_TEXT:
				//(new CreateTextLaneAction(frame)).actionPerformed(null);
				project.getEditHistoryContainer().mark(getMessage("sequencer.project.add_text_lane"));
				lane = project.createTextLane();
				break;
			default: frame.error("cannot create new lane, unknown type " + type);
				return null;
			}
			lane.setName(name);
			project.getEditHistoryContainer().notifyEditHistoryListeners();
			return convert(lane, new Lane(lane));
		}
		
		public Object getLane(String name) {
			Object[] lanes = getSystemLanes();
			for (int i = 0; i < lanes.length; i++) {
				if ( name.equals( ((Lane)lanes[i]).getName() ) ) {
					return lanes[i];
				}
			}
			return null;
		}

	}

	public class Lane {

		private com.frinika.sequencer.model.Lane l;

		private int type;

		Lane(com.frinika.sequencer.model.Lane l) {
			this.l = l;
			if (l instanceof MidiLane) {
				type = TYPE_MIDI;
			} else if (l instanceof AudioLane) {
				type = TYPE_AUDIO;
			} else if (l instanceof TextLane) {
				type = TYPE_TEXT;
			} else {
				type = TYPE_UNKNOWN;
			}
		}

		public int getType() {
			return type;
		}

		public int getIndex() {
			return frame.getProjectContainer().getLanes().indexOf(l);
		}

		public String getName() {
			return l.getName();
		}

		public void setName(String name) {
			l.setName(name);
		}

		public Object[] getParts() {
			return (new Converter(l.getParts()) {
				protected Object createWrapper(Object o) {
					return new Part((com.frinika.sequencer.model.Part) o,
							Lane.this);
				}
			}).toArray();
		}

		public Object newPart(int startTick, int duration) {
			return newPartOfType(startTick, duration, this.type);
		}
		
		public Object newPartOfType(int startTick, int duration, int type) {
			ProjectContainer project = frame.getProjectContainer();

			if (typeName(type) == null) { // type-value may be left out
				type = this.getType();
			}

			com.frinika.sequencer.model.Part part;
			if (type == TYPE_MIDI) {
				part = new MidiPart();
			} else if (type == TYPE_AUDIO) {
				part = new AudioPart();
			} else if (type == TYPE_TEXT) {
				part = new TextPart((TextLane) l);
			} else {
				frame.error("cannot create new part, unknown type " + type);
				return null;
			}

			project.getEditHistoryContainer().mark(getMessage("sequencer.lane.add_part"));

			part.setStartTick(startTick);
			part.setEndTick(startTick + duration);
			l.add(part);

			project.getEditHistoryContainer().notifyEditHistoryListeners();
			
			return convert(part, new Part(part, (Lane)convert(l, new Lane(l))));
		}

		public Object getPart(int startTick) {
			Object[] parts = getParts();
			for (int i = 0; i < parts.length; i++) {
				if ( startTick == ((Part)parts[i]).getStartTick() ) {
					return parts[i];
				}
			}
			return null;
		}

		public void remove() {
			//l.setSelected(true);
			frame.getProjectContainer().getLaneSelection().setSelected(l);
			(new DeleteAction(frame.getProjectContainer())).actionPerformed(null);
		}

		// MidiLanes

		public int getMidiChannel() {
			if (type == TYPE_MIDI) {
				return ((MidiLane) l).getMidiChannel();
			} else {
				return -1;
			}
		}

		/*
		 * public void setMidiChannel(int channel) { if (type == TYPE_MIDI)
		 * ((MidiLane)l).setMidiChannel(channel); } }
		 */

		public void setMute(boolean b) {
			if (type == TYPE_MIDI) {
				((MidiLane) l).setMute(b);
			}
		}

		public boolean isMute() {
			if (type == TYPE_MIDI) {
				return ((MidiLane) l).isMute();
			} else {
				return false;
			}
		}

		public void setSolo(boolean b) {
			if (type == TYPE_MIDI) {
				((MidiLane) l).setSolo(b);
			}
		}

		public boolean isSolo() {
			if (type == TYPE_MIDI) {
				return ((MidiLane) l).isSolo();
			} else {
				return false;
			}
		}

		public void setLooped(boolean b) {
			if (type == TYPE_MIDI) {
				((MidiLane) l).getPlayOptions().looped = b;
			}
		}

		public boolean isLooped() {
			if (type == TYPE_MIDI) {
				return ((MidiLane) l).getPlayOptions().looped;
			} else {
				return false;
			}
		}

		public void setRecording(boolean b) {
			if (type == TYPE_MIDI) {
				((MidiLane) l).setRecording(b);
			}
		}

		public boolean isRecording() {
			if (type == TYPE_MIDI) {
				return ((MidiLane) l).isRecording();
			} else {
				return false;
			}
		}

		// TextLane

		public String getText(String delim) {
			if (type == TYPE_TEXT) {
				return ((TextLane) l).getAllText(delim);
			} else {
				return null;
			}
		}

		public String getText() {
			return getText(TEXT_DELIM);

		}

		public void setText(String text, String delim) {
			if (type == TYPE_TEXT) {
				((TextLane) l).setAllText(text, delim);
			}
		}

		public void setText(String text) {
			if (type == TYPE_TEXT) {
				((TextLane) l).setAllText(text, TEXT_DELIM);
			}
		}
	}
	

	public class Part {

		private com.frinika.sequencer.model.Part p;

		private int type;

		Part(com.frinika.sequencer.model.Part p, Lane parent) {
			this.p = p;
			this.lane = parent;
			if (p instanceof MidiPart) {
				type = TYPE_MIDI;
			} else if (p instanceof AudioPart) {
				type = TYPE_AUDIO;
			} else if (p instanceof TextPart) {
				type = TYPE_TEXT;
			} else {
				type = TYPE_UNKNOWN;
			}
		}

		public Object lane;

		public int getType() {
			return type;
		}

		public boolean isGhost() {
			return (p instanceof Ghost);
		}

		public void remove() {
			//p.setSelected(true);
			frame.getProjectContainer().getPartSelection().setSelected(p);
			(new DeleteAction(frame.getProjectContainer())).actionPerformed(null);
		}
		
		public int getStartTick() {
			return (int)p.getStartTick();
		}

		public void setStartTick(int tick) {
			p.setStartTick(tick);
		}

		public int getEndTick() {
			return (int)p.getEndTick();
		}

		public void setEndTick(int tick) {
			p.setEndTick(tick);
		}
		
		public int getDuration() {
			return (int)p.getDurationInTicks();
		}

		public void setDuration(int tick) {
			//p.setDuration(tick);
			setEndTick( getStartTick() + tick );
		}


		// MidiPart

		public Object[] getNotes() {
			if (type == TYPE_MIDI) {
				return (new Converter(((MidiPart) p).getMultiEvents(),
						NoteEvent.class)).toArray();
			} else {
				return null;
			}
		}
		
		public Object[] getControllers() {
			if (type == TYPE_MIDI) {
				return (new Converter(((MidiPart) p).getMultiEvents(),
						ControllerEvent.class)).toArray();
			} else {
				return null;
			}
		}

		public void insertNote(int note, int tick, int duration, int velocity) {
			if (type == TYPE_MIDI) {
				ProjectContainer project = frame.getProjectContainer();
				project.getEditHistoryContainer().mark(getMessage("sequencer.pianoroll.add_note"));

				NoteEvent newNote = new NoteEvent((MidiPart) p, tick, note, velocity, ((MidiPart) p).getMidiChannel(), duration);
				((MidiPart) p).add(newNote);

				project.getEditHistoryContainer().notifyEditHistoryListeners();
				// project.notifyDragEventListeners(newNote);
				// PianoRoll..repaintItems(); - general problem: repainting
			}
		}

		public void removeNote(int note, int tick) { // TODO test
			if (type == TYPE_MIDI) {
				ProjectContainer project = frame.getProjectContainer();
				project.getEditHistoryContainer().mark(
						getMessage("sequencer.pianoroll.add_note"));

				NoteEvent delNote = findNote(tick, note, (MidiPart) p);
				if (delNote != null) {
					((MidiPart) p).remove(delNote);
				}

				project.getEditHistoryContainer().notifyEditHistoryListeners();
			}
		}

		public int getMidiChannel() {
			if (type == TYPE_MIDI) {
				return ((MidiPart) p).getMidiChannel();
			} else {
				return -1;
			}
		}

		// TextPart

		public String getText() {
			if (type == TYPE_TEXT) {
				return ((TextPart) p).getText();
			} else {
				return null;
			}
		}

		public void setText(String text) {
			if (type == TYPE_TEXT) {
				((TextPart) p).setText(text);
			}
		}

	}

	public class Selection {

		public int start; // read-only

		public int end; // read-only

		public int duration; // read-only

		public Object part; // read-only

		public Object lane; // read-only

		public Object[] notes;

		public Object[] controllers;

		public Object[] sysex;

		Selection(SortedSet<MultiEvent> p) {

			if ((p != null) && (!p.isEmpty())) {
				MultiEvent first = p.first();
				MultiEvent last = p.last();
				start = (int)first.getStartTick();
				end = (int)last.getEndTick();
				duration = end - start;
				// lane = new
				// Lane(((com.frinika.sequencer.model.Part)part).getLane());
				lane = new Lane(first.getPart().getLane());
				part = new Part(first.getPart(), (Lane) lane);
			} else { // no events selected, but maybe at least a part?
						// (multiple part selection not supprted)
				Collection<com.frinika.sequencer.model.Part> partSelection = frame
						.getProjectContainer().getPartSelection().getSelected();
				if (!partSelection.isEmpty()) {
					com.frinika.sequencer.model.Part prt = partSelection
							.iterator().next();
					start = (int)prt.getStartTick();
					end = (int)prt.getEndTick();
					duration = (int)prt.getDurationInTicks();
					lane = new Lane(prt.getLane());
					part = new Part(prt, (Lane) lane);
				} else { // no part selected either, at least a lane?
					Collection<com.frinika.sequencer.model.Lane> laneSelection = frame
							.getProjectContainer().getLaneSelection()
							.getSelected();
					if (!laneSelection.isEmpty()) {
						com.frinika.sequencer.model.Lane ln = laneSelection
								.iterator().next();
						start = 0;
						end = (int)ln.rightTickForMove();
						duration = end - start;
						part = null;
						lane = new Lane(ln);
					}
				}
			}

			ArrayList n = new ArrayList();
			ArrayList c = new ArrayList();
			ArrayList s = new ArrayList();

			if (p != null) {
				for (MultiEvent e : p) {
					if (e instanceof NoteEvent) {
						n.add(e);
					} else if (e instanceof ControllerEvent) {
						c.add(e);
					} else if (e instanceof SysexEvent) {
						s.add(e);
					}
				}
			}

			notes = new Object[n.size()];
			n.toArray(notes);
			controllers = new Object[c.size()];
			c.toArray(controllers);
			sysex = new Object[s.size()];
			s.toArray(sysex);

		}

	}

	public class Menu {

		private JMenuItem item;

		private JMenuItem menu;

		Menu(JMenuItem item, JMenu menu) {
			this.item = item;
			this.menu = menu;
		}

		public String getLabel() {
			return item.getText();
		}

		public String getMenuLabel() {
			return menu.getText();
		}

		public void execute() {
			item.doClick();
		}

	}
	
	public class PropertiesWrapper {
		
		private Properties p;
		
		PropertiesWrapper() {
			this(null);
		}
		
		PropertiesWrapper(Properties properties) {
			this.p = properties;
		}
		
		public void set(String variable, String value) {
			p.put(variable, value);
		}
		
		public String get(String variable) {
			return p.getProperty(variable);
		}
	}

	protected void initMenu() {
		JMenuBar menuBar = frame.getJMenuBar();
		Object[][] o = new Object[menuBar.getMenuCount()][];
		for (int i = 0; i < o.length; i++) {
			final JMenu menu = menuBar.getMenu(i);
			// o[i] = new Object[menu.getMenuComponentCount()];
			// for (int j = 0; j < o[i].length; j++) {
			List<JMenuItem> m = new ArrayList<JMenuItem>();
			for (int j = 0; j < menu.getMenuComponentCount(); j++) {
				Component component = menu.getMenuComponent(j);
				if (component instanceof JMenuItem) {
					// JMenuItem menuItem = (JMenuItem)component;
					// o[i][j] = new Menu(menuItem);
					m.add((JMenuItem) component);
				}
			}
			o[i] = (new Converter(m) {
				protected Object createWrapper(Object javaObject) {
					return new Menu((JMenuItem) javaObject, menu);
				}
			}).toArray();
		}
		this.menu = o;
	}

	// --- tools ---

	private class Converter {

		private Collection c;

		Converter(Collection c, Class filterType) {
			super();
			if (filterType != null) {
				this.c = new ArrayList();
				for (Object o : c) {
					if (filterType.isAssignableFrom(o.getClass())) {
						this.c.add(o);
					}
				}
			} else {
				this.c = c;
			}
		}

		Converter(Collection c) {
			this(c, null);
		}

		Object[] toArray() {
			Object[] l = new Object[c.size()];
			int i = 0;
			for (Object q : c) {
				l[i++] = getWrapper(q); // new Lane(midilane);
			}
			return l;
		}
		
		private Object getWrapper(Object javaObject) {
			Object wrapper = wrapperCache.get(javaObject); // preserve 1:1 idendity between origial objects and wrapper, also faster
			if (wrapper == null) {
				wrapper = createWrapper(javaObject);
				if (wrapper != javaObject) {
					wrapperCache.put(javaObject, wrapper);
				}
			}
			return wrapper;
		}

		protected Object createWrapper(Object javaObject) {
			return javaObject; // by default don't wrap, ususally overwritten
								// by subclasses
		}
	}
	
	private Object convert(Object javaObject, Object newWrapper) {
		Object wrapper = wrapperCache.get(javaObject);
		if (wrapper == null) {
			wrapper = newWrapper;
			wrapperCache.put(javaObject, wrapper);
		}
		return wrapper;
	}

	/*
	 * private class LaneConverter extends Converter {
	 * 
	 * LaneConverter(Collection c) { super(c); }
	 * 
	 * LaneConverter(Collection c, Class filterType) { super(c, filterType); }
	 * 
	 * protected Object createWrapper(Object o) { return new
	 * Lane((com.frinika.sequencer.model.Lane)o); }
	 *  }
	 */

	private static NoteEvent findNote(long tick, int note, MidiPart p) {
		Collection<MultiEvent> eventsAtTick = p.getMultiEventSubset(tick,
				tick + 1);
		if ((eventsAtTick != null) && (!eventsAtTick.isEmpty())) {
			for (MultiEvent e : eventsAtTick) {
				if (e instanceof NoteEvent) {
					NoteEvent n = (NoteEvent) e;
					if (n.getNote() == note) {
						return n;
					}
				}
			}
		}
		return null;
	}
}
