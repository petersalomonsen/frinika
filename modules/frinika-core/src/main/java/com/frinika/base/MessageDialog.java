package com.frinika.base;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Extracted from Project Container.
 *
 * @author hajdam
 */
public class MessageDialog {

    public static void message(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, "Frinika Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String prompt(Component parentComponent, String message, String initialValue) {
        // Jens
        if (initialValue == null) {
            initialValue = "";
        }
        String result = JOptionPane.showInputDialog(parentComponent, message, initialValue);
        return result;
    }

    public static String prompt(Component parentComponent, String message) {
        // Jens
        return prompt(parentComponent, message, null);
    }

    public static String promptFile(Component parentComponent, String defaultFilename, String[][] suffices, boolean saveMode, boolean directoryMode) {
        // Jens
        JFileChooser fc = new JFileChooser();
        if (!directoryMode) {
            final boolean save = saveMode;
            // final String[][] suff = suffices;
            if (suffices != null) {
                for (String[] suffice : suffices) {
                    final String suffix = suffice[0];
                    final String description = suffice[1];
                    // if (suffix == null) suffix = "*";
                    // if (description == null) description = "";
                    FileFilter ff = new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if (file.isDirectory()) {
                                return true;
                            }
                            String name = file.getName();
                            return suffix.equals("*") || name.endsWith("." + suffix) || (save && fileDoesntExistAndDoesntEndWithAnySuffix(file));
                        }

                        @Override
                        public String getDescription() {
                            return "." + suffix + " - " + description;
                        }
                    };
                    fc.addChoosableFileFilter(ff);
                }
            }
        } else {
            // directory mode
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int r;
        if (defaultFilename != null) {
            File file = new File(defaultFilename);
            fc.setSelectedFile(file);
        }
        if (saveMode) {
            r = fc.showSaveDialog(null);
        } else {
            r = fc.showOpenDialog(null);
        }
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String name = file.getName();
            String extraSuffix = "";
            if (name.indexOf('.') == -1) {
                // no suffix entered
                if (suffices != null && suffices.length > 0) {
                    extraSuffix = "." + suffices[0][0]; // use first one as default
                }
            }
            String filename = file.getAbsolutePath() + extraSuffix;
            if (saveMode) {
                File fl = new File(filename);
                if (fl.exists()) {
                    if (!confirm(parentComponent, "File " + filename + " already exists. Overwrite?")) {
                        return null;
                    }
                }
            }
            return filename;
        } else {
            return null;
        }
    }

    public static String promptFile(Component parentComponent, String defaultFilename, String[][] suffices, boolean saveMode) {
        return promptFile(parentComponent, defaultFilename, suffices, saveMode, false);
    }

    public static String promptFile(Component parentComponent, String defaultFilename, String[][] suffices) {
        return promptFile(parentComponent, defaultFilename, suffices, false);
    }

    public static boolean confirm(Component parentComponent, String message) {
        // Jens
        int result = JOptionPane.showConfirmDialog(parentComponent, message, "Frinika Question", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    private static boolean fileDoesntExistAndDoesntEndWithAnySuffix(File file) {
        if (file.exists()) {
            return false;
        }
        String name = file.getName();
        return (name.indexOf('.') == -1);
    }

    public static void error(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, "Frinika Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component parentComponent, Throwable ex) {
        JOptionPane.showMessageDialog(parentComponent, ex, "Frinika Error", JOptionPane.ERROR_MESSAGE);
    }
}
