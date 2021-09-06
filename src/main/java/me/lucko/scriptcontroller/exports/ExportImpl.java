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

package me.lucko.scriptcontroller.exports;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Atomic implementation of {@link Export}.
 *
 * @param <T> the type
 */
final class ExportImpl<T> implements Export<T> {
    private final String name;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private T value = null;
    private Pointer<T> pointer = null;

    ExportImpl(String name) {
        this.name = name;
    }

    private Lock readLock() { return this.lock.readLock(); }
    private Lock writeLock() { return this.lock.writeLock(); }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public synchronized Pointer<T> pointer() {
        if (this.pointer == null) {
            this.pointer = new PointerImpl<>(this);
        }
        return this.pointer;
    }

    @Override
    public T get() {
        readLock().lock();
        try {
            return this.value;
        } finally {
            readLock().unlock();
        }
    }

    @Override
    public T get(T other) {
        T value = get();
        return value != null ? value : other;
    }

    @Override
    public Export<T> put(T value) {
        writeLock().lock();
        try {
            this.value = value;
        } finally {
            writeLock().unlock();
        }
        return this;
    }

    @Override
    public Export<T> putIfAbsent(T value) {
        writeLock().lock();
        try {
            if (this.value == null) {
                this.value = value;
            }
        } finally {
            writeLock().unlock();
        }
        return this;
    }

    @Override
    public Export<T> computeIfAbsent(Supplier<? extends T> other) {
        writeLock().lock();
        try {
            if (this.value == null) {
                this.value = other.get();
            }
        } finally {
            writeLock().unlock();
        }
        return this;
    }

    @Override
    public boolean containsValue() {
        readLock().lock();
        try {
            return this.value != null;
        } finally {
            readLock().unlock();
        }
    }

    @Override
    public void clear() {
        writeLock().lock();
        try {
            this.value = null;
        } finally {
            writeLock().unlock();
        }
    }

    private static final class PointerImpl<T> implements Pointer<T> {
        private final Export<T> export;

        private PointerImpl(Export<T> export) {
            this.export = export;
        }

        @Override
        public T get() {
            return this.export.get();
        }
    }

}
