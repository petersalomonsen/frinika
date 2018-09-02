// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

public interface NativeSupport
{
	boolean canAddUI();
	void addUI(javax.swing.JPanel panel);
	
	boolean canPersistMidi();
	void recall(javax.sound.midi.Track t, int index);
	void store(javax.sound.midi.Track t);
}
