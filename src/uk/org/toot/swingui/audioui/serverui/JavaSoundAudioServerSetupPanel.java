package uk.org.toot.swingui.audioui.serverui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.JavaSoundAudioServer;

public class JavaSoundAudioServerSetupPanel extends AbstractAudioServerPanel
{
	private JavaSoundAudioServer server;
	private AudioServerConfiguration setup;
	private JComboBox sampleRateCombo;
	
	public JavaSoundAudioServerSetupPanel(JavaSoundAudioServer server, AudioServerConfiguration p) {
		this.server = server;
		setup = p;
		sampleRateCombo = new SampleRateCombo();
		addRow(this, "Sample Rate", sampleRateCombo, "Hz");
 	}
	
	private class SampleRateCombo extends JComboBox
	{
		public SampleRateCombo() {
			addItem("44100");
			addItem("48000");
			addItem("88200");
			addItem("96000");
			addItemListener(
				new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						float sampleRate = Float.parseFloat((String)sampleRateCombo.getSelectedItem());
						server.setSampleRate(sampleRate);
						setup.update();
					}					
				}
			);
		}
	}

}
