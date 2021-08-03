package util;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import mediator.ControllerMediator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class containing useful helper methods
 */
public class Util {
    /**
     * Puts given collection into sorted list
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static double roundToOneDecimal (double value) {
        int scale = 10;
        return (double) Math.round(value * scale) / scale;
    }

    public static void setFieldDragNDrop(TextField node) {
        setNodeDragOver(node);
        setTextFieldDragDropped(node);
    }

    public static void setFieldDragNDrop(ComboBox node) {
        setNodeDragOver(node);
        setTextFieldDragDropped(node);
    }

    private static void setNodeDragOver(Node node) {
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    private static void setTextFieldDragDropped(TextField node) {
        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            node.requestFocus();
            if (db.getFiles().size() > 1)
                ControllerMediator.getInstance().addConsoleErrorMessage("You cannot load more than one file at a time");
            else
                node.setText(db.getFiles().get(0).getAbsolutePath());
            event.consume();
        });
    }

    private static void setTextFieldDragDropped(ComboBox node) {
        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            node.requestFocus();
            if (db.getFiles().size() > 1)
                ControllerMediator.getInstance().addConsoleErrorMessage("You cannot load more than one file at a time");
            else
                node.setValue(db.getFiles().get(0).getAbsolutePath());
            event.consume();
        });
    }
}
