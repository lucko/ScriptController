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

import java.util.Collection;

/**
 * A registry of {@link Export}s shared between scripts.
 *
 * <p>Allows scripts to share persistent state, or provide a resource in a known
 * namespace.</p>
 *
 * <p>Some scripts will be designed to be totally stateless, and may use exports
 * to store state between invocations.</p>
 */
public interface ExportRegistry {

    /**
     * Creates a new standalone {@link ExportRegistry}.
     *
     * @return a new export registry
     */
    static ExportRegistry create() {
        return new ExportRegistryImpl();
    }

    /**
     * Gets an export
     *
     * @param name the name of the export
     * @param <T> the export type
     * @return the export
     */
    <T> Export<T> get(String name);

    /**
     * Gets a pointer to an export
     *
     * @param name the name of the export
     * @param <T> the export type
     * @return a pointer
     * @see Export.Pointer
     */
    default <T> Export.Pointer<T> pointer(String name) {
        return this.<T>get(name).pointer();
    }

    /**
     * Deletes an export
     *
     * @param name the name of the export to remove.
     */
    void remove(String name);

    /**
     * Returns a collection of all known exports.
     *
     * @return a collection of known exports
     */
    Collection<Export<?>> getAll();

}
