package uk.org.toot.swingui.midiui;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import uk.org.toot.midi.core.ConnectedMidiSystem;

public class MidiConnectionView extends JPanel
{
	public MidiConnectionView(ConnectedMidiSystem system) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new MidiConnectionMap(system));
		add(Box.createHorizontalStrut(32));
		JTable table = new MidiConnectionTable(system);
		JScrollPane scrollPane = new JScrollPane(table);
//1.6		table.setFillsViewportHeight(true);
		add(scrollPane);
	}
}
