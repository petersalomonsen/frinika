// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.service;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Services encapsulates the platform=specific provision of service lookup.
 */
public class Services {
  public static <T extends ServiceProvider> Iterator<T> lookup(Class<T> clazz) {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
    return serviceLoader.iterator();
  }
}
