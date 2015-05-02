// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.util.List;

public interface CompoundControlPersistence
{
    /*#List getPresets(CompoundControl c);*/
    List<String> getPresets(CompoundControl c);

    void loadPreset(CompoundControl c, String name);

    void savePreset(CompoundControl c, String name);
}
