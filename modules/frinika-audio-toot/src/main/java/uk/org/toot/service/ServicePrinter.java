// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.service;

import java.io.PrintStream;

/**
 * An example of using the Visitor pattern to print ServiceDescriptors
 * provided by ServiceProviders.
 */
public class ServicePrinter extends ServiceVisitor
{
    private PrintStream s;

    public ServicePrinter() {
        this(System.out);
    }

    public ServicePrinter(PrintStream stream) {
        s = stream;
    }

   	public void visitProvider(ServiceProvider p) {
       	s.println(p);
   	}

   	public void visitDescriptor(ServiceDescriptor d) {
       	s.print("  ");
       	s.println(d);
   	}
}
