// Copyright (C) 2005 - 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.misc;

import java.util.Observer;

public interface IObservable
{
    void addObserver(Observer o);

    int countObservers();

    void deleteObserver(Observer o);

    void deleteObservers();

    boolean hasChanged();

    void notifyObservers();

    void notifyObservers(Object arg);

//    void fireChanged(); //!!!
}
