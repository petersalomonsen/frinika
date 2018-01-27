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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration property for recent project.
 *
 * @author hajdam
 */
public class RecentProjectsProperty extends ConfigurationProperty<List<RecentProjectRecord>> implements CustomGlobalProperty {

    public RecentProjectsProperty(@Nonnull FrinikaGlobalProperty globalProperty, @Nullable List<RecentProjectRecord> value) {
        super(generify(List.class), globalProperty, value);
    }

    // TODO conversion to/from XML
    @SuppressWarnings("unchecked")
    private static <T> Class<T> generify(Class<?> cls) {
        return (Class<T>) cls;
    }

    @Override
    public void load(@Nonnull Properties properties) {
        List<RecentProjectRecord> value = new ArrayList<>();
        String name = getName();
        int i = 1;
        Object file;
        do {
            file = properties.get(name + "_" + i + "_FILE");
            if (file == null || !(file instanceof String) || "".equals(file)) {
                break;
            }
            Object projectName = properties.get(name + "_" + i + "_NAME");
            if (projectName == null || !(projectName instanceof String) || "".equals(projectName)) {
                break;
            }

            value.add(new RecentProjectRecord((String) projectName, (String) file, null));
            i++;
        } while (true);

        setValue(value);
    }

    @Override
    public void save(@Nonnull Properties properties) {
        String name = getName();
        int i = 1;
        List<RecentProjectRecord> value = getValue();
        if (value != null) {
            for (RecentProjectRecord recentProject : value) {
                properties.put(name + "_" + i + "_FILE", recentProject.getProjectPath());
                properties.put(name + "_" + i + "_NAME", recentProject.getProjectName());
                i++;
            }
        }
        properties.remove(name + "_" + i + "_FILE");
    }
}
