// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.service;

/**
 * ServiceVisitor defines the Gang of Four Visitor pattern contract for
 * visiting service providers and their provided service descriptors.
 * It is provided as a class with empty implementations rather than an
 * interface so that simple implementations need override only one of
 * the methods.
 */
public class ServiceVisitor
{
    public void visitProvider(ServiceProvider p) {}

    public void visitDescriptor(ServiceDescriptor d) {}
}


