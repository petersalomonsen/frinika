// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import uk.org.toot.swingui.SpringUtilities;
import uk.org.toot.audio.server.*;
//import uk.org.toot.swing.DisposablePanel;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeEvent;
import com.frinika.toot.PriorityAudioServer;

/**
 * An AudioServerPanel provides a UI for an AudioServer which allows control
 * of internal buffer time and latency time and monitors actual latency.
 * The panel polls the values at periodic intervals. 
 */
public class AudioServerPanel extends AbstractAudioServerPanel
{
    private ExtendedAudioServer server;
    private AudioServerConfiguration config;

    private JSpinner bufferMillis;
    private JSpinner latencyMillis;
    private JLabel actualLatencyMillis, lowestLatencyMillis, totalLatencyMillis; 
    private JLabel bufferUnderRuns;
    private JLabel loadTimePercent;

    private List<JLabel> outputLatencyLabels = new java.util.ArrayList<JLabel>();
    private List<JLabel> inputLatencyLabels = new java.util.ArrayList<JLabel>();

    private boolean eachIOlatency = true;

    private int underRunCount = 0;

    private DateFormat shortTime;

	private static boolean isLinux =
		System.getProperty("os.name").equals("Linux");
	
    public AudioServerPanel(final ExtendedAudioServer server, final AudioServerConfiguration config) {
        this.server = server;
        this.config = config;
        eachIOlatency = server instanceof JavaSoundAudioServer; // !!!
        shortTime = DateFormat.getTimeInstance(DateFormat.SHORT);
        setLayout(new BorderLayout());
        add(buildManagementPanel(), BorderLayout.WEST);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

  	protected void updatePeriodic() {
        if ( server == null ) return;
        if ( !isShowing() ) return;
  		actualLatencyMillis.setText(dpString(server.getActualLatencyMilliseconds(), 1));
  		lowestLatencyMillis.setText(dpString(server.getLowestLatencyMilliseconds(), 1));
  		float totalLatencyFrames = server.getTotalLatencyFrames();
  		float totalLatency = 1000 * totalLatencyFrames / server.getSampleRate();
  		totalLatencyMillis.setText(dpString(totalLatency, 1));
        int underRuns = server.getBufferUnderRuns();
        bufferUnderRuns.setText(String.valueOf(underRuns));
		loadTimePercent.setText(dpString(100 * server.getLoad(), 1));
        
      	if ( underRuns != underRunCount ) {
			String time = shortTime.format(new Date());
        	System.err.println(time+" UnderRun "+underRuns+
                ", L="+dpString(server.getLowestLatencyMilliseconds(), 1)+"ms");
        	underRunCount = underRuns;
            // reset the metrics !!! !!!
            server.resetMetrics(false);
      	}

        if ( eachIOlatency ) {
        	for ( int i = 0; i < outputLatencyLabels.size(); i++ ) {
            	float latencyMillis = 1000 * server.getOutputs().get(i).getLatencyFrames() / server.getSampleRate();
	            outputLatencyLabels.get(i).setText(dpString(latencyMillis, 1));
    		}

	        for ( int i = 0; i < inputLatencyLabels.size(); i++ ) {
    	        float latencyMillis = 1000 * server.getInputs().get(i).getLatencyFrames() / server.getSampleRate();
        	    inputLatencyLabels.get(i).setText(dpString(latencyMillis, 1));
        	}
        }
	}

    protected JPanel buildButtonPanel() {
    	JPanel p = new JPanel();
    	JButton reset = new JButton("Reset");
    	reset.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			server.resetMetrics(true);
    		}
    	});
       	p.add(reset);
    	return p;
    }

    protected JPanel buildManagementPanel() {
		// Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
        addRow(p, "Sample Rate", new JLabel(String.valueOf((int)server.getSampleRate()), JLabel.CENTER), "Hz");
        addRow(p, "Sample Size", new JLabel(String.valueOf(server.getSampleSizeInBits()), JLabel.CENTER), "bits");

        if ( isLinux && server instanceof PriorityAudioServer ) {
        	final PriorityAudioServer pas = (PriorityAudioServer)server;
        	int prio = pas.getPriority();
        	if ( prio < 0 || prio > 99 ) prio = 0;
            final SpinnerNumberModel priorityModel =
                new SpinnerNumberModel(prio, 0, 99, 1);
            final JSpinner priority = new Spinner(priorityModel);
            priority.addChangeListener(
                new ChangeListener() {
                	public void stateChanged(ChangeEvent e) {
                    	pas.requestPriority(priorityModel.getNumber().intValue());
                    	config.update();
                	}
            	}
            );
            addRow(p, "Priority", priority, "");
        }

        final SpinnerNumberModel bufferModel =
            new SpinnerNumberModel((int)server.getBufferMilliseconds(), 1, 10, 1);
        bufferMillis = new Spinner(bufferModel);
        bufferMillis.addChangeListener(
            new ChangeListener() {
            	public void stateChanged(ChangeEvent e) {
                	server.setBufferMilliseconds((float)bufferModel.getNumber().intValue());
                	config.update();
            	}
        	}
        );
        final SpinnerNumberModel latencyModel =
            new SpinnerNumberModel((int)server.getLatencyMilliseconds(),
            	(int)server.getMinimumLatencyMilliseconds(), 
            	(int)server.getMaximumLatencyMilliseconds(), 1);
        latencyMillis = new Spinner(latencyModel);
        latencyMillis.addChangeListener(
            new ChangeListener() {
            	public void stateChanged(ChangeEvent e) {
            		float latencyms = (float)latencyModel.getNumber().intValue();
           			server.setLatencyMilliseconds(latencyms);
           			config.update();
            	}
        	}
        );
        actualLatencyMillis = new JLabel("n/a", JLabel.CENTER);
        lowestLatencyMillis = new JLabel("n/a", JLabel.CENTER);
        totalLatencyMillis = new JLabel("n/a", JLabel.CENTER);
		bufferUnderRuns = new  JLabel("n/a", JLabel.CENTER);
        loadTimePercent = new JLabel("n/a", JLabel.CENTER);

        addRow(p, "Internal Buffer", bufferMillis, "ms");
        addRow(p, "Requested Latency", latencyMillis, "ms");
        addRow(p, "Actual Latency", actualLatencyMillis, "ms");
        addRow(p, "Lowest Latency", lowestLatencyMillis, "ms");
        addRow(p, "Total Latency", totalLatencyMillis, "ms");
        addRow(p, "Buffer UnderRuns", bufferUnderRuns, "");
        addRow(p, "Time Load", loadTimePercent, "%");

        if ( eachIOlatency ) {
	        for ( int i = 0; i < server.getOutputs().size(); i++ ) {
    	        JLabel outputLatency = new JLabel("n/a", JLabel.CENTER);
        	    outputLatencyLabels.add(outputLatency);
        		addRow(p, server.getOutputs().get(i).getName()+" Latency", outputLatency, "ms");
    	    }

        	for ( int i = 0; i < server.getInputs().size(); i++ ) {
            	JLabel inputLatency = new JLabel("n/a", JLabel.CENTER);
	            inputLatencyLabels.add(inputLatency);
    	    	addRow(p, server.getInputs().get(i).getName()+" Latency", inputLatency, "ms");
        	}
        }

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p,
			gridRows, 3, 	// rows, cols
            6, 6,       	// initX, initY
            6, 6);      	// xPad, yPad
        return p;
    }

    private static Dimension spinnerSize = new Dimension(50, 24);

    static protected class Spinner extends JSpinner
    {
        public Spinner(SpinnerModel model) {
            super(model);
        }

        public Dimension getMaximumSize() {
            return spinnerSize;
        }

        public Dimension getPreferredSize() {
            return spinnerSize;
        }
    }
}
