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

import java.util.function.Consumer;

import javax.script.Bindings;

/**
 * Chainable bindings builder.
 *
 * @see Bindings
 */
public interface BindingsBuilder {

    /**
     * Creates a new {@link BindingsBuilder}
     *
     * @param bindings the bindings to apply to
     * @return a new builder
     */
    static BindingsBuilder wrap(Bindings bindings) {
        return new BindingsBuilderImpl(bindings);
    }

    /**
     * Adds a binding to the builder
     *
     * @param name the name of the binding
     * @param object the value of the binding
     * @return this builder (for chaining)
     */
    BindingsBuilder put(String name, Object object);

    /**
     * Applies an action to this builder
     *
     * @param action the action to apply
     * @return this builder (for chaining)
     */
    BindingsBuilder apply(Consumer<Bindings> action);

    /**
     * Returns the modified {@link Bindings} instance
     *
     * @return the bindings
     */
    Bindings build();

}
