package com.tamv.systema.frontend.Utils;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class General {
    public static void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }
    public static <T> void loadEntities(Supplier<List<T>> supplier, Consumer<List<T>> onLoad) {
        new Thread(() -> {
            List<T> entityList = supplier.get();
            if(onLoad != null) {
                Platform.runLater(() -> onLoad.accept(entityList));
            }
        }).start();
    }
}
