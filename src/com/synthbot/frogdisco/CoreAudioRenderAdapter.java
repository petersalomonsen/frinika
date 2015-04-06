/*
 *  Copyright 2011 Martin Roth (mhroth@gmail.com)
 *
 *  This file is part of FrogDisco.
 *
 *  FrogDisco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FrogDisco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with FrogDisco.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.synthbot.frogdisco;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CoreAudioRenderAdapter implements CoreAudioRenderListener {

  @Override
  public void onCoreAudioShortRenderCallback(ShortBuffer buffer) {
    // nothing to do
  }

  @Override
  public void onCoreAudioFloatRenderCallback(FloatBuffer buffer) {
    // nothing to do
  }

}
