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

import org.mozilla.javascript.*;

import com.frinika.global.FrinikaConfig;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.scripting.gui.ScriptingDialog;
import com.frinika.project.scripting.javascript.JavascriptScope;
import com.frinika.sequencer.model.MultiEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Scriptig-engine for Frinika. The static elements of this class handle
 * application-wide script-execution. the instance elements of this class
 * provide a script container which can be (de)serialized along with
 * ProjectContainer. Although the execution of scripts is handled globally,
 * each script runs inside it's associated project's focus, and the set of
 * scripts which are currently loaded and thus available for execution is
 * project-specific.
 * 
 * The architecture allows for deploying multiple scripting languages, but
 * the only language used for scripting now is JavaScript, and there are no
 * plans to add support for any other languages. 
 * The actual interfacing between the 'Frinika world' and the 'JavaScript
 * world' is done by class com.frinika.project.scripting.javascript.JavascriptScope.
 * 
 * @see com.frinika.project.scripting.javascript.JavascriptScope
 * @author Jens Gulden
 */
public class FrinikaScriptingEngine implements ScriptContainer, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final static File GLOBAL_PROPERTIES_FILE = new File(FrinikaConfig.SCRIPTS_DIRECTORY, "scripting.properties");
	
	// --- static ---
	
	transient static Map<FrinikaScript, ScriptThread> runningScripts = new HashMap<FrinikaScript, ScriptThread>();
	transient static Properties global = null;
	transient private static Collection<ScriptListener> scriptListeners = new HashSet<ScriptListener>(); 
	
	public static void executeScript(FrinikaScript script, ProjectFrame frame, ScriptingDialog dialog) {
		ScriptThread thread = new ScriptThread(script, frame, dialog);
		thread.start();
	}
	
	static Object runScript(FrinikaScript script, ProjectFrame frame, ScriptingDialog dialog) { // called from SriptThread
		int language = script.getLanguage();
		String name = script.getName();
		
                if (language != FrinikaScript.LANGUAGE_JAVASCRIPT
                        && language != FrinikaScript.LANGUAGE_GROOVY) {
			System.out.println("cannot execute script " + name +": unsupported language " + language);
			return null;
		}
		
                System.out.println("Executing script '"+name+"'...");
		String source = script.getSource();
		frame.getProjectContainer().getEditHistoryContainer().mark("Script "+name);
		
		Collection<MultiEvent> events = frame.getProjectContainer().getMidiSelection().getSelected();
		SortedSet<MultiEvent> clones = new TreeSet<MultiEvent>();
		
		// work on clones
		if (events != null) {
			for (MultiEvent ev : events) {
				try {
					MultiEvent clone = (MultiEvent) (ev.clone());
					clones.add(clone);
				} catch (CloneNotSupportedException cnse) {
					cnse.printStackTrace();
				}
			}
		}
		
		// do it
                Object result = null;
                switch(script.getLanguage()) {
                    case FrinikaScript.LANGUAGE_JAVASCRIPT:
                        result = executeJavascript(source, name, frame, clones, dialog);
                        break;
                    case FrinikaScript.LANGUAGE_GROOVY:
                        result = executeGroovyScript(source, name, frame, clones, dialog);
                        break;
                }
		
		
		if (events != null) {
			Iterator<MultiEvent> clonesIterator = clones.iterator();
			for (MultiEvent ev : events) {
				ev.getPart().remove(ev);
				ev.restoreFromClone(clonesIterator.next());
				ev.getPart().add(ev);
			}
		}

		if (result != null) {
			System.out.println(result.toString());
		}
		frame.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();
		
		return result;
	}
	
	public static void stopScript(FrinikaScript script) {
		ScriptThread thread = runningScripts.get(script);
		if (thread != null) {
			thread.stop();
		}
	}
	
	protected static Object executeJavascript(String source, String name, ProjectFrame frame, SortedSet<MultiEvent> events, ScriptingDialog dialog) {
        Context cx = Context.enter();
        try {
        	JavascriptScope scope = new JavascriptScope(cx, frame, events, dialog);
            Object result;
            try {
            	result = cx.evaluateString(scope, source, name, 1, null);
            } catch (Throwable t) {
            	if (t instanceof ThreadDeath) {
            		frame.message("Script execution has been aborted.");
            		result = "";
            	} else {
                	frame.error(t);
                	result = null;
            	}
            }

            if (result != null) {
            	return cx.toString(result);
            } else {
            	return null;
            }

        } finally {
            Context.exit();
        }
		
	}
	
        protected static Object executeGroovyScript(String source, String name, ProjectFrame frame, SortedSet<MultiEvent> events, final ScriptingDialog dialog) {
            ScriptEngineManager factory = new ScriptEngineManager();	                    
            ScriptEngine engine = factory.getEngineByName("groovy");
        
            Writer writer = new Writer() {

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    dialog.print(new String(cbuf,off,len));
                }

                @Override
                public void flush() throws IOException {                    
                }

                @Override
                public void close() throws IOException {                    
                }
            };
            
            engine.put("projectFrame", frame);        
            engine.getContext().setWriter(writer);
            engine.getContext().setErrorWriter(writer);            
            try {                        
                return engine.eval(source);
            } catch (ScriptException ex) {
                frame.message("Script execution has been aborted.");
                Logger.getLogger(FrinikaScriptingEngine.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
	}
	
	/**
	 * Adds a ScriptListener. This method is static, as scripts can potentially affect the whole running system,
	 * across projects, so it doesn't make sense to restrict references to scripts to individual projects.
	 * 
	 * @param l
	 */
	public static void addScriptListener(ScriptListener l) {
		scriptListeners.add(l);
	}
	
	public static void removeScriptListener(ScriptListener l) {
		scriptListeners.remove(l);
	}
	
	/**
	 * 
	 * @param script
	 * @param returnValue if ==this, then 'script has started', else 'script as exited'
	 */
	protected static void notifyScriptListeners(FrinikaScript script, Object returnValue) {
		for (ScriptListener l : scriptListeners) {
			if (returnValue == script) { // start
				l.scriptStarted(script);
			} else {
				l.scriptExited(script, returnValue); // returnValue may be null to indicate failure
			}
		}
	}
	
	public static void globalPut(String variable, String value) {
		if (global == null) {
			loadGlobalProperties();
		}
		if (global == null) {
			global = new Properties();
		}
		synchronized (global) {
			global.put(variable, value);
			try {
				OutputStream out = new FileOutputStream(GLOBAL_PROPERTIES_FILE);
				global. store(out, "Frinika Scripting - Global Properties");
				out.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public static String globalGet(String variable) {
		if (global == null) {
			loadGlobalProperties();
		}
		if (global != null) {
			return global.getProperty(variable);
		} else {
			return null;
		}
	}
	
	private static void loadGlobalProperties() {
		try {
			Properties p = new Properties();
			InputStream in = new FileInputStream(GLOBAL_PROPERTIES_FILE);
			p.load(in);
			in.close();
			global = p;
		} catch (IOException ioe) {
			// nop, remains null
		}
	}
	
	// --- instance members --------------------------------------------------
	
	
	protected Collection<FrinikaScript> scripts;	
	
	protected ProjectContainer project;
	
	protected Properties persistent = null; // init on first get (usual case will remain null)
	
	/**
	 * Constructor. One instance per ProjectContainer (1:1).
	 * 
	 * @param project
	 */
	public FrinikaScriptingEngine(ProjectContainer project) {
		this.project = project;
		scripts = new ArrayList<FrinikaScript>();
	}
	
	public Collection<FrinikaScript> getScripts() {
		return scripts;
	}
	
	public void addScript(FrinikaScript script) {
		if ( ! scripts.contains(script) ) {
			scripts.add(script);
		}
	}
	
	public void removeScript(FrinikaScript script) {
		scripts.remove(script);
	}
	
	public Properties getPersistentProperties() {
		if (persistent == null) {
			persistent = new Properties();
		}
		return persistent;
	}
	
	public FrinikaScript loadScript(File file) throws IOException {
		String source = loadString(file);
		DefaultFrinikaScript script = new DefaultFrinikaScript();
                if(file.getName().endsWith("groovy")) {
                    script.setLanguage(FrinikaScript.LANGUAGE_GROOVY);
                }
                else {
                    script.setLanguage(FrinikaScript.LANGUAGE_JAVASCRIPT);
                }
		script.setSource(source);
		String name = file.getAbsolutePath();
		script.setFilename(name);
		this.addScript(script);
		return script;
	}
	
	public void saveScript(FrinikaScript script, File file) throws IOException {
		saveString(script.getSource(), file);
		if (script instanceof DefaultFrinikaScript) {
			((DefaultFrinikaScript)script).setFilename(file.getAbsolutePath());
		}
	}
	
	public static String loadString(File file) throws IOException { // TODO move to global utilities-class
		long len = file.length();
		char[] c = new char[(int)len];
		FileReader f = new FileReader(file);
		f.read(c);
		f.close();
		return new String(c);
	}
	
	public static void saveString(String s, File file) throws IOException { // TODO move to global utilities-class
		FileWriter f = new FileWriter(file);
		f.write(s);
		f.close();
	}
	
	/*private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
	}*/
	
	private void writeObject(ObjectOutputStream out) throws ClassNotFoundException, IOException {
		// remove all from open scripts which are not serializable (i.e. Preset-Scripts)
		Collection<FrinikaScript> c = new ArrayList<FrinikaScript>( scripts ); 
		for (FrinikaScript script : scripts) {
			if ( ! (script instanceof Serializable) ) {
				c.remove(script);
			}
		}
		Collection<FrinikaScript> backup = scripts;
		scripts = c;
		
		out.defaultWriteObject();
		
		scripts = backup;
	}

	
}