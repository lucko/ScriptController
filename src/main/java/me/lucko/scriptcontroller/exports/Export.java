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

package me.lucko.scriptcontroller.exports;

import java.util.function.Supplier;

/**
 * A namespaced value wrapper.
 *
 * @param <T> the export type
 */
public interface Export<T> {

    /**
     * Gets the name of the export
     *
     * @return the name
     */
    String name();

    /**
     * Returns a pointer to this export
     *
     * @return a pointer
     */
    Pointer<T> pointer();

    /**
     * Gets the current value of the export
     *
     * @return the current value
     */
    T get();

    /**
     * Gets the current value of the export, or returns the other if a value
     * isn't present.
     *
     * @param other the other value
     * @return the value
     */
    T get(T other);

    /**
     * Sets the value of the export
     *
     * @param value the value to set
     *  @return this
     */
    Export<T> put(T value);

    /**
     * Sets the value of the export if a value isn't already present,
     * then returns the export
     *
     * @param value the value to set if absent
     * @return this
     */
    Export<T> putIfAbsent(T value);

    /**
     * Uses the provided function to compute a value if one isn't already present.
     *
     * @param other the other value
     * @return the value
     */
    Export<T> computeIfAbsent(Supplier<? extends T> other);

    /**
     * Gets if this export has a value
     *
     * @return true if this export has a value
     */
    boolean containsValue();

    /**
     * Clears the export
     */
    void clear();

    /**
     * A pointer to the value of an export.
     *
     * <p>Can be used in scripts to simplify the process of obtaining an export
     * whose instance is likely to change during runtime.</p>
     *
     * <p>e.g.</p>
     *
     * <code>
     *     const someExport = exports.pointer("example-namespace");
     *     someExport().doSomething();
     * </code>
     *
     * @param <T> the type
     */
    interface Pointer<T> extends Supplier<T> {

    }

}
