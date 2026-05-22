package org.talias.bmf.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DbExecutor {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DbExecutor() {
    }

    public static void run(Runnable task) {
        executor.execute(task);
    }
}
