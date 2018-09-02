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
package com.frinika.global.property;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Recent filename property value.
 *
 * @author hajdam
 */
@Immutable
public class RecentProjectRecord {

    @Nullable
    private final String projectName;
    @Nullable
    private final String projectFile;
    @Nullable
    private final String projectType;

    public RecentProjectRecord(@Nullable String projectName, @Nullable String projectFile, @Nullable String projectType) {
        this.projectName = projectName;
        this.projectFile = projectFile;
        this.projectType = projectType;
    }

    @Nullable
    public String getProjectName() {
        return projectName;
    }

    @Nullable
    public String getProjectPath() {
        return projectFile;
    }

    @Nullable
    public String getProjectType() {
        return projectType;
    }
}
