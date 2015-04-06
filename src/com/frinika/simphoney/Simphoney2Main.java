/*
 * Created on 23-Feb-2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
package com.frinika.simphoney;

import com.frinika.simphoney.Simphoney2Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;
import net.roydesign.app.Application;

import com.frinika.About;
import com.frinika.SplashDialog;
import com.frinika.WelcomeDialog;
import com.frinika.project.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;
import com.frinika.global.Toolbox;
import com.frinika.tootX.midi.MidiInDeviceManager;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFocusListener;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.menu.CreateProjectAction;
import com.frinika.sequencer.gui.menu.OpenProjectAction;
import com.frinika.settings.SetupDialog;
//import com.frinika.toot.SwitchedAudioClient;

import static com.frinika.localization.CurrentLocale.getMessage;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

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

/**
 * The main entry class for Simphoney
 * 
 * Adapted from FrinikaMain
 * 
 * @author Peter Johan Salomonsen PJ Leonard
 */
public class Simphoney2Main {

	static FrinikaExitHandler exitHook = null;

	private static ProjectFrame project;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

           
		prepareRunningFromSingleJar();

		configureUI();

		try {
			int n = 1;

			Object[] options = { getMessage("welcome.new_project"),
					getMessage("welcome.open_existing"),
					getMessage("welcome.settings"), getMessage("welcome.quit") };

			// String setup = FrinikaConfig.getProperty("multiplexed_audio");

			WelcomeDialog welcome = new WelcomeDialog(options);

			// if (setup == null) {
			if (!FrinikaConfig.SETUP_DONE) {
				// welcome = new WelcomeDialog(options);
				welcome.setModal(false);
				welcome.setVisible(true);
				SetupDialog.showSettingsModal();
				welcome.setVisible(false);
			}

			welcome.setModal(true);

			welcome.addButtonActionListener(2, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SetupDialog.showSettingsModal();
				}
			});

			welcome.setVisible(true);

			n = welcome.getSelectedOption();

			switch (n) {
			case -1:
				System.exit(0);
				break;
			case 0:
				// new ProjectFrame(new ProjectContainer());
				SplashDialog.showSplash();
				CreateProjectAction act = new CreateProjectAction();
				act.actionPerformed(null);
				project = act.getProjectFrame();// .getProjectContainer();
				break;
			case 1:
				SplashDialog.showSplash();
				String lastFile = FrinikaConfig.lastProjectFile();
				if (lastFile != null)
					OpenProjectAction.setSelectedFile(new File(lastFile));
				OpenProjectAction oact = new OpenProjectAction();
				oact.actionPerformed(null);
				project = oact.getProjectFrame();// .getProjectContainer();
				break;
			case 3:
				System.exit(0);
				break;

			default:
				assert (false);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); // new ProjectFrame(new ProjectContainer());
		}

		// project.setBounds(screenPos());
		
		exitHook = new FrinikaExitHandler();
		Runtime.getRuntime().addShutdownHook(exitHook);

		ProjectFrame.addProjectFocusListener(new ProjectFocusListener() {

			public void projectFocusNotify(ProjectContainer project) {
				FrinikaAudioSystem.installClient(project.getAudioClient());
			}

		});

		SplashDialog.closeSplash();

		FrinikaAudioSystem.getAudioServer().start();

		// EditHistoryListener listener=new MusicListener(project,new
		// WhiteNoteMusicProcess());

		new Simphoney2Frame(project);
		
		
		
		
	//	project.perspectivePreset3();

	}

	static Rectangle screenPos() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Rectangle windowSize;
		Insets windowInsets;

		GraphicsEnvironment ge = java.awt.GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice()
				.getDefaultConfiguration();
//		if (gc == null)
//			gc = JFrame.getGraphicsConfiguration();

		if (gc != null) {
			windowSize = gc.getBounds();
			windowInsets = toolkit.getScreenInsets(gc);
		} else {
			windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
			windowInsets = new java.awt.Insets(0, 0, 0, 0);
		}

		// PJL using xrandr and a virtual screen 2560x1024 then windowSize.x
		// == windowSize.width=1280;

		windowSize.x = 0;

		int w = (windowSize.width - windowSize.x)
				- (windowInsets.left + windowInsets.right + 10);
		int h = (windowSize.height - windowSize.y)
				- (windowInsets.top + windowInsets.bottom + 10);
		int y = windowInsets.top
				+ ((windowSize.height - windowSize.y)
						- (windowInsets.top + windowInsets.bottom) - h) / 2;
		int x = windowInsets.left
				+ ((windowSize.width - windowSize.x)
						- (windowInsets.left + windowInsets.right) - w) / 2;

		return new Rectangle(x+w/2,y,w/2,(int)(h*.6));
	}

	private static boolean ismac = false;

	public static boolean isMac() {
		return ismac;
	}

	private static void configureUI_MacOS() {
		try {

			ismac = true;

			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"Frinika");

			Object cMenuBarUI = UIManager.get("MenuBarUI");
			Object cMenuItemUI = UIManager.get("MenuItemUI");
			Object cMenuUI = UIManager.get("MenuUI");
			Object cCheckBoxMenuItemUI = UIManager.get("CheckBoxMenuItemUI");
			Object cRadioButtonMenuItemUI = UIManager
					.get("RadioButtonMenuItemUI");
			Object cPopupMenuUI = UIManager.get("PopupMenuUI");
			Object cProgressBarUI = UIManager.get("ProgressBarUI");

			UIManager.setLookAndFeel(new PlasticLookAndFeel());

			UIManager.put("MenuBarUI", cMenuBarUI);
			UIManager.put("MenuItemUI", cMenuItemUI);
			UIManager.put("MenuUI", cMenuUI);
			UIManager.put("CheckBoxMenuItemUI", cCheckBoxMenuItemUI);
			UIManager.put("RadioButtonMenuItemUI", cRadioButtonMenuItemUI);
			UIManager.put("PopupMenuUI", cPopupMenuUI);
			UIManager.put("ProgressBarUI", cProgressBarUI);

			Application.getInstance().getAboutJMenuItem().addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							About.about(null);
						}
					});

			Application.getInstance().getQuitJMenuItem().addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							ProjectFrame.getFocusFrame().tryQuit();
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void configureUI() {

		String lcOSName = System.getProperty("os.name").toLowerCase();
		boolean MAC_OS_X = lcOSName.startsWith("mac os x");
		if (MAC_OS_X) {
			configureUI_MacOS();
			return;
		}

		try {
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static class FrinikaExitHandler extends Thread {
		public void run() {
			MidiInDeviceManager.close();
			FrinikaAudioSystem.close();
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// System.out.println(" Closing ALL midi devices ");
			// ProjectContainer.closeAllMidiOutDevices();
			// }
			// });
		}
	}

	/**
	 * Detect whether running from a single .jar-file (e.g. via "java -jar
	 * frinika.jar"). In this case, copy native binary libraries to a
	 * file-system accessible location where the JVM can load them from. (There
	 * is a comparable mechanism already implemented in
	 * com.frinika.priority.Priority, but this here works for all native
	 * libraries, esp. libjjack.so.) (Jens)
	 */
	public static void prepareRunningFromSingleJar() {
		String classpath = System.getProperty("java.class.path");
		if (classpath.indexOf(File.pathSeparator) == -1) { // no pathSeparator:
															// single entry
															// classpath
			if (classpath.endsWith(".jar")) {
				File file = new File(classpath);
				if (file.exists() && file.isFile()) { // yes, running from 1
														// jar
					String osarch = System.getProperty("os.arch");
					String osname = System.getProperty("os.name");
					String libPrefix = "lib/" + osarch + "/" + osname + "/";
					String tmp = System.getProperty("java.io.tmpdir");
					File tmpdir = new File(tmp);
					try {
						System.out.println("extracting files from " + libPrefix
								+ " to " + tmpdir.getAbsolutePath() + ":");
						Toolbox.extractFromJar(file, libPrefix, tmpdir);
						System.setProperty("java.library.path", tmp);
					} catch (IOException ioe) {
						System.err
								.println("Native library extraction failed. Problems may occur.");
						ioe.printStackTrace();
					}
				}
			}
		}
	}
}
