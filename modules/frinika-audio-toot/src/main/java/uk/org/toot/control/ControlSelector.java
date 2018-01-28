// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * ControlSelector defines the contract for selecting Controls together with
 * some simple logical unary and binary implementations.
 */
public interface ControlSelector
{
    /**
     * The contract for slecting Controls.
     * @return true if Control selected, false otherwise.
     */
    boolean select(Control control);

    /**
     * A logical NOT selector which selects all Controls except the Control
     * specified.
     */
    class Not implements ControlSelector
    {
        private ControlSelector selector;

        public Not(ControlSelector selector) {
            this.selector = selector;
        }

        public boolean select(Control control) {
            return !selector.select(control);
        }
    }

	/**
     * An abstract logical binary selector which operates with two specified
     * Controls.
     */
    abstract class LogicalBinary implements ControlSelector
    {
        protected ControlSelector a;
        protected ControlSelector b;

        public LogicalBinary(ControlSelector a, ControlSelector b) {
            this.a = a;
            this.b = b;
        }
    }


    /**
     * A logical OR selector which selects either specified Control but no
     * others.
     */
    class Or extends LogicalBinary
    {
        public Or(ControlSelector a, ControlSelector b) {
            super(a, b);
        }

        public boolean select(Control control) {
            return a.select(control) || b.select(control);
        }
    }
}
