package com.patex.zombie.opds.service;

import com.patex.zombie.opds.entity.ExtLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Component
public class ExtLibInScopeRunner {

    private final ExtLibScopeStorage scopeStorage;

    @Autowired
    public ExtLibInScopeRunner(ExtLibScopeStorage scopeStorage) {
        this.scopeStorage = scopeStorage;
    }

    public <T> T runInScope(ExtLibrary extLibrary, Supplier<T> supplier) {
        try {
            scopeStorage.setScope(extLibrary);
            return supplier.get();
        } finally {
            scopeStorage.clearScope();
        }
    }

    public <T> void runInScope(ExtLibrary extLibrary, Runnable runnable) {
        try {
            scopeStorage.setScope(extLibrary);
            runnable.run();
        } finally {
            scopeStorage.clearScope();
        }
    }

    public <T> Future<T> runInSameScope(ExecutorService executor, Supplier<T> supplier) {
        try {
            ExtLibrary current = scopeStorage.getCurrentExtLib();
            Callable<T> task = () -> runInScope(current, supplier);
            return executor.submit(task);
        } finally {
            scopeStorage.clearScope();
        }
    }

    public <T> void runInSameScope(ExecutorService executor, Runnable runnable) {
        try {
            ExtLibrary current = scopeStorage.getCurrentExtLib();
            executor.execute(() -> runInScope(current, runnable));
        } finally {
            scopeStorage.clearScope();
        }
    }
}
