package uk.org.toot.misc;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;



public class ChangeSupport implements java.io.Serializable
{
	/**
	 * "listeners" lists all the generic listeners.
	 *
	 *  This is transient - its state is written in the writeObject method.
	 */
	transient private java.util.Vector<ChangeListener> listeners;

	public ChangeSupport(Object source) {
		
	}
	
	/**
	 * Add a ChangeListener to the listener list.
	 *
	 * @param listener  The ChangeListener to be added
	 */
	public synchronized void addChangeListener(
			ChangeListener listener) {

		if (listeners == null) {
			listeners = new java.util.Vector<ChangeListener>();
		}
		listeners.addElement(listener);
	}

	/**
	 * Remove a ChangeListener from the listener list.
	 *
	 * @param listener  The ChangeListener to be removed
	 */
	public synchronized void removeChangeListener(
			ChangeListener listener) {

		if (listeners == null) {
			return;
		}
		listeners.removeElement(listener);
	}

	/**
	 * Returns an array of all the listeners that were added to the
	 * ChangeSupport object with addChangeListener().
	 * <p>
	 * @return all of the <code>ChangeListeners</code> added or an
	 *         empty array if no listeners have been added
	 * @since 1.4
	 */
	public synchronized ChangeListener[] getChangeListeners() {
		List<ChangeListener> returnList = new ArrayList<ChangeListener>();

		// Add all the ChangeListeners 
		if (listeners != null) {
			returnList.addAll(listeners);
		}
		return (ChangeListener[])(returnList.toArray(
				new ChangeListener[0]));
	}

	/**
	 * Fire an existing ChangeEvent to any registered listeners.
	 * @param evt  The ChangeEvent object.
	 */
	public void fireChange(ChangeEvent evt) {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				ChangeListener listener = (ChangeListener)listeners.elementAt(i);
				listener.stateChanged(evt);
			}
		}
	}

	/**
	 * Check if there are any listeners.
	 *
	 * @return true if there are ore or more listeners
	 */
	public synchronized boolean hasListeners() {
		if (listeners != null && !listeners.isEmpty()) {
			// there is a generic listener
			return true;
		}
		return false;
	}

	/**
	 * @serialData Null terminated list of <code>ChangeListeners</code>.
	 * <p>
	 * At serialization time we skip non-serializable listeners and
	 * only serialize the serializable listeners.
	 *
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();

		java.util.Vector v = null;
		synchronized (this) {
			if (listeners != null) {
				v = (java.util.Vector) listeners.clone();
			}
		}

		if (v != null) {
			for (int i = 0; i < v.size(); i++) {
				ChangeListener l = (ChangeListener)v.elementAt(i);
				if (l instanceof Serializable) {
					s.writeObject(l);
				}
			}
		}
		s.writeObject(null);
	}


	private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
		s.defaultReadObject();

		Object listenerOrNull;
		while (null != (listenerOrNull = s.readObject())) {
			addChangeListener((ChangeListener)listenerOrNull);
		}
	}

	/**
	 * Internal version number
	 * @serial
	 * @since
	 */
//	private int changeSupportSerializedDataVersion = 2;

	/**
	 * Serialization version ID, so we're compatible with JDK 1.1
	 */
//	static final long serialVersionUID = 6401253773779951803L;
}
