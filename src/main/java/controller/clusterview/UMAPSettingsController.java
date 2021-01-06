package controller.clusterview;

import exceptions.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import mediator.ControllerMediator;
import org.json.JSONObject;
import persistence.SessionMaker;

import java.net.URL;
import java.util.ResourceBundle;

public class UMAPSettingsController implements Initializable {
    private static float DEFAULT_MIN_DIST = 0.5f;
    private static int DEFAULT_NEAREST_NEIGHBORS = 15;

    @FXML private TextField minDistField;
    @FXML private TextField nearestNeighborsField;

    // settings that have been saved
    private float savedMinDist;
    private int savedNearestNeighbors;
    // settings in the fields
    private float tempMinDist;
    private int tempNearestNeighbors;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpMinDistField();
        setUpNearestNeighborsField();
        saveSettings();
    }

    public float getMinDist() {
        return savedMinDist;
    }

    public int getNearestNeighbors() {
        return savedNearestNeighbors;
    }

    public void saveSettings() {
        savedMinDist = tempMinDist;
        savedNearestNeighbors = tempNearestNeighbors;
    }

    public void restoreSettingsToSaved() {
        setTempMinDist(savedMinDist);
        setTempNearestNeighbors(savedNearestNeighbors);
    }

    public void setSettingsToDefault() {
        setTempMinDist(DEFAULT_MIN_DIST);
        setTempNearestNeighbors(DEFAULT_NEAREST_NEIGHBORS);
        saveSettings();
    }

    public void restoreSettingsFromPrevSession(JSONObject prevSession) {
        setTempMinDist(prevSession.getFloat(SessionMaker.MIN_DIST_KEY));
        setTempNearestNeighbors(prevSession.getInt(SessionMaker.NEAREST_NEIGHBORS_KEY));
        saveSettings();
    }

    @FXML
    protected void handleChangedMinDist() {
        try {
            updateMinDist();
        } catch (RNAScoopException e) {
            minDistField.setText(String.valueOf(tempMinDist));
            e.addToMessage(". Changed min distance back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    @FXML
    protected void handleChangedNearestNeighbors() {
        try {
            updateNearestNeighbors();
        } catch (RNAScoopException e) {
            nearestNeighborsField.setText(String.valueOf(tempNearestNeighbors));
            e.addToMessage(". Changed number of nearest neighbors back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    private void setTempMinDist(float tempMinDist) {
        this.tempMinDist = tempMinDist;
        minDistField.setText(Float.toString(tempMinDist));
    }

    private void setTempNearestNeighbors(int tempNearestNeighbors) {
        this.tempNearestNeighbors = tempNearestNeighbors;
        nearestNeighborsField.setText(Integer.toString(tempNearestNeighbors));
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

        tempMinDist = newMinDist;
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

        tempNearestNeighbors = newNearestNeighbors;
    }

    private void setUpMinDistField() {
        tempMinDist = DEFAULT_MIN_DIST;
        minDistField.setText(String.valueOf(DEFAULT_MIN_DIST));
        minDistField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedMinDist();
            }
        });
    }

    private void setUpNearestNeighborsField() {
        tempNearestNeighbors = DEFAULT_NEAREST_NEIGHBORS;
        nearestNeighborsField.setText(String.valueOf(DEFAULT_NEAREST_NEIGHBORS));
        nearestNeighborsField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedNearestNeighbors();
            }
        });
    }
}
