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

import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.loader.SystemScriptLoader;
import me.lucko.scriptcontroller.environment.registry.ScriptRegistry;
import me.lucko.scriptcontroller.exports.ExportRegistry;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.io.IOException;
import java.nio.file.Path;

import javax.script.ScriptEngine;

class ScriptEnvironmentImpl implements ScriptEnvironment {

    /** The script controller */
    private final ScriptControllerImpl controller;

    /** The environment settings */
    private final EnvironmentSettingsImpl settings;

    /** The root directory of this environment */
    private final Path directory;

    /** The script registry for scripts loaded in this environment */
    private final ScriptRegistry scriptRegistry;

    /** The script export registry */
    private final ExportRegistry exportRegistry;

    /** The script engine */
    private final ScriptEngine scriptEngine;

    /** The script loader operating within this environment */
    private final SystemScriptLoader loader;

    /** An autoclosable which represents the repeating load task */
    private final AutoCloseable loaderPollingTask;

    public ScriptEnvironmentImpl(ScriptControllerImpl controller, Path directory, EnvironmentSettingsImpl settings) {
        this.controller = controller;
        this.directory = directory;
        this.settings = settings;

        this.scriptRegistry = ScriptRegistry.create();
        this.exportRegistry = ExportRegistry.create();
        this.scriptEngine = new NashornScriptEngineFactory().getScriptEngine(ScriptEnvironmentImpl.class.getClassLoader());
        try {
            this.loader = new ScriptLoaderImpl(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.loader.watch(settings.getInitScript());
        this.loader.preload();

        // setup a ticking task on the environments loader
        Duration rate = settings.getPollRate();
        this.loaderPollingTask = settings.getLoadExecutor().scheduleAtFixedRate(this.loader, rate.getDuration(), rate.getUnit());
    }

    @Override
    public ScriptControllerImpl getController() {
        return this.controller;
    }

    @Override
    public EnvironmentSettingsImpl getSettings() {
        return this.settings;
    }

    @Override
    public Path getDirectory() {
        return this.directory;
    }

    @Override
    public SystemScriptLoader getLoader() {
        return this.loader;
    }

    public ScriptEngine getScriptEngine() {
        return this.scriptEngine;
    }

    @Override
    public ScriptRegistry getScriptRegistry() {
        return this.scriptRegistry;
    }

    @Override
    public ExportRegistry getExportRegistry() {
        return this.exportRegistry;
    }

    @Override
    public void close() throws Exception {
        this.loaderPollingTask.close();
        this.loader.close();
        this.scriptRegistry.close();
    }
}
