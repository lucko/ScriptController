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

package me.lucko.scriptcontroller;

import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.settings.EnvironmentSettings;
import me.lucko.scriptcontroller.internal.ScriptControllerImpl;
import me.lucko.scriptcontroller.logging.SystemLogger;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Controls the execution and management of {@link ScriptEnvironment}s.
 */
public interface ScriptController {

    /**
     * Creates a new {@link Builder} using the internal implementation.
     *
     * @return the builder
     */
    static Builder builder() {
        //noinspection deprecation
        return ScriptControllerImpl.builder();
    }

    /**
     * Gets the {@link ScriptEnvironment}s being processed by the controller
     *
     * @return the environments
     */
    Collection<ScriptEnvironment> getEnvironments();

    /**
     * Sets up a new {@link ScriptEnvironment} in the given load directory.
     *
     * @param loadDirectory the directory
     * @param settings the environment settings
     * @return the new environment
     * @throws UnsupportedOperationException if the controller does not support
     * setting up new environments after construction
     */
    ScriptEnvironment setupNewEnvironment(Path loadDirectory, EnvironmentSettings settings);

    /**
     * Sets up a new {@link ScriptEnvironment} in the given load directory.
     *
     * @param loadDirectory the directory
     * @return the new environment
     * @throws UnsupportedOperationException if the controller does not support
     * setting up new environments after construction
     */
    default ScriptEnvironment setupNewEnvironment(Path loadDirectory) {
        return setupNewEnvironment(loadDirectory, EnvironmentSettings.defaults());
    }

    /**
     * Shuts down this script controller
     */
    void shutdown();

    /**
     * Builds a {@link ScriptController}
     */
    interface Builder {

        /**
         * Add a directory to be handled by this script controller
         *
         * @param loadDirectory the directory
         * @return this builder
         */
        Builder withDirectory(Path loadDirectory);

        /**
         * Defines the logger to use.
         *
         * @param logger the logger
         * @return this builder
         */
        Builder logger(SystemLogger logger);

        /**
         * Defines the default {@link EnvironmentSettings} to use when this
         * controller creates new {@link ScriptEnvironment}s.
         *
         * @param settings the default settings
         * @return this builder
         */
        Builder defaultEnvironmentSettings(EnvironmentSettings settings);

        /**
         * Builds a new {@link ScriptController} from the settings defined in
         * this builder
         *
         * @return a new controller
         */
        ScriptController build();

    }

}
