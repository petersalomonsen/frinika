/*
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
package com.frinika.main.panel;

import com.frinika.gui.util.BareBonesBrowserLaunch;
import com.frinika.gui.util.WindowUtils;
import com.frinika.project.dialog.VersionProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

/**
 * Panel for about dialog.
 *
 * @author hajdam
 */
public class AboutPanel extends javax.swing.JPanel {

    private WindowUtils.OkCancelListener okCancelListener;

    public static final char COPYRIGHT_SYMBOL = (char) 169; // The (C) Symbol

    public static final String MAIN_TITLE
            = "<html><center>"
            + "<b>Frinika DEV version " + VersionProperties.getVersion() + " </b><br>"
            + "<a href=\"http://frinika.sourceforge.net\">http://frinika.sourceforge.net</a><br><font color='#A0A0A0'><i>Build date: " + VersionProperties.getBuildDate() + "</i></font>"
            + "</html>";

    public static final String COPYRIGHT_NOTICE
            = "<html><font size=\"3\"><center>"
            + "Copyright " + COPYRIGHT_SYMBOL + " " + VersionProperties.getCopyrightStart() + "-" + VersionProperties.getCopyrightEnd() + " The Frinika developers. All rights reserved<br>"
            + "This software is licensed under the GNU General Public License (GPL) version 2<br>"
            + "<a href=\"http://www.gnu.org/licenses/gpl.html\">http://www.gnu.org/licenses/gpl.html</a>"
            + "</center></font></html>";

    public static final String CREDITS
            = "<html>"
            + "<h2>The team behind Frinika:</h2>"
            + "Peter Johan Salomonsen - Initiative, sequencer, audiodriver, soft synths, tracker, maintenance and more<br>"
            + "Jon Aakerstrom - Audiodriver, JACK integration<br>"
            + "P.J. Leonard - Pianoroll, partview, overall GUI and sequence objects design and more<br>"
            + "Karl Helgason - RasmusDSP, flexdock, jmod integration with Frinika and more<br>"
            + "Toni (oc2pus@arcor.de) - Ant build scripts and Linux RPMs<br>"
            + "Steve Taylor - Toot integration<br>"
            + "Jens Gulden - Ghosts parts, Midi Tools menu, step recording, ctrl tools, scripting and more<br>"
            + "<br>"
            + "<b>Libraries:</b><br>"
            + "JJack Copyright " + COPYRIGHT_SYMBOL + " Jens Gulden<br>"
            + "RasmusDSP Copyright " + COPYRIGHT_SYMBOL + " Karl Helgason<br>"
            + "Toot audio foundation - Steve Taylor<br>"
            + "Tritonus Copyright " + COPYRIGHT_SYMBOL + " by Florian Bomers and Matthias Pfisterer<br>"
            + "launch4j - Cross-platform Java executable wrapper - <a href=\"http://launch4j.sourceforge.net\">http://launch4j.sourceforge.net</a><br>"
            + "jgoodies - Look and feel - <a href=\"https://looks.dev.java.net\">https://looks.dev.java.net</a><br>"
            + "flexdock - Floating and dockable windows - <a href=\"https://flexdock.dev.java.net\">https://flexdock.dev.java.net</a><br>"
            + "Java Sound MODules Library - <a href=\"http://jmod.dev.java.net\">http://jmod.dev.java.net</a><br>"
            + "Rhino JavaScript engine - <a href=\"http://www.mozilla.org/rhino/\">http://www.mozilla.org/rhino/</a><br>"
            + "LZMA SDK - <a href=\"http://www.7-zip.org/sdk.html\">http://www.7-zip.org/sdk.html</a><br>"
            + "jVorbisEnc - Zbigniew Sudnik - XIPHOPHORUS, <a href=\"http://www.xiph.org\">http://www.xiph.org</a><br>"
            + "MRJ Adapter - <a href=\"http://homepage.mac.com/sroy/mrjadapter/\">http://homepage.mac.com/sroy/mrjadapter/</a><br>"
            + "JVSTHost - <a href=\"http://github.com/mhroth/jvsthost\">http://github.com/mhroth/jvsthost</a><br>"
            + "<br>"
            + "<b>Other contributors:</b><br>"
            + "Bob Lang - Bezier synth (<a href=\"http://www.cems.uwe.ac.uk/~lrlang/BezierSynth/index.html\">http://www.cems.uwe.ac.uk/~lrlang/BezierSynth/index.html</a>)<br>"
            + "Edward H - GUI decoration patches<br>"
            + "Artur Rataj (arturrataj@gmail.com) - Pianoroll patches<br>"
            + "Thibault Aspe - French locale (<a href=\"http://thibault.aspe.free.fr\">http://thibault.aspe.free.fr</a>)<br>"
            + "<br></html>";

    private static final long serialVersionUID = 1L;

    public AboutPanel() {
        initComponents();
        init();
    }

    private void init() {
        boolean darkMode = WindowUtils.isDarkMode();
        AnimatedLogoPanel animatedLogo = new AnimatedLogoPanel();
        add(animatedLogo, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(darkMode ? Color.BLACK : Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextPane mainTitle = new JTextPane();
        mainTitle.setOpaque(false);
        initializeTextPane(mainTitle, MAIN_TITLE);
        panel.add(mainTitle, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);

        {
            JButton button = new JButton("License");
            button.addActionListener((ActionEvent e) -> {
                showLicense();
            });
            if (WindowUtils.isDarkMode()) {
                button.setOpaque(false);
            }
            buttonsPanel.add(button);
        }

        {
            JButton button = new JButton("Credits");
            button.addActionListener((ActionEvent e) -> {
                showCredits();
            });
            if (WindowUtils.isDarkMode()) {
                button.setOpaque(false);
            }
            buttonsPanel.add(button);
        }

        {
            JButton button = new JButton("System Info");
            button.addActionListener((ActionEvent e) -> {
                showSystemInfo();
            });
            if (WindowUtils.isDarkMode()) {
                button.setOpaque(false);
            }
            buttonsPanel.add(button);
        }

        {
            JButton okButton = new JButton("OK");
            okButton.addActionListener((ActionEvent e) -> {
                okCancelListener.okEvent();
            });
            okButton.setDefaultCapable(true);
            // getRootPane().setDefaultButton(okButton);
            if (WindowUtils.isDarkMode()) {
                okButton.setOpaque(false);
            }
            buttonsPanel.add(okButton);
        }

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel copyrightPanel = new JPanel();
        copyrightPanel.setOpaque(false);

        JTextPane copyrightNotice = new JTextPane();
        copyrightNotice.setOpaque(false);
        initializeTextPane(copyrightNotice, COPYRIGHT_NOTICE);
        copyrightPanel.add(copyrightNotice);

        panel.add(copyrightPanel, BorderLayout.SOUTH);

        panel.setSize(getPreferredSize().width, getPreferredSize().height);

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        panel.registerKeyboardAction((ActionEvent e) -> {
            okCancelListener.cancelEvent();
        },
                stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        add(panel, BorderLayout.CENTER);
    }

    public void showLicense() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextPane licenseAgreement;
        try {
            InputStream is = AboutPanel.class.getResource("/com/frinika/resources/license-gpl2.html").openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (is.available() > 0) {
                bos.write(is.read());
            }

            licenseAgreement = new JTextPane();
            initializeTextPane(licenseAgreement, new String(bos.toByteArray()));
            licenseAgreement.setCaretPosition(0);

            JScrollPane licenseScrollPane = new JScrollPane(licenseAgreement);
            panel.add(licenseScrollPane, BorderLayout.CENTER);
            licenseAgreement.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            panel.setPreferredSize(new Dimension(700, 400));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't find license agreement.. Exiting.");
            System.exit(0);
        }

        JOptionPane.showMessageDialog(this, panel, "License",
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new AboutPanel());
    }

    public void whitening(Container co) {
        boolean darkMode = WindowUtils.isDarkMode();
        Component[] comps = co.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JOptionPane) {
                if (!darkMode) {
                    comp.setBackground(Color.WHITE);
                }
            }
            if (comp instanceof JPanel) {
                if (!darkMode) {
                    comp.setBackground(Color.WHITE);
                }
            }
            if (comp instanceof Container) {
                whitening((Container) comp);
            }
        }
    }

    public void showCredits() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTextPane creditsTextArea = new JTextPane();
        initializeTextPane(creditsTextArea, CREDITS);
        creditsTextArea.setFont(creditsTextArea.getFont().deriveFont(11f).deriveFont(Font.PLAIN));

        panel.add(creditsTextArea);

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.INFORMATION_MESSAGE);

        optionPane.setIcon(new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
        JDialog dialog = optionPane.createDialog(this, "Credits");
        dialog.setBackground(Color.WHITE);
        whitening(dialog);

        dialog.setVisible(true);
    }

    public void showSystemInfo() {
        // Jens:
        Properties p = System.getProperties();
        String[][] ss = new String[p.size()][2];
        int i = 0;
        for (Object o : (new TreeSet(p.keySet()))) {
            String s = (String) o;
            String value = p.getProperty(s);
            ss[i][0] = s;
            ss[i][1] = value;
            i++;
        }
        JTable systemInfo = new JTable(ss, new String[]{"Entry", "Value"});
        systemInfo.setEnabled(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(systemInfo), "System Info",
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
    }

    public void setOkCancelListener(WindowUtils.OkCancelListener okCancelListener) {
        this.okCancelListener = okCancelListener;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public static void initializeTextPane(@Nonnull JTextPane textPane, @Nonnull String text) {
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.addHyperlinkListener((HyperlinkEvent event) -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                BareBonesBrowserLaunch.openDesktopURL(event.getURL().toExternalForm());
            }
        });

        // Set default text color
        textPane.setText(text);

        if (!WindowUtils.isDarkMode()) {
            HTMLDocument doc = (HTMLDocument) textPane.getDocument();
            doc.getStyleSheet().addRule("body { color: #000000; }");
        }
    }
}
