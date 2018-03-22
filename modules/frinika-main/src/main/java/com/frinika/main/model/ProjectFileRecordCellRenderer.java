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
package com.frinika.main.model;

import com.frinika.main.FrinikaFrame;
import java.awt.Component;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;

/**
 * Cell renderer for project files.
 *
 * @author hajdam
 */
public class ProjectFileRecordCellRenderer extends JLabel implements ListCellRenderer<ProjectFileRecord> {

    @Override
    public Component getListCellRendererComponent(@Nonnull JList<? extends ProjectFileRecord> list, @Nonnull ProjectFileRecord value, int index, boolean isSelected, boolean cellHasFocus) {
        setOpaque(true);

        ImageIcon imageIcon = FrinikaFrame.getIconResource("frinika-project.png");
        setIcon(imageIcon);

        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        setBackground(isSelected ? defaults.getColor("List.selectionBackground") : defaults.getColor("List.background"));
        String projectName = value.getProjectName();
        String filePath = value.getFilePath();
        setText("<html>" + projectName + "<br><small>" + filePath + "</small></html>");

        return this;
    }
}
