package controller.clusterview;

import exceptions.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import mediator.ControllerMediator;

import java.net.URL;
import java.util.ResourceBundle;

public class UMAPSettingsController implements Initializable {
    private static float DEFAULT_MIN_DIST = 0.5f;
    private static int DEFAULT_NEAREST_NEIGHBORS = 15;

    @FXML private TextField minDistField;
    @FXML private TextField nearestNeighborsField;

    private float minDist;
    private int nearestNeighbors;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpMinDistField();
        setUpNearestNeighborsField();
    }

    public float getMinDist() {
        return minDist;
    }

    public int getNearestNeighbors() {
        return nearestNeighbors;
    }

    @FXML
    private void handleChangedMinDist() {
        try {
            updateMinDist();
        } catch (RNAScoopException e) {
            minDistField.setText(String.valueOf(minDist));
            e.addToMessage(". Changed min distance back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    @FXML
    private void handleChangedNearestNeighbors() {
        try {
            updateNearestNeighbors();
        } catch (RNAScoopException e) {
            nearestNeighborsField.setText(String.valueOf(nearestNeighbors));
            e.addToMessage(". Changed number of nearest neighbors back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    private void updateMinDist() throws InvalidMinDistException{
        float newMinDist;

        try {
            newMinDist = Float.parseFloat(minDistField.getText());
        } catch (NumberFormatException e) {
            throw new InvalidMinDistException();
        }

        if (newMinDist < 0 || newMinDist > 1)
            throw new InvalidMinDistException();

        minDist = newMinDist;
    }

    private void updateNearestNeighbors() throws InvalidNearestNeighborsException{
        int newNearestNeighbors;

        try {
            newNearestNeighbors = Integer.parseInt(nearestNeighborsField.getText());
        } catch (NumberFormatException e) {
            throw new InvalidNearestNeighborsException();
        }

        if (newNearestNeighbors < 2)
            throw new InvalidNearestNeighborsException();

        nearestNeighbors = newNearestNeighbors;
    }

    private void setUpMinDistField() {
        minDist = DEFAULT_MIN_DIST;
        minDistField.setText(String.valueOf(DEFAULT_MIN_DIST));
        minDistField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedMinDist();
            }
        });
    }

    private void setUpNearestNeighborsField() {
        nearestNeighbors = DEFAULT_NEAREST_NEIGHBORS;
        nearestNeighborsField.setText(String.valueOf(DEFAULT_NEAREST_NEIGHBORS));
        nearestNeighborsField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedNearestNeighbors();
            }
        });
    }
}
