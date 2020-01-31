package ui.fxml.main.controllers;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class tSNEPlotController implements Initializable {
    @FXML private VBox tSNEPlot;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode canvas;

    private JPanel pane;

    public VBox getTSNEPlot() {
        return tSNEPlot;
    }

    /**
     * When isoform plot toggle is pressed, toggles visibility of the isoform plot
     */
    @FXML
    protected void drawTSNE(ActionEvent e) {
        System.out.println("TODO");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pane = new JPanel(new GridLayout(1, 2));
        pane.setPreferredSize(new Dimension(5, 500));
        canvas.setContent(pane);
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0,0,0.68f)));
    }
}
