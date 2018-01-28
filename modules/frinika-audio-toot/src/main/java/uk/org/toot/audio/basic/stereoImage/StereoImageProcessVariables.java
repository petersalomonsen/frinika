// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.stereoImage;

/**
 * Provides the contract that decouples StereoImageProcess from StereoImageControls
 * @author st
 *
 */
public interface StereoImageProcessVariables
{
    float getWidthFactor(); // +1..-1 for Mono..Wide
    boolean isLRSwapped();
    boolean isBypassed();
}
