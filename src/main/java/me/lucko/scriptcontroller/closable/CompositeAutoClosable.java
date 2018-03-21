/*
 * This file is part of ScriptController, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.scriptcontroller.closable;

/**
 * Represents an {@link AutoCloseable} made up of several other
 * {@link AutoCloseable}s.
 *
 * <p>The {@link #close()} method closes in LIFO (Last-In-First-Out) order.</p>
 *
 * <p>{@link CompositeAutoClosable}s can be reused. The instance is effectively
 * cleared on each invocation of {@link #close()}.</p>
 */
public interface CompositeAutoClosable extends AutoCloseable {

    /**
     * Creates a new standalone {@link CompositeAutoClosable}.
     *
     * @return a new {@link CompositeAutoClosable}.
     */
    static CompositeAutoClosable create() {
        return new AbstractCompositeAutoClosable();
    }

    /**
     * Binds an {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contains
     * closables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link #close() closed} twice.</p>
     *
     * @param autoCloseable the closable to bind
     * @throws NullPointerException if the closable is null
     * @return this (for chaining)
     */
    CompositeAutoClosable bind(AutoCloseable autoCloseable);

    /**
     * Binds all given {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contains
     * closables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link #close() closed} twice.</p>
     *
     * <p>Ignores null values.</p>
     *
     * @param autoCloseables the closables to bind
     * @return this (for chaining)
     */
    default CompositeAutoClosable bindAll(AutoCloseable... autoCloseables) {
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable == null) {
                continue;
            }
            bind(autoCloseable);
        }
        return this;
    }

    /**
     * Binds all given {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contains
     * closables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link #close() closed} twice.</p>
     *
     * <p>Ignores null values.</p>
     *
     * @param autoCloseables the closables to bind
     * @return this (for chaining)
     */
    default CompositeAutoClosable bindAll(Iterable<? extends AutoCloseable> autoCloseables) {
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable == null) {
                continue;
            }
            bind(autoCloseable);
        }
        return this;
    }

    /**
     * Closes this composite resource.
     *
     * @throws CompositeClosingException if any of the sub instances throw an
     *                                   exception whilst closing
     */
    @Override
    void close() throws CompositeClosingException;

    /**
     * Closes this composite resource, but doesn't rethrow or print any
     * exceptions.
     *
     * @see #close()
     */
    default void closeSilently() {
        try {
            close();
        } catch (CompositeClosingException e) {
            // ignore
        }
    }

    /**
     * Closes this composite resource, but simply prints any resultant
     * exceptions instead of rethrowing them.
     *
     * @see #close()
     * @see CompositeClosingException#printAllStackTraces()
     */
    default void closeAndReportExceptions() {
        try {
            close();
        } catch (CompositeClosingException e) {
            e.printAllStackTraces();
        }
    }

}
