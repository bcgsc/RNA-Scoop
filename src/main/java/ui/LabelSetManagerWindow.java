package ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;

/**
 * Window which displays the Label Set Manager pop up
 * (Label Set Manager has custom window to allow for changing between views)
 */
public class LabelSetManagerWindow extends Stage {
    private static final float LABEL_SET_MANAGER_SCALE_FACTOR = 0.33f;

    private Scene mainViewScene;
    private Scene addLabelSetViewScene;
    private Scene viewDisplayed;

    /**
     * Creates window to hold Label Set Manager pop up
     * Window is always on top, and can display either main view, or view which
     * allows users to add and customize a new label set
     * Makes it so window is hidden when X button is pressed, if "Add Label Set" view
     * is currently being displayed, deletes the label set the user was adding, and switches
     * the display to the main view
     */
    public LabelSetManagerWindow(Parent mainView, Parent addLabelSetView) {
        setTitle("RNA-Scoop - Cluster Label Set Manager");
        getIcons().add(Main.RNA_SCOOP_LOGO);
        setAlwaysOnTop(true);
        setUpViews(mainView, addLabelSetView);
        setOnCloseRequest(event -> {
            if (viewDisplayed == addLabelSetViewScene) {
                ControllerMediator.getInstance().removeLabelSet(ControllerMediator.getInstance().getLabelSetInUse());
                displayMainView();
            }
            event.consume();
            hide();
        });
    }

    public void displayMainView() {
        setScene(mainViewScene);
        viewDisplayed = mainViewScene;
    }

    public void displayAddLabelSetView() {
        ControllerMediator.getInstance().prepareAddLabelSetViewForDisplay();
        setScene(addLabelSetViewScene);
        viewDisplayed = addLabelSetViewScene;
    }

    /**
     * Makes scenes for main view and "Add Label Set" view, which window can switch
     * between
     * Displays main view
     */
    private void setUpViews(Parent mainView, Parent addLabelSetView) {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        double windowWidth = screen.getWidth() * LABEL_SET_MANAGER_SCALE_FACTOR;
        double windowHeight = screen.getHeight() * LABEL_SET_MANAGER_SCALE_FACTOR;
        mainViewScene = new Scene(mainView, windowWidth, windowHeight);
        addLabelSetViewScene = new Scene(addLabelSetView, windowWidth, windowHeight);
        displayMainView();
    }
}
