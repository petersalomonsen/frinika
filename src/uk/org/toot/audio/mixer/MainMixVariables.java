// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import uk.org.toot.control.EnumControl;

/**
 * Specialises MixVariables for the main bus which has routing.
 * @author st
 *
 */
public interface MainMixVariables extends MixVariables
{
    EnumControl getRouteControl();
}
