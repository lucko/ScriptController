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

import me.lucko.scriptcontroller.closable.CompositeAutoClosable;
import me.lucko.scriptcontroller.environment.loader.EnvironmentScriptLoader;
import me.lucko.scriptcontroller.environment.registry.ScriptRegistry;
import me.lucko.scriptcontroller.environment.script.Script;
import me.lucko.scriptcontroller.logging.SystemLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ScriptLoaderImpl implements EnvironmentScriptLoader {
    private static final WatchEvent.Kind<?>[] EVENTS = new WatchEvent.Kind[]{
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
    };

    /** The environment this loader is operating within */
    private final ScriptEnvironmentImpl environment;

    /** The watch service monitoring the directory */
    private final WatchService watchService;

    /** The watch key for the script directory */
    private final List<WatchKey> watchKeys = new CopyOnWriteArrayList<>();

    /**
     * The script files currently being monitored by this instance.
     * These paths are relative to the script directory
     */
    private List<Path> files = new ArrayList<>();

    /** The instance mutex */
    private final ReentrantLock lock = new ReentrantLock();

    public ScriptLoaderImpl(ScriptEnvironmentImpl environment) throws IOException {
        this.environment = environment;

        // init file watcher
        this.watchService = environment.getDirectory().getFileSystem().newWatchService();
        try (Stream<Path> dirs = Files.walk(environment.getDirectory())) {
            List<Path> directories = dirs.filter(Files::isDirectory).collect(Collectors.toList());
            for (Path dir : directories) {
                this.watchKeys.add(dir.register(this.watchService, EVENTS));
            }
        }
    }

    @Override
    public ScriptEnvironmentImpl getEnvironment() {
        return this.environment;
    }

    @Override
    public void watchAll(Collection<String> paths) {
        this.lock.lock();
        try {
            for (String s : paths) {
                this.files.add(Paths.get(s));
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void unwatchAll(Collection<String> paths) {
        this.lock.lock();
        try {
            for (String s : paths) {
                this.files.remove(Paths.get(s));
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void preload() {
        // keep running until we stop loading files
        int filesLength;
        do {
            filesLength = this.files.size();
            reload(true);
        } while (filesLength != this.files.size());
    }

    @Override
    public void run() {
        this.lock.lock();
        try {
            reload(false);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            this.lock.unlock();
        }
    }

    private void reload(boolean runImmediately) {
        ScriptRegistry registry = this.environment.getScriptRegistry();
        SystemLogger logger = this.environment.getController().getLogger();

        // gather work
        Set<Path> toReload = new LinkedHashSet<>();
        Set<Path> toLoad = new LinkedHashSet<>();
        Set<Script> toUnload = new LinkedHashSet<>();

        checkWatched(toLoad, toUnload);
        checkRegistry(toUnload);
        checkFilesystem(toLoad, toUnload, toReload);

        // handle reloading first
        // create a reload queue - by taking the paths to reload, and then
        // recursively looking for anything which depends on them
        Set<Path> reloadQueue = new LinkedHashSet<>();
        for (Path p : toReload) {
            resolveDepends(reloadQueue, p);
        }

        // a set of scripts to terminate at the end of this cycle
        Set<Script> toTerminate = new HashSet<>();
        // a set of scripts to run at the end of this cycle
        Set<ScriptImpl> toRun = new HashSet<>();

        // process the reload queue before unloads or loads
        for (Path path : reloadQueue) {
            Script oldScript = registry.getScript(path);
            if (oldScript == null) {
                continue;
            }

            // since we're creating a new script instance, we need to schedule an unload for the old one.
            toTerminate.add(oldScript);

            // init a new script instance
            ScriptImpl newScript = new ScriptImpl(this, path);
            registry.register(newScript);
            toRun.add(newScript);

            logger.info("[LOADER] Reloaded script: " + pathToString(path));
        }

        // then handle loads
        for (Path path : toLoad) {
            // double check the script isn't loaded already.
            if (registry.getScript(path) != null) {
                continue;
            }

            // init a new script instance & register it
            ScriptImpl script = new ScriptImpl(this, path);
            registry.register(script);
            toRun.add(script);

            logger.info("[LOADER] Loaded script: " + pathToString(path));
        }

        // then handle unloads
        for (Script s : toUnload) {
            registry.unregister(s);
            toTerminate.add(s);
            logger.info("[LOADER] Unloaded script: " + pathToString(s.getPath()));
        }

        if (toTerminate.isEmpty() && toRun.isEmpty()) {
            return;
        }

        // handle init of new scripts & cleanup of old ones
        Executor runExecutor = runImmediately ? Runnable::run : this.environment.getSettings().getRunExecutor();
        runExecutor.execute(() -> {
            // terminate old scripts
            CompositeAutoClosable.create()
                    .bindAll(toTerminate)
                    .closeAndReportExceptions();

            // init new/reloaded scripts
            for (ScriptImpl script : toRun) {
                try {
                    script.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkWatched(Set<Path> toLoad, Set<Script> toUnload) {
        Path directory = this.environment.getDirectory();
        ScriptRegistry registry = this.environment.getScriptRegistry();

        // handle scripts being watched
        // effectively: ensure that for all files being watched, if the file
        // exists it's loaded. (this check covers new scripts being watched at runtime)
        // additionally, ensure that watched scripts still exist, otherwise unload them.
        for (Path path : this.files) {
            Script script = registry.getScript(path);

            if (Files.exists(directory.resolve(path))) {
                // if the path exists, make sure we have something loaded for it
                if (script == null) {
                    toLoad.add(path);
                }
            } else {
                // path doesn't exist (the script has been deleted?), so make sure the script isn't loaded.
                if (script != null) {
                    toUnload.add(script);
                }
            }
        }
    }

    private void checkRegistry(Set<Script> toUnload) {
        ScriptRegistry registry = this.environment.getScriptRegistry();

        // unload scripts which are in the registry, but were unwatched since the last check
        for (Map.Entry<Path, Script> script : registry.getAll().entrySet()) {
            if (!this.files.contains(script.getKey())) {
                toUnload.add(script.getValue());
            }
        }
    }

    private void checkFilesystem(Set<Path> toLoad, Set<Script> toUnload, Set<Path> toReload) {
        Path directory = this.environment.getDirectory();
        ScriptRegistry registry = this.environment.getScriptRegistry();
        SystemLogger logger = this.environment.getController().getLogger();

        // a set of paths which we're going to 'try' to unload.
        // meaning, they'll only get unloaded if we also aren't (re)loading in this same cycle
        Set<Path> tryUnload = new HashSet<>();

        // poll the filesystem for changes
        Iterator<WatchKey> keys = this.watchKeys.iterator();
        while (keys.hasNext()) {
            WatchKey key = keys.next();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path context = (Path) event.context();
                if (context == null) {
                    continue;
                }

                Path keyPath = (Path) key.watchable();
                Path fullPath = keyPath.resolve(context);
                Path relativePath = directory.relativize(fullPath);

                if (Files.isDirectory(fullPath) && !fullPath.getFileName().toString().equals("New folder")) {
                    logger.info("[LOADER] New directory detected at: " + relativePath.toString());
                }

                // already being loaded / unloaded
                // soo, just ignore the change
                if (toLoad.contains(relativePath) || toUnload.stream().anyMatch(s -> s.getPath().equals(relativePath))) {
                    continue;
                }

                // try delete
                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    tryUnload.add(relativePath);
                    continue;
                }

                // otherwise, try (re)load
                Script script = registry.getScript(relativePath);
                if (script == null) {
                    if (this.files.contains(relativePath)) {
                        toLoad.add(relativePath);
                    } else {
                        // add to the reload queue anyways - we want to resolve it's dependencies
                        toReload.add(relativePath);
                    }
                } else {
                    toReload.add(script.getPath());
                }
            }

            // check if key is still valid
            boolean valid = key.reset();
            if (!valid) {
                logger.warning("[LOADER] Watch key is no longer valid: " + key.watchable().toString());
                keys.remove();
            }
        }

        // process scripts which might need to be unloaded.
        for (Path p : tryUnload) {
            Script script = registry.getScript(p);

            // only unload if the script exists
            if (script == null) {
                continue;
            }

            // only unload if the script isn't otherwise being loaded
            if (toLoad.contains(p) || toReload.contains(p)) {
                continue;
            }

            toUnload.add(script);
        }
    }

    /**
     * Recursively finds dependencies on a given path.
     *
     * @param accumulator the path accumulator
     * @param path the start path
     */
    private void resolveDepends(Set<Path> accumulator, Path path) {
        if (!accumulator.add(path)) {
            return;
        }

        for (Script other : this.environment.getScriptRegistry().getAll().values()) {
            if (other.getDependencies().contains(path)) {
                resolveDepends(accumulator, other.getPath());
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.watchKeys.clear();
        this.watchService.close();
        this.files.clear();
    }

    private static String pathToString(Path path) {
        return path.toString().replace("\\", "/");
    }

}
