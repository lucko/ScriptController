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
import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.closable.CompositeAutoClosable;
import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.loader.ScriptLoadingExecutor;
import me.lucko.scriptcontroller.logging.SystemLogger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class ScriptControllerImpl implements ScriptController {

    public static ScriptController.Builder builder() {
        return new Builder();
    }

    /**
     * The sub environments originating from this controller
     */
    private final Map<Path, ScriptEnvironment> environments = new HashMap<>();

    // various settings and properties defined when the controller was created.
    private final Interval pollRate;
    private final ScriptLoadingExecutor loadExecutor;
    private final Executor runExecutor;
    private final SystemLogger logger;
    private final Set<BindingsSupplier> bindings;

    private ScriptControllerImpl(Builder builder) {
        this.pollRate = builder.pollRate;
        this.loadExecutor = builder.loadExecutor.get();
        this.runExecutor = builder.runExecutor;
        this.logger = builder.logger.get();
        this.bindings = Collections.unmodifiableSet(new HashSet<>(builder.bindings));

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
    public synchronized ScriptEnvironment setupNewEnvironment(Path loadDirectory) {
        if (this.environments.containsKey(loadDirectory)) {
            throw new IllegalStateException("Already an environment setup at path " + loadDirectory.toString());
        }

        // create a new environment
        ScriptEnvironmentImpl environment = new ScriptEnvironmentImpl(this, loadDirectory);
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

    AutoCloseable schedulePollingTask(Runnable runnable) {
        // setup a ticking task on the environments loader
        return this.loadExecutor.scheduleAtFixedRate(runnable, this.pollRate.time, this.pollRate.unit);
    }

    Executor getRunExecutor() {
        return this.runExecutor;
    }

    SystemLogger getLogger() {
        return this.logger;
    }

    Set<BindingsSupplier> getBindings() {
        return this.bindings;
    }

    private static final class Interval {
        private final long time;
        private final TimeUnit unit;

        private Interval(long time, TimeUnit unit) {
            this.time = time;
            this.unit = unit;
        }
    }

    private static final class Builder implements ScriptController.Builder {
        private Supplier<ScriptLoadingExecutor> loadExecutor = () -> ScriptLoadingExecutor.usingJavaScheduler(Executors.newSingleThreadScheduledExecutor());
        private Executor runExecutor = Runnable::run;
        private final Set<Path> directories = new HashSet<>();
        private final Set<BindingsSupplier> bindings = new HashSet<>();
        private Interval pollRate = new Interval(1, TimeUnit.SECONDS);
        private Supplier<SystemLogger> logger = FallbackSystemLogger.INSTANCE;

        @Override
        public Builder loadExecutor(ScriptLoadingExecutor executor) {
            Objects.requireNonNull(executor, "executor");
            this.loadExecutor = () -> executor;
            return this;
        }

        @Override
        public Builder runExecutor(Executor executor) {
            this.runExecutor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        @Override
        public Builder withDirectory(Path loadDirectory) {
            this.directories.add(Objects.requireNonNull(loadDirectory, "loadDirectory"));
            return this;
        }

        @Override
        public Builder withBindings(BindingsSupplier supplier) {
            this.bindings.add(Objects.requireNonNull(supplier, "supplier"));
            return this;
        }

        @Override
        public Builder pollRate(long time, TimeUnit unit) {
            this.pollRate = new Interval(time, Objects.requireNonNull(unit, "unit"));
            return this;
        }

        @Override
        public Builder logger(SystemLogger logger) {
            Objects.requireNonNull(logger, "logger");
            this.logger = () -> logger;
            return this;
        }

        @Override
        public ScriptController build() {
            return new ScriptControllerImpl(this);
        }
    }

}
