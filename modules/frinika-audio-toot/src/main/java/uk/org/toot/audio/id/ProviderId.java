// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.id;

/**
 * This interface is kept for backwards compatibility but all provider ids
 * are now maintained in uk.org.toot.control.id.ProviderId such that ids
 * can be used across all domains, not just audio.
 */
public interface ProviderId extends uk.org.toot.control.id.ProviderId
{
}
