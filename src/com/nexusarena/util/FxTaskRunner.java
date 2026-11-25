package com.nexusarena.util;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Chạy các tác vụ truy vấn SQL Server trên luồng nền thông qua JavaFX Task,
 * tự động cập nhật UI trên JavaFX Application Thread khi hoàn tất hoặc gặp lỗi.
 */
public final class FxTaskRunner {

    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread t = new Thread(runnable, "nexus-db-worker");
        t.setDaemon(true);
        return t;
    });

    private FxTaskRunner() {}

    public static <T> void runAsync(
            Callable<T> backgroundWork,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            Node... nodesToDisable) {

        setNodesDisabled(true, nodesToDisable);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundWork.call();
            }
        };

        task.setOnSucceeded(event -> {
            setNodesDisabled(false, nodesToDisable);
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(event -> {
            setNodesDisabled(false, nodesToDisable);
            Throwable ex = task.getException();
            if (onError != null) {
                onError.accept(ex);
            } else {
                showErrorAlert("Lỗi thao tác cơ sở dữ liệu",
                        ex != null ? ex.getMessage() : "Đã xảy ra lỗi không xác định.");
            }
        });

        DB_EXECUTOR.submit(task);
    }

    public static void runAsyncVoid(
            Runnable backgroundWork,
            Runnable onSuccess,
            Consumer<Throwable> onError,
            Node... nodesToDisable) {

        runAsync(() -> {
            backgroundWork.run();
            return null;
        }, ignored -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }, onError, nodesToDisable);
    }

    public static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ql TOurnament - Thông báo");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ql TOurnament - Thông báo");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void setNodesDisabled(boolean disabled, Node... nodes) {
        if (nodes == null) return;
        for (Node node : nodes) {
            if (node != null) {
                node.setDisable(disabled);
            }
        }
    }

    public static void shutdown() {
        DB_EXECUTOR.shutdownNow();
    }
}