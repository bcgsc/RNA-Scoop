package controller.clusterview;

import exceptions.InvalidMaxIterationsException;
import exceptions.InvalidPerplexityException;
import exceptions.RNAScoopException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import mediator.ControllerMediator;
import org.json.JSONObject;
import persistence.SessionMaker;

import java.net.URL;
import java.util.ResourceBundle;

public class TSNESettingsController implements Initializable {
    private double DEFAULT_PERPLEXITY = 20;
    private int DEFAULT_MAX_ITERATIONS = 1000;
    @FXML private TextField perplexityField;
    @FXML private TextField maxIterationsField;

    // settings that have been saved
    private double savedPerplexity;
    private int savedMaxIterations;
    // settings in the fields
    private double tempPerplexity;
    private int tempMaxIterations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpPerplexityField();
        setUpMaxIterationsField();
        saveSettings();
    }

    public double getPerplexity() {
        return savedPerplexity;
    }

    public int getMaxIterations() {
        return savedMaxIterations;
    }

    public void saveSettings() {
        savedPerplexity = tempPerplexity;
        savedMaxIterations = tempMaxIterations;
    }

    public void restoreSettingsToSaved() {
        setTempPerplexity(savedPerplexity);
        setTempMaxIterations(savedMaxIterations);
    }

    public void setSettingsToDefault() {
        setTempPerplexity(DEFAULT_PERPLEXITY);
        setTempMaxIterations(DEFAULT_MAX_ITERATIONS);
        saveSettings();
    }

    public void restoreSettingsFromPrevSession(JSONObject prevSession) {
        setTempPerplexity(prevSession.getDouble(SessionMaker.PERPLEXITY_KEY));
        setTempMaxIterations(prevSession.getInt(SessionMaker.MAX_ITERATIONS_KEY));
        saveSettings();
    }

    @FXML
    protected void handleChangedPerplexity() {
        try {
            updatePerplexity();
        } catch (RNAScoopException e) {
            perplexityField.setText(String.valueOf(tempPerplexity));
            e.addToMessage(". Changed perplexity back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    @FXML
    protected void handleChangedMaxIterations() {
        try {
            updateMaxIterations();
        } catch (RNAScoopException e) {
            maxIterationsField.setText(String.valueOf(tempMaxIterations));
            e.addToMessage(". Changed max iterations back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    private void setTempPerplexity(double tempPerplexity) {
        this.tempPerplexity = tempPerplexity;
        perplexityField.setText(String.valueOf(tempPerplexity));
    }

    private void setTempMaxIterations(int tempMaxIterations) {
        this.tempMaxIterations = tempMaxIterations;
        maxIterationsField.setText(String.valueOf(tempMaxIterations));
    }

    private void updatePerplexity() throws InvalidPerplexityException {
        double newPerplexity;

        try {
            newPerplexity = Double.parseDouble(perplexityField.getText());
        } catch (NumberFormatException e) {
            throw new InvalidPerplexityException();
        }

        if (newPerplexity < 0)
            throw new InvalidPerplexityException();

        tempPerplexity = newPerplexity;
    }

    private void updateMaxIterations() throws InvalidMaxIterationsException {
        int newMaxIterations;

        try {
            newMaxIterations = Integer.parseInt(maxIterationsField.getText());
        } catch (NumberFormatException e) {
            throw new InvalidMaxIterationsException();
        }

        if (newMaxIterations < 0)
            throw new InvalidMaxIterationsException();

        tempMaxIterations = newMaxIterations;
    }

    private void setUpPerplexityField() {
        tempPerplexity = DEFAULT_PERPLEXITY;
        perplexityField.setText(String.valueOf(DEFAULT_PERPLEXITY));
        perplexityField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedPerplexity();
            }
        });
    }

    private void setUpMaxIterationsField() {
        tempMaxIterations = DEFAULT_MAX_ITERATIONS;
        maxIterationsField.setText(String.valueOf(DEFAULT_MAX_ITERATIONS));
        maxIterationsField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedMaxIterations();
            }
        });
    }
}
