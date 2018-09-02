/*
 * Created on Mar 6, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika;

import com.frinika.global.FrinikaConfig;
import com.frinika.global.Toolbox;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.gui.util.SupportedLaf;
import com.frinika.gui.util.WindowUtils;
import com.frinika.main.FrinikaMain;
import com.frinika.project.dialog.VersionProperties;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * The main entry class for Frinika.
 *
 * @author Peter Johan Salomonsen
 */
public class FrinikaApp {

    private static String argProjectFile = null;

    public static void main(String[] args) throws Exception {

        parseArguments(args);

        prepareRunningFromSingleJar();

        loadProperties();

        configureUI();

        FrinikaMain frinikaMain = FrinikaMain.getInstance();
        frinikaMain.startFrinika(argProjectFile);
    }

    public static void configureUI() {
// TODO        String lcOSName = System.getProperty("os.name").toLowerCase();

        String theme = FrinikaGlobalProperties.THEME.getValue();
        SupportedLaf selectedLaf = SupportedLaf.DEFAULT;
        if (theme != null) {
            try {
                selectedLaf = SupportedLaf.valueOf(theme);
            } catch (IllegalArgumentException ex) {
                // do nothing
            }
        }
        WindowUtils.switchLookAndFeel(selectedLaf);
    }

    private static void loadProperties() {
        FrinikaGlobalProperties.initialize();
        try {
            FrinikaConfig.load();
        } catch (IOException ex) {
            Logger.getLogger(FrinikaApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Detects whether running from a single .jar-file (e.g. via "java -jar
     * frinika.jar"). In this case, copy native binary libraries to a
     * file-system accessible location where the JVM can load them from. (There
     * is a comparable mechanism already implemented in
     * com.frinika.priority.Priority, but this here works for all native
     * libraries, esp. libjjack.so.) (Jens)
     */
    public static void prepareRunningFromSingleJar() {
        String classpath = System.getProperty("java.class.path");
        if (!classpath.contains(File.pathSeparator)) { // no pathSeparator: single entry classpath
            if (classpath.endsWith(".jar")) {
                File file = new File(classpath);
                if (file.exists() && file.isFile()) { // yes, running from 1 jar
                    String osArch = System.getProperty("os.arch");
                    String osName = System.getProperty("os.name");
                    String libPrefix = "lib/" + osArch + "/" + osName + "/";
                    String tmp = System.getProperty("java.io.tmpdir");
                    File tmpDirectory = new File(tmp);
                    try {
                        System.out.println("extracting files from " + libPrefix + " to " + tmpDirectory.getAbsolutePath() + ":");
                        Toolbox.extractFromJar(file, libPrefix, tmpDirectory);
                        System.setProperty("java.library.path", tmp);
                    } catch (IOException ioe) {
                        System.err.println("Native library extraction failed. Problems may occur.");
                        Logger.getLogger(FrinikaApp.class.getName()).log(Level.SEVERE, null, ioe);
                    }
                }
            }
        }
    }

    public static void parseArguments(@Nonnull String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                System.out.println("Command usage is java -jar frinika.jar [options]");
                System.out.println("Available options:");
                System.out.println("-h, --help: Display this help message and exit.");
                System.out.println("-v, --version: Display the version number and exit");
                System.out.println("-c, --config [path]: Specifies an alternate file at 'path' to use as a config.");
                System.out.println("\tExample: java -jar frinika.jar -c ~/Documents/Config.xml file.prinika");
                System.exit(0);
            } else if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--version")) {
                System.out.println("Frinika version " + VersionProperties.getVersion() + " (build date " + VersionProperties.getBuildDate() + ")");
                System.exit(0);
            } else if (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--config")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Error: a path must be specified to with the " + arg + " argument.");
                    System.exit(-1);
                }
                String path = args[i];
                FrinikaConfig.setConfigLocation(path);
            } else if (!arg.startsWith("-") && argProjectFile == null) {
                argProjectFile = arg;
            } else {
                System.out.println("Unknown argument " + arg + ", ignoring.");
                System.out.println("For help with command line usage, please see java -jar frinika.jar --help");
            }
        }
    }
}
