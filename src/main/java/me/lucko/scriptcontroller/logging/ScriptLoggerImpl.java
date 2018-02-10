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

import me.lucko.scriptcontroller.environment.script.Script;

final class ScriptLoggerImpl implements ScriptLogger {
    private static final String FORMAT = "[%s]%s";
    private final SystemLogger logger;
    private final Script script;

    public ScriptLoggerImpl(SystemLogger logger, Script script) {
        this.logger = logger;
        this.script = script;
    }

    @Override
    public void info(Object... message) {
        this.logger.info(formatLog(message));
    }

    @Override
    public void warn(Object... message) {
        this.logger.warning(formatLog(message));
    }

    @Override
    public void error(Object... message) {
        this.logger.severe(formatLog(message));
    }

    private String formatLog(Object... message) {
        return String.format(FORMAT, this.script.getName(), format(message));
    }

    private static String format(Object[] message) {
        if (message == null || message.length == 0) {
            return " ";
        } else if (message.length == 1) {
            return " " + String.valueOf(message[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Object o : message) {
                sb.append(" ").append(String.valueOf(o));
            }
            return sb.toString();
        }
    }
}
