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

package me.lucko.scriptcontroller.environment.loader;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simplified scheduler for {@link ScriptLoader}s.
 */
public interface ScriptLoadingExecutor extends Executor {

    /**
     * Creates a {@link ScriptLoadingExecutor} using a java executor service
     *
     * @param service the service
     * @return a new loading scheduler
     */
    static ScriptLoadingExecutor usingJavaScheduler(ScheduledExecutorService service) {
        Objects.requireNonNull(service, "service");
        return new ScriptLoadingExecutor() {
            @Override
            public AutoCloseable scheduleAtFixedRate(Runnable task, long time, TimeUnit unit) {
                ScheduledFuture<?> future = service.scheduleAtFixedRate(task, 0L, time, unit);
                return () -> future.cancel(false);
            }

            @Override
            public void execute(Runnable command) {
                service.execute(command);
            }
        };
    }

    /**
     * Schedules a task to run at a fixed rate, with the first execution
     * occurring with no delay.
     *
     * @param task the task
     * @param time the time
     * @param unit the unit of the time
     * @return an {@link AutoCloseable}, which will cancel the task when
     *         {@link AutoCloseable#close() closed}.
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    AutoCloseable scheduleAtFixedRate(Runnable task, long time, TimeUnit unit);

}
