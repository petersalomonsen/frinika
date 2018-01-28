package uk.org.toot.swingui.audioui.serverui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public abstract class AbstractAudioServerPanel extends JPanel
{
	protected int gridRows = 0;

	// default 2 second periodic timer
	public AbstractAudioServerPanel() {
		this(2000);
	}
	
	public AbstractAudioServerPanel(int periodMilliseconds) {
		if ( periodMilliseconds > 0 ) {
			Timer timer = new Timer(periodMilliseconds,
				new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						updatePeriodic();
					}				
				}
			);
  			timer.start();
		}
	}
	
	protected void updatePeriodic() {		
	}
	
	protected void addRow(JPanel p, String label, JComponent comp, String units) {
		JLabel l = new JLabel(label+" :", JLabel.TRAILING);
	    p.add(l);
	    l.setLabelFor(comp);
	    p.add(comp);
	    l = new JLabel(units, JLabel.LEADING);
	    p.add(l);
	    l.setLabelFor(comp);
	    gridRows += 1;
	}

	protected String dpString(float ms, int dp) {
	    return String.format("%1$."+dp+"f", ms);
	}

}
