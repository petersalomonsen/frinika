// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control.spi;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.service.*;

/**
 * Adds a module ID for Controls
 */
public class ControlServiceDescriptor extends ServiceDescriptor
{
    private int moduleId = 0;

    public ControlServiceDescriptor(Class<? extends CompoundControl> clazz, int moduleId, String name, String description, String version) {
        super(clazz, name, description, version);
        this.moduleId = moduleId;
    }

    public int getModuleId() { return moduleId; }
}
