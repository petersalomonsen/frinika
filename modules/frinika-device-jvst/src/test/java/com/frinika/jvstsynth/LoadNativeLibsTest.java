/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://www.petersalomonsen.com) - Licensed under GNU GPL
 */
package com.frinika.jvstsynth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class LoadNativeLibsTest {

    public LoadNativeLibsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void loadLibs() throws Exception {
        NativeLibLoader.loadNativeLibs();
        assertTrue(NativeLibLoader.isLoadedNativeLib());
    }
}
