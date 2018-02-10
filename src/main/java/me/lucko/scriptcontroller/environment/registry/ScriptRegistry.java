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

package me.lucko.scriptcontroller.environment.registry;

import me.lucko.scriptcontroller.environment.script.Script;

import java.nio.file.Path;
import java.util.Map;

/**
 * A registry of {@link Script}s
 */
public interface ScriptRegistry extends AutoCloseable {

    static ScriptRegistry create() {
        return new ScriptRegistryImpl();
    }

    /**
     * Registers a script
     *
     * @param script the script to register
     */
    void register(Script script);

    /**
     * Unregisters a script
     *
     * @param script the script to unregister
     */
    void unregister(Script script);

    /**
     * Gets a script by path
     *
     * @param path the path
     * @return a script for the path, or null
     */
    Script getScript(Path path);

    /**
     * Gets all scripts known to this registry
     *
     * @return the scripts
     */
    Map<Path, Script> getAll();

    @Override
    void close();
}
