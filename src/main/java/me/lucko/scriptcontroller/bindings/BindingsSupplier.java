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

import java.util.Map;

/**
 * Supplies bindings to an accumulator
 */
@FunctionalInterface
public interface BindingsSupplier {

    static BindingsSupplier singleBinding(String name, Object value) {
        return accumulator -> accumulator.put(name, value);
    }

    static BindingsSupplier ofMap(Map<String, Object> map) {
        return accumulator -> {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                accumulator.put(entry.getKey(), entry.getValue());
            }
        };
    }

    /**
     * Adds this suppliers bindings to the accumulator
     *
     * @param accumulator the accumulator
     */
    void accumulateTo(BindingsBuilder accumulator);

}
