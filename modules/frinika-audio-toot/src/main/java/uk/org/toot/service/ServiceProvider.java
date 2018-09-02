// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.service;

import java.util.Iterator;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * An abstract ServiceProvider.
 *
 * Extend this class for specific subject domains.
 */
abstract public class ServiceProvider
{
    protected final int providerId;

    /**
     * A <code>String</code> to be returned from
     * <code>getProviderName</code>, initially <code>null</code>.
     * Constructors should set this to a non-<code>null</code> value.
     */
    protected String providerName;

    /**
     * A <code>String</code> to be returned from
     * <code>getVersion</code>, initially null.  Constructors should
     * set this to a non-<code>null</code> value.
     */
    protected String version;

    protected String description;

    /**
     * Constructs an <code>ServiceProvider</code> with a given
     * provider name and version identifier.
     *
     * @param providerName the provider name.
     * @param version a version identifier.
     *
     * @exception IllegalArgumentException if <code>providerName</code>
     * is <code>null</code>.
     * @exception IllegalArgumentException if <code>version</code>
     * is <code>null</code>.
     */
    public ServiceProvider(int providerId, String providerName,
        					  String description,
                              String version) {

        if (providerName == null) {
            throw new IllegalArgumentException("providerName == null!");
        }
        if (version == null) {
            throw new IllegalArgumentException("version == null!");
        }
        if (description == null) {
            throw new IllegalArgumentException("description == null!");
        }
        this.providerId = providerId;
        this.providerName = providerName;
        this.description = description;
        this.version = version;
    }

    /**
     * Constructs a blank <code>ServiceProvider</code>.  It is up
     * to the subclass to initialize instance variables and/or
     * override method implementations in order to ensure that the
     * <code>getProviderName</code> and <code>getVersion</code> methods
     * will return non-<code>null</code> values.
     */
//    protected ServiceProvider() {
//    }

    public int getProviderId() { return providerId; }

    /**
     * Returns the name of the provider responsible for creating this
     * service provider and its associated implementation.  Because
     * the provider name may be used to select a service provider,
     * it is not localized.
     *
     * <p> The default implementation returns the value of the
     * <code>providerName</code> instance variable.
     *
     * @return a non-<code>null</code> <code>String</code> containing
     * the name of the provider.
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Returns a string describing the version
     * number of this service provider and its associated
     * implementation.  Because the version may be used by transcoders
     * to identify the service providers they understand, this method
     * is not localized.
     *
     * <p> The default implementation returns the value of the
     * <code>version</code> instance variable.
     *
     * @return a non-<code>null</code> <code>String</code> containing
     * the version of this service provider.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns a brief, human-readable description of this service
     * provider and its associated implementation.  The resulting
     * string should be localized for the supplied
     * <code>Locale</code>, if possible.
     *
     * @param locale a <code>Locale</code> for which the return value
     * should be localized.
     *
     * @return a <code>String</code> containing a description of this
     * service provider.
     */
    public String getDescription(Locale locale) {
        return description;
    }

    private Hashtable<Class<?>,List<ServiceDescriptor>> services =
        new Hashtable<Class<?>,List<ServiceDescriptor>>();

    protected List<ServiceDescriptor> service(Class<?> serviceClass) {
        // if already a list keyed by clazz we're ok
        // otherwise create a new list keyed by clazz and add to that list
        if ( services.get(serviceClass) == null ) {
            services.put(serviceClass, new ArrayList<ServiceDescriptor>());
//            System.out.println("new service class: "+serviceClass);
        }
        return services.get(serviceClass);
    }

    /**
     * Adds a ServiceDescriptor to the matching service.
     */
    protected void add(ServiceDescriptor d) {
//        System.out.println("service: "+d);
		for ( Class<?> clazz : services.keySet() ) {
//            System.out.println("checking class: "+clazz);
            if ( clazz.isAssignableFrom(d.getServiceClass()) ) {
                services.get(clazz).add(d);
//	            System.out.println("new service : "+d);
                return;
            }
        }
	}

    /**
     * Adds a ServiceDescriptor for the matching service.
     */
    protected void add(Class clazz, String name, String description, String version) {
        add(new ServiceDescriptor(clazz, name, description, version));
    }

    /**
     * Accepts a ServiceVisitor to visit all or specific services.
     * If clazz is null all services are visited, otherwise the services
     * specified by clazz are visited,
     */
	public void accept(ServiceVisitor v, Class<?> clazz) {
		v.visitProvider(this);
        // if clazz is null, visit all service classes iterators
		if ( clazz == null ) {
            for ( Class<?> c : services.keySet() ) {
	            visit(v, services.get(c).iterator());
            }
        } else { // else just visit clazz iterator
            visit(v, services.get(clazz).iterator());
        }
	}

    protected void visit(ServiceVisitor v, Iterator<ServiceDescriptor> dit) {
       	while ( dit.hasNext() ) {
			v.visitDescriptor(dit.next());
       	}
    }


    public String toString() {
        return description+" v"+version+" by "+providerName;
    }
}
