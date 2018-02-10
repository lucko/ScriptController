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

package me.lucko.scriptcontroller.environment.registry;

import me.lucko.scriptcontroller.closable.CompositeAutoClosable;
import me.lucko.scriptcontroller.environment.script.Script;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class ScriptRegistryImpl implements ScriptRegistry {
    private final Map<Path, Script> scripts = new HashMap<>();

    @Override
    public void register(Script script) {
        this.scripts.put(script.getPath(), script);
    }

    @Override
    public void unregister(Script script) {
        this.scripts.remove(script.getPath());
    }

    @Override
    public Script getScript(Path path) {
        return this.scripts.get(path);
    }

    @Override
    public Map<Path, Script> getAll() {
        return Collections.unmodifiableMap(this.scripts);
    }

    @Override
    public void close() {
        CompositeAutoClosable.create()
                .bindAll(this.scripts.values())
                .closeAndReportExceptions();
    }

}
