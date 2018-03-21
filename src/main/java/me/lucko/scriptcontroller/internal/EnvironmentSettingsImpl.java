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

package me.lucko.scriptcontroller.internal;

import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.environment.loader.ScriptLoadingExecutor;
import me.lucko.scriptcontroller.environment.settings.EnvironmentSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class EnvironmentSettingsImpl implements EnvironmentSettings {
    private static final Supplier<ScriptLoadingExecutor> DEFAULT_LOAD_EXECUTOR = () -> ScriptLoadingExecutor.usingJavaScheduler(Executors.newSingleThreadScheduledExecutor());
    private static final Executor DEFAULT_RUN_EXECUTOR = Runnable::run;
    private static final Duration DEFAULT_POLL_RATE = new Duration(1, TimeUnit.SECONDS);
    private static final String DEFAULT_INIT_SCRIPT = "init.js";

    private static final EnvironmentSettings DEFAULT = builder().build();

    static EnvironmentSettings defaults() {
        return DEFAULT;
    }

    static EnvironmentSettings.Builder builder() {
        return new Builder();
    }

    private final ScriptLoadingExecutor loadExecutor;
    private final Executor runExecutor;
    private final Set<BindingsSupplier> bindings;
    private final Duration pollRate;
    private final String initScript;

    private EnvironmentSettingsImpl(Builder builder) {
        this.pollRate = builder.pollRate;
        this.loadExecutor = builder.loadExecutor;
        this.runExecutor = builder.runExecutor;
        this.bindings = Collections.unmodifiableSet(new HashSet<>(builder.bindings));
        this.initScript = builder.initScript;
    }

    public ScriptLoadingExecutor getLoadExecutor() {
        if (this.loadExecutor == null) {
            return DEFAULT_LOAD_EXECUTOR.get();
        }
        return this.loadExecutor;
    }

    public Executor getRunExecutor() {
        if (this.runExecutor == null) {
            return DEFAULT_RUN_EXECUTOR;
        }
        return this.runExecutor;
    }

    public Set<BindingsSupplier> getBindings() {
        return this.bindings;
    }

    public Duration getPollRate() {
        if (this.pollRate == null) {
            return DEFAULT_POLL_RATE;
        }
        return this.pollRate;
    }

    public String getInitScript() {
        if (this.initScript == null) {
            return DEFAULT_INIT_SCRIPT;
        }
        return this.initScript;
    }

    private static final class Builder implements EnvironmentSettings.Builder {
        private ScriptLoadingExecutor loadExecutor = null;
        private Executor runExecutor = null;
        private final Set<BindingsSupplier> bindings = new HashSet<>();
        private Duration pollRate = null;
        private String initScript = null;

        @Override
        public Builder mergeSettingsFrom(EnvironmentSettings other) {
            Objects.requireNonNull(other, "other");
            EnvironmentSettingsImpl that = (EnvironmentSettingsImpl) other;

            if (that.loadExecutor != null) {
                this.loadExecutor = that.loadExecutor;
            }
            if (that.runExecutor != null) {
                this.runExecutor = that.runExecutor;
            }
            this.bindings.addAll(that.bindings);
            if (that.pollRate != null) {
                this.pollRate = that.pollRate;
            }
            return this;
        }

        @Override
        public Builder loadExecutor(ScriptLoadingExecutor executor) {
            Objects.requireNonNull(executor, "executor");
            this.loadExecutor = executor;
            return this;
        }

        @Override
        public Builder runExecutor(Executor executor) {
            this.runExecutor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        @Override
        public Builder withBindings(BindingsSupplier supplier) {
            this.bindings.add(Objects.requireNonNull(supplier, "supplier"));
            return this;
        }

        @Override
        public Builder pollRate(long time, TimeUnit unit) {
            this.pollRate = new Duration(time, Objects.requireNonNull(unit, "unit"));
            return this;
        }

        @Override
        public EnvironmentSettings.Builder initScript(String path) {
            this.initScript = Objects.requireNonNull(path, "path");
            return this;
        }

        @Override
        public EnvironmentSettings build() {
            return new EnvironmentSettingsImpl(this);
        }
    }
}
