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

package me.lucko.scriptcontroller.logging;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents a bridge between the platforms logger and a ScriptController.
 */
public interface SystemLogger {

    /**
     * Creates a {@link SystemLogger} using a java logger
     *
     * @param logger the logger
     * @return a new system logger
     */
    static SystemLogger usingJavaLogger(Logger logger) {
        Objects.requireNonNull(logger, "logger");
        return new SystemLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warning(String message) {
                logger.warning(message);
            }

            @Override
            public void severe(String message) {
                logger.severe(message);
            }
        };
    }

    void info(String message);

    void warning(String message);

    void severe(String message);

}
