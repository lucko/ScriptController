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

package me.lucko.scriptcontroller.environment.script;

import me.lucko.scriptcontroller.logging.ScriptLogger;

import java.nio.file.Path;
import java.util.Set;

/**
 * Represents an individual script
 */
public interface Script extends AutoCloseable {

    /**
     * Gets the name of the script, usually formed from the scripts
     * {@link #getPath() path} {@link Path#getFileName() file name}.
     *
     * @return the name of the script
     */
    String getName();

    /**
     * Gets the path of the script.
     *
     * <p>The path is relative to the loader directory.</p>
     *
     * @return the path
     */
    Path getPath();

    /**
     * Gets the scripts logger instance
     *
     * @return the logger
     */
    ScriptLogger getLogger();

    /**
     * Gets the other scripts depended on by this script.
     *
     * @return this scripts dependencies
     */
    Set<Path> getDependencies();

    /**
     * Marks that this script depends on another script.
     *
     * @param path the other script
     */
    void depend(String path);

    /**
     * Marks that this script depends on another script.
     *
     * @param path the other script
     */
    void depend(Path path);

}
