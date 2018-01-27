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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration property for list packed as single string.
 *
 * @author hajdam
 */
public class PackedStringListProperty extends ConfigurationProperty<String> {

    public PackedStringListProperty(@Nonnull FrinikaGlobalProperty globalProperty, @Nullable String value) {
        super(String.class, globalProperty, value);
    }

    @Nullable
    public List<String> getStringList() {
        String encodedDevices = super.getValue();
        if (encodedDevices == null) {
            encodedDevices = "";
        }
        String[] list = encodedDevices.split(";");
        List<String> devicesList = new ArrayList<>();
        for (String deviceName : list) {
            if (!deviceName.equals("")) {
                devicesList.add(deviceName);
            }
        }
        return devicesList;
    }

    public void setStringList(@Nullable List<String> list) {
        if (list == null) {
            super.setValue(null);
        } else {
            StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (String deviceName : list) {
                if (!first) {
                    buf.append(";");
                }
                buf.append(deviceName);
                first = false;
            }
            String encodedDevices = buf.toString();
            super.setValue(encodedDevices);
        }
    }
}
