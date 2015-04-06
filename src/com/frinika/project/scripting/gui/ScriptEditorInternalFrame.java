/*
 * Created on February 16, 2007
 * 
 * Copyright (c) 2007 Jens Gulden
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.project.scripting.gui;

import com.frinika.project.scripting.FrinikaScript;
import com.frinika.project.scripting.FrinikaScriptingEngine;
import com.frinika.project.scripting.DefaultFrinikaScript;
import com.frinika.project.scripting.ScriptListener;
import javax.swing.JInternalFrame;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

/**
 * A simple text-editor for script source-code. 
 *
 * (Created with NetBeans 5.5 gui-editor, see corresponding .form file.)
 *
 * @see ScriptingDialog
 * @author Jens Gulden
 */
class ScriptEditorInternalFrame extends JInternalFrame implements ScriptListener, Runnable {
    
    final int EXTERNAL_CHANGE_TEST_INTERVAL = 250; // milliseconds to test whether file externally overwritten
	
    protected FrinikaScript script;
    protected boolean dirty;
    protected ScriptingDialog dialog;
    protected long lastSaveTimestamp = 0;
    
    /** Creates new form ScriptEditorInternalFrame */
    public ScriptEditorInternalFrame(FrinikaScript script, ScriptingDialog dialog) {
    	super();
    	this.script = script;
        this.dialog = dialog;
    	dirty = false;
        initComponents();
        if (! (script instanceof DefaultFrinikaScript) ) {
            editorPane.setEditable( false );
            editorPane.setBackground(new Color(240, 240, 240));
        }
        getRootPane().setDefaultButton(runButton);
        //runStopButtonGroup.add(runToggleButton);
        //runStopButtonGroup.add(stopToggleButton);
        scriptExited(script, null); // to set initial state of buttons
        /*editorPane.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyPressed(KeyEvent e) {
        		if ( ! editorPane.isEditable() ) return;
        		if ( ( e.isControlDown() ) && (e.getKeyCode() == KeyEvent.VK_ENTER) ) { // Ctrl-Enter: execute script 
        			runButton.doClick();
        			e.consume();
        		} else {
        			//setDirty(true);
        		}
        	}
        	@Override
        	public void keyTyped(KeyEvent e) {
        		char c = e.getKeyChar();
        		if (Character.isDefined(c)) { // 'real' character has been entered
        			setDirty(true);
        		}
        	}
        });
        editorPane.addFocusListener(new FocusAdapter() {
        	public void focusLost(FocusEvent e) {
        		update();
        	}
        });*/
        /*editorPane.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setDirty(true);
			}
			public void insertUpdate(DocumentEvent e) {
				setDirty(true);
			}
			public void removeUpdate(DocumentEvent e) {
				setDirty(true);
			}
        });*/
        refresh();
        dialog.engine.addScriptListener(this);
        //editorPane.requestFocus();
        if ( script instanceof DefaultFrinikaScript ) {
        	String filename = ((DefaultFrinikaScript)script).getFilename();
        	if (filename != null) {
        		File file = new File(filename);
        		if (file.exists()) {
                	lastSaveTimestamp = file.lastModified(); 
        		}
        	}
            (new Thread(this)).start(); // watchdog for external changes
        }
    }
    
    public void toFront() {
    	super.toFront();
    	editorPane.requestFocus();
    }
    
    void setDirty(boolean d) {
    	if (dirty != d) {
        	dirty = d;
    		updateTitle();
    	}
    }
    
    boolean hasBeenModifiedWithoutSaving() {
    	return dirty;
    }
    
    boolean hasBeenModifiedByExternalApplication() {
        if (script instanceof DefaultFrinikaScript) {
            if (lastSaveTimestamp > 0) {
               String filename = ((DefaultFrinikaScript)script).getFilename();
               File file = new File(filename);
               return (file.lastModified() != lastSaveTimestamp);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    FrinikaScript getScript() {
    	return script;
    }
    
    void setScript(FrinikaScript script) {
    	this.script = script;
    	refresh();
    	setDirty(false);
    }
    
    void reload() {
        try {
            //dialog.engine.reloadScript((DefaultFrinikaScript)script);
        	DefaultFrinikaScript script = ((DefaultFrinikaScript)this.script);
    		String filename = script.getFilename();
    		File file = new File(filename);
    		String source = FrinikaScriptingEngine.loadString(file);
    		script.setSource(source);
            refresh();
            lastSaveTimestamp = file.lastModified();
            setDirty(false);
        } catch (IOException ioe) {
            dialog.frame.error(ioe);
        }
    }
    
    public void refresh() {
    	editorPane.setText(script.getSource());
        updateTitle();
    }
    
    public void update() {
        if (script instanceof DefaultFrinikaScript) {
            ((DefaultFrinikaScript)script).setSource(editorPane.getText());
        }
    }
    
    protected void updateTitle() {
    	/*String t = this.getTitle();
    	boolean e = t.endsWith(" *");
    	if ( (!dirty) && e ) {
    		t = t.substring(0, t.length() - 2);
    	} else if ( dirty && (!e) ) {
    		t = t + " *";
    	}*/
    	String t = script.getName();
        if (dirty) {
            t = t + " *";
    	}
        if (! t.equals(this.getTitle()) ) {
        	this.setTitle(t);
        	dialog.updateMenus();
        }
    }
    
    public void scriptStarted(FrinikaScript script) {
//System.out.println("script started "+script);
        if (script == this.script) {
            //runToggleButton.setSelected(true);
            runButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    public void scriptExited(FrinikaScript script, Object returnValue) {
//System. out.println("script exited "+script);
        if (script == this.script) {
            //stopToggleButton.setSelected(true);
            runButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
    
    
    /**
     * Background thread for watching external changes.
     */
    public void run() {
        String filename = ((DefaultFrinikaScript)script).getFilename();
//System. out.println("starting watchdog thread for " + filename);
        while ( ! this.isClosed() ) {
            if ( hasBeenModifiedByExternalApplication() ) {
                if ( dialog.frame.confirm("Script " + filename + " has been modified by an external application. Reload?") ) {
                    if ( ( ! hasBeenModifiedWithoutSaving() ) || dialog.frame.confirm( "This will DESTROY local changes. Reload anyway?" ) ) {
                        reload();
                    }
                } else { // don't reload, leave external changes for now
                	lastSaveTimestamp = 0;
                	setDirty(true);
                }
            }
            try {
                Thread.sleep( EXTERNAL_CHANGE_TEST_INTERVAL );
            } catch (InterruptedException ie) {
                // nop
            }
        }
//System. out.println("watchdog thread for " + ((DefaultFrinikaScript)script).getFilename() + " has ended");
    }

/*    
    	update();
    	dialog.engine.executeScript(this.getScript(), dialog.frame);

        dialog.engine.stopScript(this.getScript());
*/        
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        toolBar = new javax.swing.JToolBar();
        jPanel1 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        editorPane.setFont(new java.awt.Font("DialogInput", 0, 12));
        editorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                editorPaneFocusLost(evt);
            }
        });
        editorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                editorPaneKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                editorPaneKeyTyped(evt);
            }
        });

        scrollPane.setViewportView(editorPane);

        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 0));

        runButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("icons/play.png")));
        runButton.setToolTipText("Ctrl-Enter");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        jPanel1.add(runButton);

        stopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("icons/stop.png")));
        stopButton.setMnemonic('S');
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        jPanel1.add(stopButton);

        toolBar.add(jPanel1);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void editorPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorPaneKeyPressed
        if ( ! editorPane.isEditable() ) return;
        if ( ( evt.isControlDown() ) && (evt.getKeyCode() == KeyEvent.VK_ENTER) ) { // Ctrl-Enter: execute script 
            runButton.doClick();
            evt.consume();
        } else {
            //setDirty(true);
        }
    }//GEN-LAST:event_editorPaneKeyPressed

    private void editorPaneKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorPaneKeyTyped
        //char c = evt.getKeyChar();
        //if (Character.isDefined(c)) { // 'real' character has been entered
            setDirty(true);
        //}
    }//GEN-LAST:event_editorPaneKeyTyped

    private void editorPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editorPaneFocusLost
        update();
    }//GEN-LAST:event_editorPaneFocusLost

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        update();
        dialog.executeScript(script);
    }//GEN-LAST:event_runButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        dialog.stopScript(script);
    }//GEN-LAST:event_stopButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton runButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton stopButton;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
    
}
