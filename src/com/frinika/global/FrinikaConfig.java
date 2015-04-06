/*
 * Created on Apr 14, 2006
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.global;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.gui.DefaultOptionsBinder;
import com.frinika.gui.util.FontChooser;
import com.frinika.gui.util.PresentationPanel;
import com.frinika.project.gui.ProjectFrame;


/**
 * Global settings to be stored and restored when starting the program.
 *
 * In order to provide compile-time safe access to options,
 * a field of type "Meta" with the prefix "_" is declared for each
 * config option. Example:
 *
 *	public static Font TEXT_LANE_FONT = new Font("Arial",Font.PLAIN, 8);
 *	public static Meta _TEXT_LANE_FONT; // field of type "Meta", value will automatically be set when class FrinikaConfig is loaded
 *
 * Attach a listener, e.g.:
 * 
 *  FrinikaConfig.addConfigListener(new ConfigListener() {
 *    public void configurationChanged(ChangeEvent event) {
 *        if (event.getSource() == FrinikaConfig._TEXT_LANE_FONT) { // test if it's the option we're interested in
 *           // ...etc...
 *        }
 *    }
 *  });
 * 
 * @author Peter Johan Salomonsen
 * @author Jens Gulden
 */
public class FrinikaConfig {
	
// --- global Frinika options here ----------------------------------------------------------	
	
	public static boolean SETUP_DONE = false;
	public static Meta _SETUP_DONE;
	
	public static int TICKS_PER_QUARTER = 128;
	public static Meta _TICKS_PER_QUARTER;
	
	public static int SEQUENCER_PRIORITY = 0;
	public static Meta _SEQUENCER_PRIORITY;
	
	// used by JavaSoundVoiceServer
	public static int AUDIO_BUFFER_LENGTH = 512; // TODO: IS THIS IN MILLISECONDS? OTHERWISE CONVERSION MILLISECONDS (as in gui) <-> AUDIO_BUFFER_LENGTH is needed 
	public static Meta _AUDIO_BUFFER_LENGTH;
	
	public static String MIDIIN_DEVICES_LIST = "";
	public static Meta _MIDIIN_DEVICES_LIST;
	
	public static boolean DIRECT_MONITORING = false;
	public static Meta _DIRECT_MONITORING;
	
	public static boolean MULTIPLEXED_AUDIO = false;
	public static Meta _MULTIPLEXED_AUDIO;
	
	public static boolean BIG_ENDIAN = false;
	public static Meta _BIG_ENDIAN;
	
	public static int sampleRate = 44100; // lowercase spelling for 'historic' reasons
	public static Meta _sampleRate;
	
	public static boolean JACK_AUTO_CONNECT = false;
	public static Meta _JACK_AUTO_CONNECT;

        public static boolean AUTOMATIC_CHECK_FOR_NEW_VERSION = true;
        public static Meta _AUTOMATIC_CHECK_FOR_NEW_VERSION;

	// deprecated I believe PJL
//	public static int OS_LATENCY_MILLIS = 0;
//	public static Meta _OS_LATENCY_MILLIS;
	
	public static boolean MAXIMIZE_WINDOW = false;
	public static Meta _MAXIMIZE_WINDOW;
	
	public static float MOUSE_NUMBER_DRAG_INTENSITY = 2.0f;
	public static Meta _MOUSE_NUMBER_DRAG_INTENSITY;

	public static Font TEXT_LANE_FONT = new Font("Arial",Font.PLAIN, 8);
	public static Meta _TEXT_LANE_FONT;
	
	public static File GROOVE_PATTERN_DIRECTORY = new File(System.getProperty("user.home"), "frinika/groovepatterns/");
	public static Meta _GROOVE_PATTERN_DIRECTORY;
	
	public static File SCRIPTS_DIRECTORY = new File(System.getProperty("user.home"), "frinika/scripts/");
	public static Meta _SCRIPTS_DIRECTORY;

        public static File PATCHNAME_DIRECTORY = new File(System.getProperty("user.home"), "frinika/patchname/");
	public static Meta _PATCHNAME_DIRECTORY;

	
	public static File AUDIO_DIRECTORY = new File(System.getProperty("user.home"), "frinika/audio/");
	public static Meta _AUDIO_DIRECTORY;
	
	public static File SOUNDFONT_DIRECTORY = new File(System.getProperty("user.home"), "frinika/soundfonts/");
	public static Meta _SOUNDFONT_DIRECTORY;
        
    public static File  DEFAULT_SOUNDFONT = new File(System.getProperty("user.home"), "frinika/soundfonts/8MBGMSFX.SF2");
	public static Meta _DEFAULT_SOUNDFONT;
	
	public static String LAST_PROJECT_FILENAME = null;
	public static Meta _LAST_PROJECT_FILENAME;
	
	
// --- gui binding ---
	
	/**
	 * Binds all option fields to GUI elements in the ConfigDialogPanel (or to null, if not used in GUI).
	 * 
	 *  The validity of this binding is verified during application startup
	 *  - ALL public static fields must be named here, none more or less
	 *  - (all must be spelled correctly, of course)
	 *  Otherwise the application refuses to start and gives an error.
	 *  
	 * The fields are associated with concrete instances of GUI elements (e.g. JTextField, JCheckBox, ButtonGroup etc.).
	 * The DefaultOptionsBinder should know how to convert between actual GUI elements and typed field values, otherwise
	 * its refresh()/update() method should be overridden and customized here.
	 * 
	 * @param d
	 * @return
	 */
	private static Map<Meta, Object> bindMap(ConfigDialogPanel d) {
		Map<Meta, Object> m =  new HashMap<Meta, Object>();
		m.put( _AUDIO_BUFFER_LENGTH, 					d.spinnerBufferSize );
		m.put( _DIRECT_MONITORING, 						d.checkboxUseDirectMonitoring );
		m.put( _MULTIPLEXED_AUDIO, 						d.checkboxUseMultiplexedJavasoundServer );
	//	m.put( _OS_LATENCY_MILLIS, 						d.spinnerOutputLatency );
		m.put( _JACK_AUTO_CONNECT, 						d.checkboxAutoconnectJack );
		m.put( _sampleRate, 							d.comboboxSampleRate );
		m.put(_TICKS_PER_QUARTER,                       d.spinnerTicksPerQuarter );
		m.put(_SEQUENCER_PRIORITY,                      d.spinnerSequencerPriority );
		m.put( _BIG_ENDIAN, 							d.checkboxBigEndian );
		m.put( _MAXIMIZE_WINDOW,						d.checkboxOpenMaximizedWindow );
		m.put( _MOUSE_NUMBER_DRAG_INTENSITY,			d.spinnerMouseDragSpeedSpinners );
		m.put( _TEXT_LANE_FONT, 						d.textfieldFontTextLane );
		m.put( _GROOVE_PATTERN_DIRECTORY,				d.textfieldGroovePatternsDirectory );
		m.put( _SCRIPTS_DIRECTORY,						d.textfieldScriptsDirectory );
		m.put( _AUDIO_DIRECTORY,						d.textfieldAudioDirectory );
		m.put( _SOUNDFONT_DIRECTORY,					d.textfieldSoundFontDirectory );
                m.put( _PATCHNAME_DIRECTORY,					d.textfieldPatchNameDirectory );
                m.put( _DEFAULT_SOUNDFONT,                      d.textfieldDefaultSoundFont );
		m.put( _MIDIIN_DEVICES_LIST,						null ); // handled 'manually' by dialog
		m.put( _LAST_PROJECT_FILENAME,				null );
		m.put( _SETUP_DONE,									null );
                m.put( _AUTOMATIC_CHECK_FOR_NEW_VERSION, null);
		return m;
	}

	/**
	 * Bind map for options that are stored in dynamic properties, not as public static fields.
	 * @param d
	 * @return
	 */
	/*private static Object[][] dynamicBindMap(ConfigDialogPanel d) {
		return new Object[][] {
				//{ "javasound.output", 							d.comboboxOutputDevice},
		};
	}*/

// --- end of configurable part ----------------------------------------------

	private static final String CONFIG_FILE_NAME = "FrinikaConfig.xml";
	
	private static final String META_PREFIX = "_";
	
	private static File userFrinikaDir = new File(System.getProperty("user.home"), "frinika");
	
	private static File configFile = new File(userFrinikaDir, CONFIG_FILE_NAME);

	/**
	 * If a field is of tpe java.io.File, this suffix denotes that a directory,
	 * not a real file is required. 
	 */
	private static final String DIRECTORY_SUFFIX = "_DIRECTORY";

	private static Map<String, Field> fieldsByName;
	
	private static Map<String, Field> metafieldsByName;
	
	private static Map<Field, Meta> metasByField;
	
	private static Collection<ConfigListener> listeners;
	
	private static JDialog showingDialog = null;
	
	private static JFrame showingDialogFrame = null;
	
	private static Properties properties; // dynamic storage 
	
	static { // class initializer
		properties = new Properties();
		fieldsByName = new HashMap<String, Field>();
		metafieldsByName = new HashMap<String, Field>();
		metasByField = new HashMap<Field, Meta>();
		listeners = new ArrayList<ConfigListener>();
		Field[] fields = FrinikaConfig.class.getFields();
		for (Field f : fields) {
			if ((f.getModifiers() & Modifier.STATIC) != 0) { // only look at static fields
				String name = f.getName();
				if (f.getType() == Meta.class) {
					if (!name.startsWith(META_PREFIX)) {
						throw new ConfigError("meta-field '"+name+"' does not start with prefix "+ META_PREFIX);
					} else {
						metafieldsByName.put(name, f);
					}
				} else {
					fieldsByName.put(name, f);
				}
			}
		}
		
		// resolve meta-fields
		for (Map.Entry<String, Field> metafieldEntry : metafieldsByName.entrySet()) {
			String metafieldName = metafieldEntry.getKey();
			Field metafield = metafieldEntry.getValue();
			String name = metafieldName.substring(META_PREFIX.length());
			Field field = fieldsByName.get(name);
			if (field == null) {
				throw new ConfigError("no corresponding option field '"+name+"' found for meta-field '"+metafieldName+"'");
			}
			Meta meta = new Meta(field);
			try {
				metafield.set(null, meta);
			} catch (IllegalAccessException iae) {
				throw new ConfigError(iae);
			}
			metasByField.put(field, meta);
		}
		
		if (!userFrinikaDir.exists()) {
			System.out.println(" Creating frinka user settings directory  " + userFrinikaDir);
			if (!userFrinikaDir.mkdir()) {
				System.err.println(" Failed to create " + userFrinikaDir);
			}
		}
		
		try {
			load();
		} catch (FileNotFoundException fnfe) {
			System.out.println("Can't find file " + configFile.getAbsolutePath() + ". It will be created when you quit the program or change configuration options.");
		} catch (Exception e) {
			System.err.println("error loading configuration. defaults will be used where possible.");
			e.printStackTrace();
		}
		
		// verify static bindMap
		// make sure all available options are at least named in the map 
		Map<Meta, Object> m = bindMap(new ConfigDialogPanel(null)); // dummy for verifying
		Collection<Field> boundFields = new ArrayList<Field>();
		/*for (int i = 0; i < m.length; i++) {
			Object[] pair = m[i];
			boundFields.add(findField((String)pair[0]));
		}*/
		for (Map.Entry<Meta, Object> e : m.entrySet()) {
			Meta meta = e.getKey();
			Object component = e.getValue();
			if ((component != null) && (component instanceof JComponent) && (((JComponent)component).getParent() == null)) {
				throw new ConfigError("gui element bound to config option '"+meta.getName()+"' has no parent, type "+component.getClass().getName());
			}
			boundFields.add(meta.getField());
		}
		Collection<Field> allfields = new ArrayList<Field>(fieldsByName.values());
		allfields.removeAll(boundFields);
		int a = boundFields.size();
		int b = fieldsByName.size();
		if ((a != b) || (!allfields.isEmpty())) {
			for (Field f : allfields) {
				System.err.println("unbound field: "+f.getName());
			}
			throw new ConfigError("there are fields which are not bound to gui elements (or to null), see above");
		}
	}
	
	public static void showDialog(ProjectFrame frame) {
		if (showingDialog != null) { // already showing (or initialized and hidden)?
			if (showingDialogFrame == frame) { // for same frame?
				showingDialog.show();
				showingDialog.toFront(); // then just put to front
				return;
			} else { // showing for different frame: close old one first
				showingDialog.dispose();
			}
		}
		showingDialogFrame = frame;
		showingDialog = createDialog(frame);
		showingDialog.show();
	}
	
	protected static JDialog createDialog(ProjectFrame frame) {
		ConfigDialogPanel configDialogPanel = new ConfigDialogPanel(frame);
		Map<Meta, Object> m = bindMap(configDialogPanel);
		Map<Field, Object> map = convertMap(m);
		/*Object[][] m2 = dynamicBindMap(configDialogPanel);
		Map<String, Object> map2 = new HashMap<String, Object>();
		for (int i = 0; i < m2.length; i++) {
			map2.put((String)m2[i][0], m2[i][1]);
		}*/
		//DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, map2, properties);
		DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, properties);
		ConfigDialog d = new ConfigDialog(frame, optionsBinder);
		PresentationPanel presentationPanel = new PresentationPanel(configDialogPanel.tabbedPane);
		d.getContentPane().add(presentationPanel, BorderLayout.CENTER);
		return d;
	}
	
	public static Properties getProperties() {
		return properties;
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	/*public static Map<String, Object> backup() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Field f : fieldsByName.values()) {
			map.put(f.getName(), getFieldValue(f));
		}
		return map;
	}
	
	public static void restore(Map<String, Object> map) {
		for (Map.Entry<String, Object> e : map.entrySet()) {
			String name = e.getKey();
			Object value = e.getValue();
			Field f = findField(name);
		}
	}*/
	
	public static void load() throws IOException {
		InputStream r = new FileInputStream(configFile);
		load(r);
	}
	
	public static boolean store() {
		try {
			OutputStream w = new FileOutputStream(configFile);
			save(w);
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Error while saving configuration file.", "Error while saving.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null);
			return false;
		}
	}
	
	public static void storeAndQuit() {
		if (!store()) {
			int answer = JOptionPane.showConfirmDialog(null, "Exit anyway?", "Error while saving.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null);
			if (answer != JOptionPane.OK_OPTION) {
				return; // don't quit
			}
		}
		System.exit(0);
	}

	public static void load(InputStream r) throws IOException {
		properties = new Properties();
		properties.loadFromXML(r);
		loadFields(properties);
		// remove fields from dynamic properties
		for (String fieldname : fieldsByName.keySet()) {
			properties.remove(fieldname);
		}
	}
	
	public static void save(OutputStream w) throws IOException {
		Properties p = new Properties();
		// copy all dynamic properties into
		for (Object key : properties.keySet()) {
			p.setProperty((String)key, properties.getProperty((String)key));
		}
		SETUP_DONE = true;
		saveFields(p);
		p.storeToXML(w, "Frinika configuration");
	}
	
	public static void loadFields(Properties p) {
		for (Field field : fieldsByName.values()) {
			String name = field.getName();
			String prop = p.getProperty(name);
			if (prop == null) {
				System.out.println("no saved property for configuration option "+name+", using default");
			} else {
				Object o = stringToValue(prop, name, field.getType());
				setFieldValue(field, o);
			}
		}
	}
	
	public static void saveFields(Properties p) {
		for (Field field : fieldsByName.values()) {
			String name = field.getName();
			Object o = getFieldValue(field);
			String s = valueToString(o, name, field.getType());
			if (s != null) {
				p.setProperty(name, s);
			} else {
				// don't save entry at all if null value
			}
		}
	}
	
	public static void addConfigListener(ConfigListener l) {
		listeners.add(l);
	}
	
	public static void removeConfigListener(ConfigListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Fire event if a public static field option has been altered.
	 * No even is fired on changes of dynamic properties-options.
	 * @param field
	 */
	public static void fireConfigurationChangedEvent(Meta meta) {
		ChangeEvent event = new ChangeEvent(meta);
		for (ConfigListener l : listeners) {
			l.configurationChanged(event);
		}
	}
	
	
	private static Field findField(String name) {
		Field field = fieldsByName.get(name);
		if (field == null) { // severe error, should fail here to ensure hard binding
			throw new ConfigError("dynamic bind error: configuration field "+name+" does not exist.");
		}
		return field;
	}
	
	private static Map<Field, Object> convertMap(Object[][] bindMap) {
		Map<Field, Object> m = new HashMap<Field, Object>();
		for (int i = 0; i < bindMap.length; i++) {
			Object[] pair = bindMap[i];
			Field field = findField((String)pair[0]);
			Object component = pair[1];
			m.put(field, component);
		}
		return m;
	}
	
	private static Map<Field, Object> convertMap(Map<Meta, Object> map) {
		Map<Field, Object> m = new HashMap<Field, Object>();
		for (Map.Entry<Meta, Object> e : map.entrySet()) {
			Field field = e.getKey().getField();
			Object component = e.getValue();
			m.put(field, component);
		}
		return m;
	}
	
	public static Object getFieldValue(Field field) {
		try {
			return field.get(null);
		} catch (IllegalAccessException iae) {
			throw new ConfigError("dynamic bind error: IllegalAccessException on getField " + iae.getMessage());
		}
	}
	
	public static void setFieldValue(Field field, Object o) {
		Object oldValue;
		try {
			oldValue = field.get(null);		
		} catch (IllegalAccessException iae) {
			throw new ConfigError("dynamic bind error: IllegalAccessException on iitial get of setField " + iae.getMessage());
		}
		
	//	System.out.println(" set field value "+ field + "   " + o );
		System.out.println("config: " + field.getName() + "=" + o );
		try {
			field.set(null, o);
			// special: make sure directories exist
			if ((o instanceof File) && (field.getName().endsWith(DIRECTORY_SUFFIX))) {
				((File)o).mkdirs();
			}
			if (((o != null) && (!o.equals(oldValue))) || ((o == null) && (oldValue != null))) {
				Meta meta = metasByField.get(field);
				if (meta != null) {
					fireConfigurationChangedEvent(meta);
				}
			}
		} catch (IllegalAccessException iae) {
			throw new ConfigError("dynamic bind error: IllegalAccessException on setField " + iae.getMessage());
		}
	}
	
	public static Object stringToValue(String prop, String name, Class type) {
		if (prop == null) return null;
		
		if (int.class.isAssignableFrom(type)) {
			return Integer.parseInt(prop);
		} else if (long.class.isAssignableFrom(type)) {
			return Long.parseLong(prop);
		} else if (double.class.isAssignableFrom(type)) {
			return Double.parseDouble(prop);
		} else if (float.class.isAssignableFrom(type)) {
			return Float.parseFloat(prop);
		} else if (boolean.class.isAssignableFrom(type)) {
			return Boolean.parseBoolean(prop);
		} else if (File.class.isAssignableFrom(type)) {
			return new File(prop);
		} else if (Font.class.isAssignableFrom(type)) {
			return stringToFont(prop);
		} else {
			return prop;
		}
	}
	
	public static String valueToString(Object o, String name, Class type) {
		if (o == null) return null;
		if (File.class.isAssignableFrom(type)) {
			return ((File)o).getAbsolutePath() ;
		} else if (Font.class.isAssignableFrom(type)) {
				return  fontToString((Font)o);
		} else {
			return  o.toString();
		}
	}
	
	public static boolean isTrue(Object o) {
		if (o instanceof Boolean) {
			return ((Boolean)o).booleanValue();
		} else if (o instanceof Number) {
			return !(Math.abs(((Number)o).doubleValue()) < 0.000000001d);
		} else {
			String s = o.toString();
			s = s.trim().toLowerCase();
			return s.equals("true")||s.equals("yes");
		}
	}

	public static Font stringToFont(String s) {
		StringTokenizer st = new StringTokenizer(s, ",", false);
		String fontName = "Helvetica";
		String fontSizeStr = "12";
		String fontStyleStr = "plain";
		if (st.hasMoreTokens()) {
			fontName = st.nextToken();
			if (st.hasMoreTokens()) {
				fontSizeStr = st.nextToken();
				if (st.hasMoreTokens()) {
					fontStyleStr = st.nextToken();
				}
			}
		}
		int fontSize;
		try {
			fontSize = Integer.parseInt(fontSizeStr);
		} catch (NumberFormatException nfe) {
			fontSize = 12;
		}
		int fontStyle = 0;
		fontStyleStr = fontStyleStr.toLowerCase();
		if (fontStyleStr.indexOf("bold") != -1) {
			fontStyle |= Font.BOLD;
		}
		if (fontStyleStr.indexOf("italic") != -1) {
			fontStyle |= Font.ITALIC;
		}
		return new Font(fontName, fontStyle, fontSize);
	}
	
	public static String fontToString(Font font) {
		int fontStyle = font.getStyle();
		String fontStyleStr = "";
		if ((fontStyle&Font.BOLD) != 0) {
			fontStyleStr += "bold";
		}
		if ((fontStyle&Font.ITALIC) != 0) {
			fontStyleStr += "italic";
		}
		if (fontStyleStr.length() == 0) {
			fontStyleStr = "plain";
		}
		return font.getName()+","+font.getSize()+","+fontStyleStr;
	}
	
	public static String fileToString(File file) {
		String s;
		try {
			s = file.getCanonicalPath();
		} catch (IOException ioe) {
			s = file.getAbsolutePath();
		}
		return s;
	}
	
	public static void pickDirectory(ProjectFrame frame, JTextField boundTextField) {
		pickFile(frame, boundTextField, true);
	}
	
	public static void pickFile(ProjectFrame frame, JTextField boundTextField) {
		pickFile(frame, boundTextField, false);
	}
	
	public static void pickFile(ProjectFrame frame, JTextField boundTextField, boolean directory) {
		String s = boundTextField.getText();
		File file = new File(s);
		JFileChooser fc = new JFileChooser(file);
		if (directory) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		fc.showDialog(frame, "Choose");
		file = fc.getSelectedFile();
		if (file != null) {
			s = fileToString(file);
			boundTextField.setText(s);
		}
	}
	
	public static void pickFont(ProjectFrame frame, JTextField boundTextField) {
		String s = boundTextField.getText();
		Font font = stringToFont(s);
		font = FontChooser.showDialog(frame, "Pick Font...", font);
		if (font != null) {
			s = fontToString(font);
			boundTextField.setText(s);
		}
	}
	
	// --- accessor methods --------------------------------------------------
	
	public static int getAudioBufferLength() { // TODO: direct read-access to field instead
		return AUDIO_BUFFER_LENGTH;
	}
	
	public static void setAudioBufferLength(int len) {
		setFieldValue(findField("AUDIO_BUFFER_LENGTH"), len);
	}

	public static boolean getDirectMonitoring() { // TODO: direct read-access to field instead
		return DIRECT_MONITORING;
	}
	 
	public static void setDirectMonitoring(boolean dm) {
		_DIRECT_MONITORING.set(dm);
	}

	public static void setMultiplexedAudio(boolean multiplex) {
		_MULTIPLEXED_AUDIO.set(multiplex);
	}

	
	public static void setJackAutoconnect(boolean auto) {
		_JACK_AUTO_CONNECT.set(auto);
	}

	public static String lastProjectFile() { // TODO: direct read-access to field instead
		return LAST_PROJECT_FILENAME;
	}
	
	public static void setLastProjectFilename(String filename) {
		setFieldValue(findField("LAST_PROJECT_FILENAME"), filename);
	}

	public static void setMidiInDeviceList(Vector<String> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (String o : list) {
			if (!first)
				buf.append(";");
			buf.append(o.toString());
			first = false;
		}
		System.out.println(buf);
		String s = buf.toString();
		setFieldValue(findField("MIDIIN_DEVICES_LIST"), s);
	}

	public static Vector<String> getMidiInDeviceList() {
		String buf = MIDIIN_DEVICES_LIST;
		if (buf == null) buf = "";
		String[] list = buf.split(";");
		Vector<String> vec = new Vector<String>();
		for (String str : list) {
			if (!str.equals(""))
				vec.add(str);
		}
		return vec;
	}
	
	public static Collection<String> getAvailableMidiInDevices() {
		ArrayList<String> a = new ArrayList<String>();
		Info infos[] = MidiSystem.getMidiDeviceInfo();
		for (Info info : infos) {
			try {
				MidiDevice dev=MidiSystem.getMidiDevice(info);
				if (dev.getMaxTransmitters() == 0 ) continue;
				String str = info.toString();
				a.add(str);
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		return a;
	}

	public static Collection<String> getAvailableAudioDevices() {
		List<String> list = FrinikaAudioSystem.getAudioServer().getAvailableOutputNames();
		return list;
	}

        public static boolean getAutomaticCheckForNewVersion() {
            return AUTOMATIC_CHECK_FOR_NEW_VERSION;
        }

        public static void setAutomatickCheckForNewVersion(boolean automaticCheckForNewVersion)
        {
            AUTOMATIC_CHECK_FOR_NEW_VERSION = automaticCheckForNewVersion;
        }

	/**
	 * Meta-Info on option fields.
	 */
	public static class Meta {
		
		private Field field;
		
		Meta(Field field) {
			this.field = field;
		}
		
		public Field getField() {
			return field;
		}
		
		public String getName() {
			return getField().getName();
		}
		
		public Class getType() {
			return getField().getType();
		}
		
		public Object get() {
			try {
				return getField().get(null);
			} catch (IllegalAccessException iae) {
				throw new ConfigError(iae);
			}
		}
		
		public void set(Object o) {
			FrinikaConfig.setFieldValue(getField(), o);
		}
		
	}
	
}
