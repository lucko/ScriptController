/*
 * This file is part of scriptcontroller, licensed under the MIT License.
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

package me.lucko.scriptcontroller.environment.settings;

import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.loader.ScriptLoadingExecutor;
import me.lucko.scriptcontroller.internal.ScriptControllerImpl;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Represents the settings for a given {@link ScriptEnvironment}.
 */
public interface EnvironmentSettings {

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new builder
     */
    static Builder builder() {
        //noinspection deprecation
        return ScriptControllerImpl.newSettingsBuilder();
    }

    /**
     * Returns a default set of environment settings
     *
     * @return the default settings
     */
    static EnvironmentSettings defaults() {
        //noinspection deprecation
        return ScriptControllerImpl.defaultSettings();
    }

    /**
     * Returns a builder encapsulating the properties already defined by this
     * instance
     *
     * @return a builder
     */
    default Builder toBuilder() {
        return builder().mergeSettingsFrom(this);
    }

    /**
     * Builds {@link EnvironmentSettings}
     */
    interface Builder {

        /**
         * Applies the settings from the give instance to this builder
         *
         * @param other the other settings
         * @return this builder
         */
        Builder mergeSettingsFrom(EnvironmentSettings other);

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
         * Adds a bindings supplier to the settings
         *
         * @param supplier the bindings supplier
         * @return this builder
         */
        Builder withBindings(BindingsSupplier supplier);

        /**
         * Marks that a {@link Package} should be imported by default.
         *
         * @param packageName the name of the package - see {@link Package#getName()}.
         * @return this builder
         */
        Builder withDefaultPackageImport(String packageName);

        /**
         * Marks that {@link Package}s should be imported by default.
         *
         * @param packageNames the package names - see {@link Package#getName()}.
         * @return this builder
         */
        Builder withDefaultPackageImports(Collection<String> packageNames);

        /**
         * Marks that a {@link Class} should be imported by default.
         *
         * @param type the name of the class - see {@link Class#getName()}
         * @return this builder
         */
        Builder withDefaultTypeImport(String type);

        /**
         * Marks that {@link Class}es should be imported by default.
         *
         * @param types class names - see {@link Class#getName()}
         * @return this builder
         */
        Builder withDefaultTypeImports(Collection<String> types);

        /**
         * Define how often the script loader should poll scripts for updates
         *
         * @param time the time
         * @param unit the unit
         * @return this builder
         */
        Builder pollRate(long time, TimeUnit unit);

        /**
         * Defines the init script for the environment
         *
         * @param path the path
         * @return this builder
         */
        Builder initScript(String path);

        /**
         * Builds a new {@link EnvironmentSettings} instance.
         *
         * @return the resultant environment settings
         */
        EnvironmentSettings build();

    }

}
