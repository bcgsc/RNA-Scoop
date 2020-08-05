package controller.clusterview;

import exceptions.InvalidMaxIterationsException;
import exceptions.InvalidPerplexityException;
import exceptions.RNAScoopException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import mediator.ControllerMediator;

import java.net.URL;
import java.util.ResourceBundle;

public class TSNESettingsController implements Initializable {
    private double DEFAULT_PERPLEXITY = 20;
    private int DEFAULT_MAX_ITERATIONS = 1000;
    @FXML private TextField perplexityField;
    @FXML private TextField maxIterationsField;

    private double perplexity;
    private int maxIterations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpPerplexityField();
        setUpMaxIterationsField();
    }

    public double getPerplexity() {
        return perplexity;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    @FXML
    private void handleChangedPerplexity() {
        try {
            updatePerplexity();
        } catch (RNAScoopException e) {
            perplexityField.setText(String.valueOf(perplexity));
            e.addToMessage(". Changed perplexity back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    @FXML
    private void handleChangedMaxIterations() {
        try {
            updateMaxIterations();
        } catch (RNAScoopException e) {
            maxIterationsField.setText(String.valueOf(maxIterations));
            e.addToMessage(". Changed max iterations back to previous value");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
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

        perplexity = newPerplexity;
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

        maxIterations = newMaxIterations;
    }

    private void setUpPerplexityField() {
        perplexity = DEFAULT_PERPLEXITY;
        perplexityField.setText(String.valueOf(DEFAULT_PERPLEXITY));
        perplexityField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedPerplexity();
            }
        });
    }

    private void setUpMaxIterationsField() {
        maxIterations = DEFAULT_MAX_ITERATIONS;
        maxIterationsField.setText(String.valueOf(DEFAULT_MAX_ITERATIONS));
        maxIterationsField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedMaxIterations();
            }
        });
    }
}
