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

import me.lucko.scriptcontroller.bindings.BindingsBuilder;
import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.closable.CompositeAutoClosable;
import me.lucko.scriptcontroller.environment.loader.DelegateScriptLoader;
import me.lucko.scriptcontroller.environment.loader.ScriptLoader;
import me.lucko.scriptcontroller.environment.script.Script;
import me.lucko.scriptcontroller.logging.ScriptLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

class ScriptImpl implements Script, Runnable {

    /**
     * Header which is evaluated before the content of the actual script is
     * loaded. It...
     *
     * Loads the nashorn mozilla_compat script, which provides importClass and
     * importPackage functions (among other things)
     *
     * Redefines the load function to prepend the loader directory and register
     * loaded scripts as dependencies
     */
    private static final String GLOBAL_SCRIPT_HEADER =
            /*
            load("nashorn:mozilla_compat.js");
            var __load = load;
            var load = function(file) {
                __load(rsd + file);
                depend(file);
            };
             */
            "load(\"nashorn:mozilla_compat.js\");\r\n" +
            "var __load = load;\r\n" +
            "var load = function(file) {\r\n" +
            "    __load(rsd + file);\r\n" +
            "    depend(file);\r\n" +
            "};";

    /**
     * Function which returns the string evaluate in order to load a js file at
     * the given path
     */
    private static final Function<Path, String> LOAD_FUNCTION = path -> "__load(\"" + path.toString().replace("\\", "/") + "\");";

    // functions to import packages / classes
    private static final Function<String, String> IMPORT_PACKAGE = pkg -> "importPackage(\"" + pkg + "\");";
    private static final Function<String, String> IMPORT_TYPE = type -> {
        String name = type.substring(type.lastIndexOf('.') + 1);
        return "var " + name + " = Java.type(\"" + type + "\")";
    };

    private final ScriptLoaderImpl loader;

    /** The name of this script */
    private final String name;

    /** The associated script file */
    private final Path path;

    /** The loader instance handling this script */
    private final ScriptLoader delegateLoader;

    /** The scripts logger */
    private final ScriptLogger logger;

    /** The terminable registry used by this script */
    private final CompositeAutoClosable compositeAutoClosable = CompositeAutoClosable.create();

    /** The scripts dependencies */
    private final Set<Path> depends = new HashSet<>();

    public ScriptImpl(ScriptLoaderImpl loader, Path path) {
        this.loader = loader;

        String name = path.getFileName().toString();
        if (name.endsWith(".js")) {
            this.name = name.substring(0, name.length() - 3);
        } else {
            this.name = name;
        }
        this.path = path;

        this.delegateLoader = new DelegateScriptLoader(loader);
        this.logger = ScriptLogger.create(loader.getEnvironment().getController().getLogger(), this);
        this.depends.add(this.path);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public ScriptLogger getLogger() {
        return this.logger;
    }

    @Override
    public CompositeAutoClosable getClosables() {
        return this.compositeAutoClosable;
    }

    @Override
    public void run() {
        try {
            Path loaderDirectory = this.loader.getEnvironment().getDirectory().normalize();
            ScriptEngine scriptEngine = this.loader.getEnvironment().getScriptEngine();

            // create bindings
            BindingsBuilder bindings = BindingsBuilder.wrap(scriptEngine.createBindings());

            // provide an export for various script attributes
            bindings.put("loader", this.delegateLoader)
                    .put("closableRegistry", this.compositeAutoClosable)
                    .put("exports", this.loader.getEnvironment().getExportRegistry())
                    .put("logger", this.logger)
                    .put("cwd", this.path.normalize().toString().replace("\\", "/")) // the path of the script file (current working directory)
                    .put("rsd", loaderDirectory.toString().replace("\\", "/") + "/") // the root scripts directory
                    .put("depend", (Consumer<String>) this::depend); // function to depend on another script

            // accumulate global bindings
            Set<BindingsSupplier> systemBindings = this.loader.getEnvironment().getSettings().getBindings();
            for (BindingsSupplier supplier : systemBindings) {
                supplier.supplyBindings(this, bindings);
            }

            // create a new script context, and attach our bindings
            ScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings.build(), ScriptContext.ENGINE_SCOPE);

            // evaluate the header
            scriptEngine.eval(GLOBAL_SCRIPT_HEADER, context);

            // apply default package/type imports
            for (String packageName : this.loader.getEnvironment().getSettings().getPackageImports()) {
                scriptEngine.eval(IMPORT_PACKAGE.apply(packageName), context);
            }
            for (String className : this.loader.getEnvironment().getSettings().getTypeImports()) {
                scriptEngine.eval(IMPORT_TYPE.apply(className), context);
            }

            // resolve the load path, relative to the loader directory.
            Path loadPath = loaderDirectory.resolve(this.path);
            scriptEngine.eval(LOAD_FUNCTION.apply(loadPath), context);
        } catch (Throwable t) {
            this.logger.error("Exception occurred whilst loading script (" + this.path + ")");
            t.printStackTrace();
        }
    }

    @Override
    public Set<Path> getDependencies() {
        return Collections.unmodifiableSet(this.depends);
    }

    @Override
    public void depend(String path) {
        depend(Paths.get(path));
    }

    @Override
    public void depend(Path path) {
        if (this.path.equals(path)) {
            return;
        }

        this.depends.add(path);
    }

    @Override
    public void close() throws Exception {
        this.delegateLoader.close();
        this.compositeAutoClosable.close();
    }
}
