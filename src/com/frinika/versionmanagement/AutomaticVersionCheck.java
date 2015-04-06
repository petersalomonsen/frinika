/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.versionmanagement;

import com.frinika.VersionProperties;
import com.frinika.global.FrinikaConfig;
import com.frinika.project.gui.ProjectFrame;
import com.lightminds.appletservice.AppletServiceClient;
import com.lightminds.appletservice.AppletServiceClientProxy;
import java.awt.BorderLayout;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import static com.frinika.localization.CurrentLocale.getMessage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 *
 * @author Peter Johan Salomonsen
 */
public class AutomaticVersionCheck {
    // Only check once per frinika session
    static boolean checkedNewVersion = false;

    public static void checkLatestVersion(final ProjectFrame pf, final JCheckBoxMenuItem automaticCheckNewVersionMenuItem)
    {
        new Thread() {
            // Since we're accessing the network, we shouldn't block Frinika if the network is not accessible
            public void run() {
                        if(FrinikaConfig.getAutomaticCheckForNewVersion() && !checkedNewVersion)
                        {
                            try {
                                AppletServiceClient.init("http://frinika.appspot.com/");

                                FrinikaVersionManager fvm = (FrinikaVersionManager)AppletServiceClientProxy.newInstance(FrinikaVersionManager.class);
                                String versionParts[] = VersionProperties.getVersion().split("\\.");
                                int versionNumeric = Integer.parseInt(versionParts[0])  * 1000000+
                                                    Integer.parseInt(versionParts[1]) * 10000 +
                                                    (versionParts.length>2 ?
                                                        Integer.parseInt(versionParts[2]) * 100 : 0) +
                                                    (versionParts.length>3 ?
                                                        Integer.parseInt(versionParts[3]) * 1 :0);

                                JPanel newVersionDialogPanel = new JPanel();
                                newVersionDialogPanel.setLayout(new BorderLayout());
                                newVersionDialogPanel.add(new JLabel(getMessage("automaticversioncheck.newversionexists")),BorderLayout.NORTH);

                                final JCheckBox automaticCheckNewVersionCheckBox = new JCheckBox(getMessage("automaticversioncheck.dontshow"),false);

                                newVersionDialogPanel.add(automaticCheckNewVersionCheckBox,BorderLayout.SOUTH);
                                
                                if(versionNumeric<fvm.getLatestFrinikaVersion())
                                    JOptionPane.showMessageDialog(pf, newVersionDialogPanel );
                                else
                                    Logger.getLogger(AutomaticVersionCheck.class.getName()).info("Checked for new Frinika version - no new versions exist");

                                checkedNewVersion = true;
                                FrinikaConfig.setAutomatickCheckForNewVersion(!automaticCheckNewVersionCheckBox.isSelected());
                                automaticCheckNewVersionMenuItem.setState(!automaticCheckNewVersionCheckBox.isSelected());
                            } catch (Exception ex) {
                                Logger.getLogger(AutomaticVersionCheck.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

            }
        }.start();
    }
}
