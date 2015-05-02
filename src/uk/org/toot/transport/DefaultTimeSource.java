// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

public class DefaultTimeSource extends AbstractTimeSource 
{
    protected void store(long tsys, long time) {
    }

    protected long extrapolate(long tsys) {
        return tsys; // !!!
    }
}
