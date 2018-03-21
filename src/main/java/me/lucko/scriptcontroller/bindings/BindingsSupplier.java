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

package me.lucko.scriptcontroller.bindings;

import me.lucko.scriptcontroller.environment.script.Script;

import java.util.Map;

/**
 * Supplies a set of bindings for scripts to use at runtime.
 */
@FunctionalInterface
public interface BindingsSupplier {

    /**
     * Returns a {@link BindingsSupplier} that encapsulates a single binding.
     *
     * @param name the name of the binding
     * @param value the corresponding value
     * @return the resultant bindings supplier
     */
    static BindingsSupplier singleBinding(String name, Object value) {
        return (script, accumulator) -> accumulator.put(name, value);
    }

    /**
     * Returns a {@link BindingsSupplier} that encapsulates a map of objects.
     *
     * @param map the map of bindings
     * @return the resultant bindings supplier
     */
    static BindingsSupplier ofMap(Map<String, Object> map) {
        return (script, accumulator) -> {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                accumulator.put(entry.getKey(), entry.getValue());
            }
        };
    }

    /**
     * Supplies this suppliers bindings for the given script.
     *
     * @param script the script the bindings are for
     * @param accumulator the accumulator
     */
    void supplyBindings(Script script, BindingsBuilder accumulator);

}
