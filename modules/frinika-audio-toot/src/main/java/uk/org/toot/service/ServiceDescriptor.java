// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.service;

/**
 * A ServiceDescriptor describes a service to help
 * UIs use a name, implementations be created etc.
 */
public class ServiceDescriptor
{
    private Class<?> serviceClass;
    private String name;
    private String description;
    private String version;

	public ServiceDescriptor(Class<?> clazz, String name, String description, String version) {
        this.serviceClass = clazz;
        this.name = name;
        this.description = description;
        this.version = version;
    }

    public Class<?> getServiceClass() { return serviceClass; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getVersion() { return version; }

    public String toString() {
        return name+" "+version+" ("+description+") "+serviceClass;
    }
}
