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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration property.
 *
 * @author hajdam
 * @param <T> property type
 */
public class ConfigurationProperty<T> {

    @Nonnull
    private static final Map<String, ConfigurationProperty<?>> PROPERTIES_BY_NAME = new HashMap<>();

    @Nonnull
    private final FrinikaGlobalProperty globalProperty;
    @Nullable
    private T value;
    @Nonnull
    private final Class<T> typeClass;

    public ConfigurationProperty(@Nonnull Class<T> typeClass, @Nonnull FrinikaGlobalProperty globalProperty, @Nullable T value) {
        this.globalProperty = globalProperty;
        this.value = value;
        this.typeClass = typeClass;

        registerProperty(globalProperty);
    }

    private void registerProperty(@Nonnull FrinikaGlobalProperty globalProperty) {
        PROPERTIES_BY_NAME.put(globalProperty.getName(), this);
    }

    @Nonnull
    public String getName() {
        return globalProperty.getName();
    }

    @Nonnull
    public Class<T> getType() {
        return typeClass;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    @Nullable
    public static ConfigurationProperty<?> findByName(@Nonnull String name) {
        return PROPERTIES_BY_NAME.get(name);
    }
}
