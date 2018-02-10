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

import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.loader.ScriptLoadingExecutor;
import me.lucko.scriptcontroller.internal.ScriptControllerImpl;
import me.lucko.scriptcontroller.logging.SystemLogger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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
     * @return the new environment
     * @throws UnsupportedOperationException if the controller does not support
     * setting up new environments after construction
     */
    ScriptEnvironment setupNewEnvironment(Path loadDirectory);

    /**
     * Shuts down this script controller
     */
    void shutdown();

    /**
     * Builds a {@link ScriptController}
     */
    interface Builder {

        /**
         * Define the executor service used to setup task to poll scripts for
         * changes and load new scripts.
         *
         * @param executor the executor
         * @return this builder
         */
        Builder loadExecutor(ScriptLoadingExecutor executor);

        /**
         * Define the executor used to run scripts
         *
         * @param executor the executor
         * @return this builder
         */
        Builder runExecutor(Executor executor);

        /**
         * Add a directory to be handled by this script controller
         *
         * @param loadDirectory the directory
         * @return this builder
         */
        Builder withDirectory(Path loadDirectory);

        /**
         * Adds a bindings supplier to the controller
         *
         * @param supplier the bindings supplier
         * @return this builder
         */
        Builder withBindings(BindingsSupplier supplier);

        /**
         * Define how often the script loader should poll scripts for updates
         *
         * @param time the time
         * @param unit the unit
         * @return this builder
         */
        Builder pollRate(long time, TimeUnit unit);

        /**
         * Defines the logger to use.
         *
         * @param logger the logger
         * @return this builder
         */
        Builder logger(SystemLogger logger);

        /**
         * Builds a new {@link ScriptController} from the settings defined in
         * this builder
         *
         * @return a new controller
         */
        ScriptController build();

    }

}
