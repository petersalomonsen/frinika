// Copyright (C) 2007,2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control.automation;

/**
 * The contract for snapshot automation of controls.
 * recall() and store() are suitable for user snapshots.
 * It is intended that as well as user snapshots a project will have a
 * default snapshot. configure() is intended for use with this default
 * project snapshot and should be called prior to recall() in this case.
 * configure() should modify the control structure to suit the snapshot.
 */
public interface SnapshotAutomation
{
    /**
     * Modify the controls structure to suit the named snapshot.
     * Not intended for use by user snapshots, just project snapshots.
     * Intended to be called prior to recall() for project snapshots.
     */
	void configure(String name);

    /**
     * Recall a controls snapshot.
     * Does not modify the controls structure, use configure() to do that.
     */
    void recall(String name);

    /**
     * Store a controls snapshot.
     */
    void store(String name);

    /**
     * List stored snapshot names.
     * @return an array of name Strings
     */
    String[] list();
}
