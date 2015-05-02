// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.tonalityui;

import java.util.List;
import javax.swing.JList;

import uk.org.toot.music.tonality.*;

public class KeyList extends JList {

	public KeyList() {
		super();
	}

	public void setKeys(List<Key> keys) {
		setListData(keys.toArray());
	}
}
