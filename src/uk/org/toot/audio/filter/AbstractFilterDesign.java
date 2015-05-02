// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

abstract public class AbstractFilterDesign implements FilterDesign
{
    /**
     * @supplierCardinality 1 
     * @link aggregation
     */
    protected FilterSpecification spec;

    public AbstractFilterDesign(FilterSpecification spec) {
        this.spec = spec;
    }

    public FilterSpecification getFilterSpecification() {
        return spec;
    }
}
