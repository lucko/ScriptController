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

package me.lucko.scriptcontroller.internal;

import me.lucko.scriptcontroller.ScriptController;
import me.lucko.scriptcontroller.closable.CompositeAutoClosable;
import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.settings.EnvironmentSettings;
import me.lucko.scriptcontroller.logging.SystemLogger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class ScriptControllerImpl implements ScriptController {

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static ScriptController.Builder builder() {
        return new Builder();
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static EnvironmentSettings defaultSettings() {
        return EnvironmentSettingsImpl.defaults();
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static EnvironmentSettings.Builder newSettingsBuilder() {
        return EnvironmentSettingsImpl.builder();
    }

    /**
     * The sub environments originating from this controller
     */
    private final Map<Path, ScriptEnvironment> environments = new HashMap<>();

    // various settings and properties defined when the controller was created.
    private final SystemLogger logger;
    private final EnvironmentSettings defaultSettings;

    private ScriptControllerImpl(Builder builder) {
        this.logger = builder.logger.get();
        this.defaultSettings = builder.settings;

        // setup the initial environments
        for (Path path : builder.directories) {
            //noinspection ResultOfMethodCallIgnored
            setupNewEnvironment(path);
        }
    }

    @Override
    public Collection<ScriptEnvironment> getEnvironments() {
        return Collections.unmodifiableCollection(this.environments.values());
    }

    @Override
    public synchronized ScriptEnvironment setupNewEnvironment(Path loadDirectory, EnvironmentSettings settings) {
        if (this.environments.containsKey(loadDirectory)) {
            throw new IllegalStateException("Already an environment setup at path " + loadDirectory.toString());
        }

        // merge the provided setting with out defaults
        EnvironmentSettings mergedSettings = this.defaultSettings.toBuilder().mergeSettingsFrom(settings).build();

        // create a new environment
        ScriptEnvironmentImpl environment = new ScriptEnvironmentImpl(this, loadDirectory, (EnvironmentSettingsImpl) mergedSettings);
        // store a ref to the new environment in the controller
        this.environments.put(loadDirectory, environment);
        return environment;
    }

    @Override
    public void shutdown() {
        CompositeAutoClosable.create()
                .bindAll(this.environments.values())
                .closeAndReportExceptions();
    }

    SystemLogger getLogger() {
        return this.logger;
    }

    private static final class Builder implements ScriptController.Builder {
        private final Set<Path> directories = new HashSet<>();
        private Supplier<SystemLogger> logger = FallbackSystemLogger.INSTANCE;
        private EnvironmentSettings settings = EnvironmentSettings.defaults();

        @Override
        public Builder withDirectory(Path loadDirectory) {
            this.directories.add(Objects.requireNonNull(loadDirectory, "loadDirectory"));
            return this;
        }

        @Override
        public Builder logger(SystemLogger logger) {
            Objects.requireNonNull(logger, "logger");
            this.logger = () -> logger;
            return this;
        }

        @Override
        public Builder defaultEnvironmentSettings(EnvironmentSettings settings) {
            this.settings = Objects.requireNonNull(settings, "settings");
            return this;
        }

        @Override
        public ScriptController build() {
            return new ScriptControllerImpl(this);
        }
    }

}
