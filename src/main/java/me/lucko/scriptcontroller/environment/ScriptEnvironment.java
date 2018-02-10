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

package me.lucko.scriptcontroller.environment;

import me.lucko.scriptcontroller.ScriptController;
import me.lucko.scriptcontroller.environment.loader.SystemScriptLoader;
import me.lucko.scriptcontroller.environment.registry.ScriptRegistry;
import me.lucko.scriptcontroller.exports.ExportRegistry;

import java.nio.file.Path;

/**
 * Represents an isolated environment in which scripts run
 *
 * Each environment operates within a given root {@link Path directory}, under a
 * {@link ScriptController}.
 */
public interface ScriptEnvironment extends AutoCloseable {

    /**
     * Gets the script controller which created this environment
     *
     * @return the parent controller
     */
    ScriptController getController();

    /**
     * Gets the environments root scripts directory
     *
     * @return the root directory of this environment
     */
    Path getDirectory();

    /**
     * Gets the script loader used by this environment.
     *
     * <p>Each environment has it's own loader.</p>
     *
     * @return the script loader
     */
    SystemScriptLoader getLoader();

    /**
     * Gets the script registry, containing all loaded scripts within this
     * environment
     *
     * @return the script registry
     */
    ScriptRegistry getScriptRegistry();

    /**
     * Gets the export registry for this environment
     *
     * @return the export registry
     */
    ExportRegistry getExportRegistry();

}
