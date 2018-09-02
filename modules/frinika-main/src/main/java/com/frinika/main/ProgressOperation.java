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
package com.frinika.main;

import com.frinika.gui.util.WindowUtils;
import com.frinika.main.panel.ProgressPanel;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.tools.ProgressInputStream;
import com.frinika.tools.ProgressObserver;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Progress operation.
 *
 * @author hajdam
 */
public interface ProgressOperation {

    void run(@Nonnull ProgressPanel panel);

    public static void openProjectFile(@Nonnull FrinikaFrame projectFrame, @Nonnull File projectFile) throws Exception {
        showProgressDialog(projectFrame, (ProgressPanel progressPanel) -> {
            ProgressObserver progressObserver = progressPanel.getProgressObserver();
            try {
                progressObserver.setGoal(projectFile.length());
                FrinikaProjectContainer project = FrinikaProjectContainer.loadProject(projectFile, progressObserver);
                projectFrame.setProject(project);
                progressObserver.finished();
            } catch (Exception ex) {
                Logger.getLogger(ProgressOperation.class.getName()).log(Level.SEVERE, null, ex);
                progressObserver.fail(ex);
            }
        });
    }

    public static void downloadSampleFile(@Nonnull FrinikaFrame frame, @Nonnull File targetFile, @Nonnull String downloadUrlString) {
        try {
            URL downloadUrl = new URL(downloadUrlString);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setInstanceFollowRedirects(true);
            long length = 0;
            do {
                int status = connection.getResponseCode();
                boolean redirect = status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER;

                if (!redirect) {
                    length = connection.getContentLengthLong();
                    break;
                }

                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setInstanceFollowRedirects(true);
            } while (true);

            final InputStream inputStream = connection.getInputStream();
            final long downloadLength = length;
            showProgressDialog(frame, (ProgressPanel progressPanel) -> {
                progressPanel.setActionTitle("Download example project");
                progressPanel.setActionText("Downloading...");

                ProgressObserver progressObserver = progressPanel.getProgressObserver();
                try {
                    ProgressInputStream progressInputStream = new ProgressInputStream(progressObserver, inputStream);
                    progressObserver.setGoal(downloadLength);
                    ReadableByteChannel rbc = Channels.newChannel(progressInputStream);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    progressObserver.finished();
                } catch (IOException ex) {
                    Logger.getLogger(ProgressOperation.class.getName()).log(Level.SEVERE, null, ex);
                    progressObserver.fail(ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ProgressOperation.class.getName()).log(Level.SEVERE, null, ex);
            // TODO
        }
    }

    public static void showProgressDialog(@Nonnull FrinikaFrame projectFrame, @Nonnull ProgressOperation operation) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();

        final JDialog progressDialog = new JDialog(projectFrame, true);
        progressDialog.setUndecorated(true);
        progressDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ProgressPanel progressPanel = new ProgressPanel();
        progressDialog.add(progressPanel);
        progressDialog.pack();

        progressPanel.setCloseListener(() -> {
            progressDialog.dispatchEvent(new WindowEvent(progressDialog, WindowEvent.WINDOW_CLOSING));
        });

        int screenWidth = allDevices[0].getDefaultConfiguration().getBounds().width;
        progressDialog.setSize(screenWidth / 2, progressDialog.getHeight());
        WindowUtils.setWindowCenterPosition(progressDialog);

        progressDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                new Thread(() -> {
                    operation.run(progressPanel);
                }).start();
            }
        });

        try {
            SwingUtilities.invokeLater(() -> {
                progressDialog.setVisible(true);
            });
        } catch (RuntimeException ex) {
            Logger.getLogger(ProgressOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
