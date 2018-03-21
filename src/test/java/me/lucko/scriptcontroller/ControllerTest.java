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

import com.google.common.io.Resources;

import me.lucko.scriptcontroller.bindings.BindingsSupplier;
import me.lucko.scriptcontroller.environment.ScriptEnvironment;
import me.lucko.scriptcontroller.environment.settings.EnvironmentSettings;
import me.lucko.scriptcontroller.exports.ExportRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ControllerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        File scripts = this.folder.newFolder("scripts");
        copyResource(getClass().getResource("/test.js"), scripts, "init.js");

        AtomicBoolean callback = new AtomicBoolean(false);

        ScriptController controller = ScriptController.builder()
                .withDirectory(scripts.toPath())
                .defaultEnvironmentSettings(EnvironmentSettings.builder()
                        .withBindings(BindingsSupplier.singleBinding("testCallback", (Runnable) () -> callback.set(true)))
                        .build()
                )
                .build();

        assertTrue(callback.get());

        Collection<ScriptEnvironment> environments = controller.getEnvironments();
        assertEquals(1, environments.size());

        ScriptEnvironment environment = environments.iterator().next();

        ExportRegistry exports = environment.getExportRegistry();
        assertEquals("Hello world", exports.get("test").get());

        controller.shutdown();
    }

    private static void copyResource(URL in, File out, String name) throws IOException {
        File outFile = new File(out, name);
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            Resources.copy(in, outputStream);
        }
    }

}
