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

import com.frinika.base.FrinikaAudioSystem;
import com.frinika.global.property.ConfigurationProperty;
import com.frinika.global.property.CustomGlobalProperty;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.global.property.FrinikaGlobalProperty;
import com.frinika.global.property.RecentProjectRecord;
import com.frinika.gui.util.FontChooser;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

/**
 * Global settings to be stored and restored when starting the program.
 *
 * In order to provide compile-time safe access to options, a field of type
 * "Meta" with the prefix "_" is declared for each config option. Example:
 *
 * <pre>
 *	public static Font TEXT_LANE_FONT = new Font("Arial",Font.PLAIN, 8);
 *	public static Meta _TEXT_LANE_FONT; // field of type "Meta", value will automatically be set when class FrinikaConfig is loaded
 * </pre>
 *
 * Attach a listener, e.g.:
 *
 * <pre>
 *  FrinikaConfig.addConfigListener(new ConfigListener() {
 *    public void configurationChanged(ChangeEvent event) {
 *        if (event.getSource() == FrinikaConfig._TEXT_LANE_FONT) { // test if it's the option we're interested in
 *           // ...etc...
 *        }
 *    }
 *  });
 * </pre>
 *
 * @author Peter Johan Salomonsen
 * @author Jens Gulden
 */
public class FrinikaConfig {

    private static final String CONFIG_FILE_NAME = "FrinikaConfig.xml";
    private static final String EXAMPLES_DIR = "Examples";

    private static File defaultUserFrinikaDir = new File(System.getProperty("user.home"), "frinika");
    private static File configFile = new File(defaultUserFrinikaDir, CONFIG_FILE_NAME);

    /**
     * If a field is of tpe java.io.File, this suffix denotes that a directory,
     * not a real file is required.
     */
    private static final String DIRECTORY_SUFFIX = "_DIRECTORY";

    private static Collection<ConfigListener> listeners;

    private static JDialog showingDialog = null;

    private static ProjectFrameIntf showingDialogFrame = null;

    private static Properties properties; // dynamic storage 

    static { // class initializer
        properties = new Properties();
        listeners = new ArrayList<>();

        try {
            load();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Can't find find config at " + configFile.getAbsolutePath() + ". If you have not specified a custom config, it will be created when you quit the program or change configuration options.");
        } catch (IOException e) {
            System.err.println("error loading configuration. defaults will be used where possible.");
            e.printStackTrace();
        }

//        // verify static bindMap
//        // make sure all available options are at least named in the map 
//        Map<FrinikaGlobalProperty, Object> m = bindMap(new ConfigDialogPanel(null)); // dummy for verifying
//        Collection<Field> boundFields = new ArrayList<>();
//        /*for (int i = 0; i < m.length; i++) {
//			Object[] pair = m[i];
//			boundFields.add(findField((String)pair[0]));
//		}*/
//        for (Map.Entry<FrinikaGlobalProperty, Object> e : m.entrySet()) {
//            FrinikaGlobalProperty meta = e.getKey();
//            Object component = e.getValue();
//            if ((component != null) && (component instanceof JComponent) && (((JComponent) component).getParent() == null)) {
//                throw new ConfigError("gui element bound to config option '" + meta.getName() + "' has no parent, type " + component.getClass().getName());
//            }
//            boundFields.add(meta.getField());
//        }
//        Collection<Field> allfields = new ArrayList<>(fieldsByName.values());
//        allfields.removeAll(boundFields);
//        int a = boundFields.size();
//        int b = fieldsByName.size();
//        if ((a != b) || (!allfields.isEmpty())) {
//            for (Field f : allfields) {
//                System.err.println("unbound field: " + f.getName());
//            }
//            throw new ConfigError("there are fields which are not bound to gui elements (or to null), see above");
//        }
    }

//    public static void showDialog(ProjectFrameIntf frame) {
//        if (showingDialog != null) { // already showing (or initialized and hidden)?
//            if (showingDialogFrame == frame) { // for same frame?
//                showingDialog.show();
//                showingDialog.toFront(); // then just put to front
//                return;
//            } else { // showing for different frame: close old one first
//                showingDialog.dispose();
//            }
//        }
//        showingDialogFrame = frame;
//        showingDialog = createDialog(frame);
//        showingDialog.show();
//    }
//
//    protected static JDialog createDialog(ProjectFrameIntf frame) {
//        ConfigDialogPanel configDialogPanel = new ConfigDialogPanel(frame.getFrame());
//        Map<FrinikaGlobalProperty, Object> m = bindMap(configDialogPanel);
//        Map<Field, Object> map = convertMap(m);
//        /*Object[][] m2 = dynamicBindMap(configDialogPanel);
//		Map<String, Object> map2 = new HashMap<String, Object>();
//		for (int i = 0; i < m2.length; i++) {
//			map2.put((String)m2[i][0], m2[i][1]);
//		}*/
//        //DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, map2, properties);
//        DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, properties);
//        ConfigDialog d = new ConfigDialog(frame, optionsBinder);
//        PresentationPanel presentationPanel = new PresentationPanel(configDialogPanel.tabbedPane);
//        d.getContentPane().add(presentationPanel, BorderLayout.CENTER);
//        return d;
//    }
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
        InputStream inputStream = new FileInputStream(configFile);
        load(inputStream);
    }

    public static boolean store() {
        if (!configFile.getParentFile().exists()) {
            if (!configFile.getParentFile().mkdirs()) {
                System.err.println(" Failed to create " + configFile.getParent());
                return false;
            }
        }

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

    public static void load(@Nonnull InputStream inputStream) throws IOException {
        properties = new Properties();
        properties.loadFromXML(inputStream);
        loadFields(properties);
        // remove fields from dynamic properties
        for (FrinikaGlobalProperty property : FrinikaGlobalProperty.values()) {
            properties.remove(property.getName());
        }
    }

    public static void save(@Nonnull OutputStream outputStream) throws IOException {
        Properties savedProperties = new Properties();
        // copy all dynamic properties into
        properties.keySet().forEach((key) -> {
            savedProperties.setProperty((String) key, properties.getProperty((String) key));
        });
        FrinikaGlobalProperties.SETUP_DONE.setValue(Boolean.TRUE);
        saveFields(savedProperties);
        savedProperties.storeToXML(outputStream, "Frinika configuration");
    }

    public static void loadFields(@Nonnull Properties properties) {
        for (FrinikaGlobalProperty globalProperty : FrinikaGlobalProperty.values()) {
            ConfigurationProperty<Object> property = (ConfigurationProperty<Object>) ConfigurationProperty.findByName(globalProperty.getName());
            if (property == null) {
                throw new IllegalStateException("Missing configuration property for name " + globalProperty.getName());
            }

            if (property instanceof CustomGlobalProperty) {
                ((CustomGlobalProperty) property).load(properties);
            } else {
                String name = property.getName();
                Class<?> type = property.getType();
                String propertyStringValue = properties.getProperty(name);
                if (propertyStringValue == null) {
                    System.out.println("no saved property for configuration option " + name + ", using default");
                } else {
                    Object propertyValue = stringToValue(propertyStringValue, type);
                    property.setValue(propertyValue);
                }
            }
        }
    }

    public static void saveFields(@Nonnull Properties properties) {
        for (FrinikaGlobalProperty globalProperty : FrinikaGlobalProperty.values()) {
            ConfigurationProperty<Object> property = (ConfigurationProperty<Object>) ConfigurationProperty.findByName(globalProperty.getName());
            if (property == null) {
                throw new IllegalStateException("Missing configuration property for name " + globalProperty.getName());
            }

            if (property instanceof CustomGlobalProperty) {
                ((CustomGlobalProperty) property).save(properties);
            } else {
                String name = globalProperty.getName();
                Object propertyValue = property.getValue();
                String propertyStringValue = valueToString(propertyValue, property.getType());
                if (propertyStringValue != null) {
                    properties.setProperty(name, propertyStringValue);
                } else {
                    // don't save entry at all if null value
                }
            }
        }
    }

    public static void addConfigListener(@Nonnull ConfigListener listener) {
        listeners.add(listener);
    }

    public static void removeConfigListener(@Nonnull ConfigListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fire event if a global property has been altered. No even is fired on
     * changes of dynamic properties-options.
     *
     * @param property global property
     */
    public static void fireConfigurationChangedEvent(ConfigurationProperty<?> property) {
        ChangeEvent event = new ChangeEvent(property);
        listeners.forEach((listener) -> {
            listener.configurationChanged(event);
        });
    }

    public static <T> void setGlobalPropertyValue(@Nonnull ConfigurationProperty<T> property, T value) {
        Object oldValue = property.getValue();

        System.out.println("config: " + property.getName() + "=" + value);
        if (value instanceof File) {
            setupGlobalPropertyDirectory((ConfigurationProperty<File>) property, (File) value);
        } else {
            property.setValue(value);
        }

        if (((value != null) && (!value.equals(oldValue))) || ((value == null) && (oldValue != null))) {
            fireConfigurationChangedEvent(property);
        }
    }

    public static void setupGlobalPropertyDirectory(ConfigurationProperty<File> property, File directory) {
        // Make sure directories exist
        if (property.getName().endsWith(DIRECTORY_SUFFIX)) {
            directory.mkdirs();
        }

        property.setValue(directory);
    }

    @Nullable
    public static Object stringToValue(@Nullable String value, @Nonnull Class type) {
        if (value == null) {
            return null;
        }

        if (Integer.class.isAssignableFrom(type)) {
            return Integer.parseInt(value);
        } else if (Long.class.isAssignableFrom(type)) {
            return Long.parseLong(value);
        } else if (Double.class.isAssignableFrom(type)) {
            return Double.parseDouble(value);
        } else if (Float.class.isAssignableFrom(type)) {
            return Float.parseFloat(value);
        } else if (Boolean.class.isAssignableFrom(type)) {
            return Boolean.parseBoolean(value);
        } else if (File.class.isAssignableFrom(type)) {
            return new File(value);
        } else if (Font.class.isAssignableFrom(type)) {
            return stringToFont(value);
        } else {
            return value;
        }
    }

    @Nullable
    public static String valueToString(@Nullable Object value, @Nonnull Class type) {
        if (value == null) {
            return null;
        }
        if (File.class.isAssignableFrom(type)) {
            return ((File) value).getAbsolutePath();
        } else if (Font.class.isAssignableFrom(type)) {
            return fontToString((Font) value);
        } else {
            return value.toString();
        }
    }

    public static boolean isTrue(Object o) {
        if (o instanceof Boolean) {
            return ((Boolean) o);
        } else {
            String s = o.toString();
            s = s.trim().toLowerCase();
            return s.equals("true") || s.equals("yes");
        }
    }

    @Nonnull
    public static Font stringToFont(@Nonnull String fontIdentifier) {
        StringTokenizer st = new StringTokenizer(fontIdentifier, ",", false);
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
        if (fontStyleStr.contains("bold")) {
            fontStyle |= Font.BOLD;
        }
        if (fontStyleStr.contains("italic")) {
            fontStyle |= Font.ITALIC;
        }
        return new Font(fontName, fontStyle, fontSize);
    }

    @Nonnull
    public static String fontToString(@Nonnull Font font) {
        int fontStyle = font.getStyle();
        String fontStyleStr = "";
        if ((fontStyle & Font.BOLD) != 0) {
            fontStyleStr += "bold";
        }
        if ((fontStyle & Font.ITALIC) != 0) {
            fontStyleStr += "italic";
        }
        if (fontStyleStr.length() == 0) {
            fontStyleStr = "plain";
        }
        return font.getName() + "," + font.getSize() + "," + fontStyleStr;
    }

    @Nonnull
    public static String fileToString(@Nonnull File file) {
        String fileName;
        try {
            fileName = file.getCanonicalPath();
        } catch (IOException ioe) {
            fileName = file.getAbsolutePath();
        }
        return fileName;
    }

    public static void pickDirectory(Component frame, JTextField boundTextField) {
        pickFile(frame, boundTextField, true);
    }

    public static void pickFile(Component frame, JTextField boundTextField) {
        pickFile(frame, boundTextField, false);
    }

    public static void pickFile(Component frame, JTextField boundTextField, boolean directory) {
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

    public static void pickFont(Frame frame, JTextField boundTextField) {
        String s = boundTextField.getText();
        Font font = stringToFont(s);
        font = FontChooser.showDialog(frame, "Pick Font...", font);
        if (font != null) {
            s = fontToString(font);
            boundTextField.setText(s);
        }
    }

    // --- accessor methods --------------------------------------------------
    public static void setConfigLocation(String path) {
        setConfigLocation(new File(path));
    }

    public static void setConfigLocation(@Nonnull File file) {
        if (file.exists() && file.isDirectory()) {
            file = new File(file, CONFIG_FILE_NAME);
        } else {
            file.getParentFile().mkdirs();
        }

        if (!file.getParentFile().isDirectory()) {
            System.out.println("Warning: Could not create the directory path '" + file.getParent() + "'. Using default config.");
            return;
        }

        configFile = file;

        try {
            load();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Can't find find config at " + configFile.getAbsolutePath() + ". It will be created when you quit the program or change configuration options.");
        } catch (IOException e) {
            System.err.println("error loading configuration. defaults will be used where possible.");
            e.printStackTrace();
        }
    }

    @Nonnull
    public static Collection<String> getAvailableMidiInDevices() {
        ArrayList<String> availableMidiDevices = new ArrayList<>();
        Info infos[] = MidiSystem.getMidiDeviceInfo();
        for (Info info : infos) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                if (dev.getMaxTransmitters() == 0) {
                    continue;
                }
                String str = info.toString();
                availableMidiDevices.add(str);
            } catch (MidiUnavailableException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }

        }
        return availableMidiDevices;
    }

    @Nullable
    public static Collection<String> getAvailableAudioDevices() {
        List<String> list = FrinikaAudioSystem.getAudioServer().getAvailableOutputNames();
        return list;
    }

    public static void setLastProject(@Nonnull String projectPath, @Nonnull String projectName) {
        String lastProjectPath = FrinikaGlobalProperties.LAST_PROJECT_FILENAME.getValue();
        String lastProjectName = FrinikaGlobalProperties.LAST_PROJECT_NAME.getValue();
        String lastProjectType = FrinikaGlobalProperties.LAST_PROJECT_TYPE.getValue();

        if (lastProjectPath != null) {
            if (lastProjectName == null) {
                lastProjectName = new File(lastProjectPath).getName();
            }
            List<RecentProjectRecord> recentProjects = FrinikaGlobalProperties.RECENT_PROJECTS.getValue();
            if (recentProjects == null) {
                recentProjects = new ArrayList<>();
            }

            for (int i = 0; i < recentProjects.size(); i++) {
                if (projectPath.equals(recentProjects.get(i).getProjectPath())) {
                    recentProjects.remove(i);
                    break;
                }
            }
            if (!projectPath.equals(lastProjectPath)) {
                RecentProjectRecord recentProject = new RecentProjectRecord(lastProjectName, lastProjectPath, lastProjectType);
                recentProjects.add(0, recentProject);
                if (recentProjects.size() > 10) {
                    recentProjects.remove(recentProjects.size() - 1);
                }
                FrinikaGlobalProperties.RECENT_PROJECTS.setValue(recentProjects);
            }
        }
        FrinikaGlobalProperties.LAST_PROJECT_FILENAME.setValue(projectPath);
        FrinikaGlobalProperties.LAST_PROJECT_NAME.setValue(projectName);
    }

    public static void setLastProject(@Nonnull String fileName) {
        File projectFile = new File(fileName);
        setLastProject(fileName, projectFile.getName());
    }

    public static void setLastProject(@Nonnull File file) {
        setLastProject(file.getAbsolutePath(), file.getName());
    }

    @Nonnull
    public static File getExampleFilesPath() {
        return new File(defaultUserFrinikaDir, EXAMPLES_DIR);
    }
}
