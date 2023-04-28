package sample.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import sample.models.FillerButton;
import sample.models.Note;
import sample.models.Utilities;
import sample.models.exceptions.InvalidNoteException;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Synthesizer;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sample.models.NotesNamingMode.FLAT_MODE;
import static sample.models.NotesNamingMode.SHARP_MODE;
import static sample.views.Styles.whiteKeysReleasedCss;
import static sample.views.Styles.whiteKeysPressedCss;
import static sample.views.Styles.blackKeysReleasedCss;
import static sample.views.Styles.blackKeysPressedCSs;

/**
 * GUI for the full size view of a midi piano
 * allowing for free play without audio feedback.
 */
public final class FreePlayWindow extends Application {
    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = Logger.getLogger(FreePlayWindow.class.getName());
    /**
     * Window configuration.
     */
    private final FreePlayWindowConfig freePlayWindowConfig = FreePlayWindowConfig.fullWidthConfig();

    private static int numKeys = 88;

    Map<Integer, String> numKeysToStartKey = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(88, "A0"),
            new AbstractMap.SimpleEntry<>(76, "C1"),
            new AbstractMap.SimpleEntry<>(61, "C2"),
            new AbstractMap.SimpleEntry<>(49, "C2")
    );

    /**
     * Array of Button representing the keys on the piano.
     */
    private final Button[] keyBoard = new Button[numKeys];

    /**
     * List of white keys on the piano in order.
     */
    private final LinkedList<Button> whiteKeys = new LinkedList<>();

    /**
     * List of black keys on the piano in order.
     */
    private final LinkedList<Button> blackKeys = new LinkedList<>();

    HBox blackKeyPane = new HBox();

    HBox whiteKeyPane = new HBox();

    BorderPane root = new BorderPane();

    /**
     * Top Level Menu Bar.
     */
    private final CommonMenu menu = new CommonMenu(true);

    /**
     * Default Button style to help
     * reset buttons to original look.
     */
    private static final String BUTTON_ORIGINAL_STYLE = new Button().getStyle();

    /**
     * Midi Receiver to capture keyboard / midi events.
     */
    private final MidiInputReceiver midiInputReceiver =
            new MidiInputReceiver("Receiver");

    /**
     * Index of the last key pressed
     * as captured from a physical midi device.
     */
    private int lastKeyPressedIndex = 0;

    private MidiChannel midiChannel;

    /**
     * Home button to return to the Main View.
     */
    private final Button homButton = new Button("Home");

    private final MenuItem keyboard88 = new MenuItem("88 Keys");
    private final MenuItem keyboard76 = new MenuItem("76 Keys");
    private final MenuItem keyboard61 = new MenuItem("61 Keys (NEEDS WORK)");
    private final MenuItem keyboard49 = new MenuItem("49 Keys (NEEDS WORK)");

    /**
     * Button to show all Note names on the piano.
     */
    private final ToggleButton showNotesButton = new ToggleButton("Show Notes Names");

    private void switchNotesNamingMode() {

        for (Button button: keyBoard) {
            String existingTooltip = button.getTooltip().getText();
            try {
                button.setTooltip(new Tooltip(new Note(existingTooltip).getName()));
            } catch (InvalidNoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initializes the components of the UI.
     */
    private void initialize() {

        final int firstKeyIndex = Utilities.NOTE_NAMES_FLAT.indexOf(numKeysToStartKey.get(numKeys));
        List<Integer> blackIndex = List.of(1, 4, 6, 9, 11);

        final boolean isFlatMode = Note.notesNamingMode == FLAT_MODE;
        for (int i = 0; i < numKeys; i++) {
            String noteName = isFlatMode ?
                    Utilities.NOTE_NAMES_FLAT.get(i + firstKeyIndex)
                    :
                    Utilities.NOTE_NAMES_SHARP.get(i + firstKeyIndex);
            Button button = new Button(noteName);
            if(blackIndex.contains(i % 12)){
                blackKeys.add(button);
            }else{
                whiteKeys.add(button);
            }
            keyBoard[i] = button;
        }

        for (Button button : keyBoard) {
            button.setOnMousePressed(mouseEvent
                    ->
                    button.setStyle("-fx-background-color: gray"));
            button.setOnMouseReleased(mouseEvent -> {
                if (blackKeys.contains(button)) {
                    button.setStyle("-fx-background-color: black");
                } else if (whiteKeys.contains(button)) {
                    button.setStyle(BUTTON_ORIGINAL_STYLE);
                }
            });
        }

        populatedKeyPanes();
    }

    private void populatedKeyPanes() {
        for (Button button : whiteKeys) {
            button.setTooltip(new Tooltip(button.getText()));
            button.setText("");
            button.setPrefSize(freePlayWindowConfig.getWhiteKeysPrefWidth(), freePlayWindowConfig.getWhiteKeysPrefHeight());
            button.setPadding(new Insets(0, 0, 0, 0));
            button.setOnMousePressed(event -> {
                button.setStyle(whiteKeysPressedCss);
                button.setTranslateY(2);
            });

            button.setOnMouseReleased(event -> {
                button.setStyle(whiteKeysReleasedCss);
                button.setTranslateY(0);
            });

            whiteKeyPane.getChildren().add(button);
        }

        for (Button button : blackKeys) {
            button.setTooltip(new Tooltip(button.getText()));
            button.setText("");
            button.setPrefSize(freePlayWindowConfig.getBlackKeysPrefWidth(), freePlayWindowConfig.getBlackKeysPrefHeight());
            button.setPadding(new Insets(0, freePlayWindowConfig.getBlackKeysPaddingRight(), 0, 0));
            button.setOnMousePressed(event -> {
                button.setStyle(blackKeysPressedCSs);
                button.setPrefSize(freePlayWindowConfig.getBlackKeysPrefWidth(), freePlayWindowConfig.getBlackKeysPrefHeight() + 2);
            });

            button.setOnMouseReleased(event -> {
                button.setStyle(blackKeysReleasedCss);
                button.setPrefSize(freePlayWindowConfig.getBlackKeysPrefWidth(), freePlayWindowConfig.getBlackKeysPrefHeight());
            });
            blackKeyPane.getChildren().add(button);
        }
        FillerButton filler = new FillerButton((int) freePlayWindowConfig.getBlackKeysPrefWidth(), (int) freePlayWindowConfig.getBlackKeysPrefHeight());
        filler.setPadding(new Insets(0, freePlayWindowConfig.getBlackKeysPaddingRight(), 0, 0));
        blackKeyPane.getChildren().add(1, filler);
        int i = switch (numKeys) {
            case 88,76 -> 1;
            case 61,49 -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + numKeys);
        };
        while (i < blackKeyPane.getChildren().size() - 1) {
            FillerButton fillerButton1 = new FillerButton(freePlayWindowConfig.getBlackKeysPrefWidth(), freePlayWindowConfig.getBlackKeysPrefHeight());
            FillerButton fillerButton2 = new FillerButton(freePlayWindowConfig.getBlackKeysPrefWidth(), freePlayWindowConfig.getBlackKeysPrefHeight());
            fillerButton1.setPadding(new Insets(0, freePlayWindowConfig.getBlackKeysPaddingRight(), 0, 0));
            fillerButton2.setPadding(new Insets(0, freePlayWindowConfig.getBlackKeysPaddingRight(), 0, 0));
            blackKeyPane.getChildren().add(i + 3, fillerButton1);
            blackKeyPane.getChildren().add(i + 7, fillerButton2);
            i = i + 7;
        }
        for (Button button: blackKeys) {
            button.setStyle(blackKeysReleasedCss);
        }
        for (Button button: whiteKeys) {
            button.setStyle(whiteKeysReleasedCss);
        }
    }

    private void resizeKeyboard(final int numKeys) {
        FreePlayWindow.numKeys = numKeys;
        blackKeyPane.getChildren().clear();
        whiteKeyPane.getChildren().clear();
        whiteKeys.clear();
        blackKeys.clear();

        initialize();
    }

    /**
     * This function assigns the text-note name on the button that is pressed.
     * On white keys it's simple because they are big. On the black keys the text displays
     * vertically so that the user can view the whole note name. It is called via show_notes action
     * listener
     */
    private void displayNotesNames() {
        for (Button button : whiteKeys) {
            String text = button.getTooltip().getText();
            text = text.substring(0, text.length() - 1);
            button.setText(text);
            button.setAlignment(Pos.BOTTOM_CENTER);
        }

        for (Button button : blackKeys) {
            String text = button.getTooltip().getText();
            text = text.substring(0, text.length() - 1);
            button.setFont(new Font("aerials", 8));
            button.setText(text);
            button.setTextFill(Color.WHITE);
            button.setAlignment(Pos.BOTTOM_CENTER);
        }
    }


    /**
     * Opens all the midi transmitters available in
     * the system.
     */
    private void openAllTransmitters() {
        Vector<MidiDevice.Info> synthInfos = new Vector<>();
        MidiDevice device = null;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
            if (device instanceof Synthesizer) {
                synthInfos.add(info);
            }
        }
        for (MidiDevice.Info info : synthInfos) {
            menu.selectMidiInput.getItems().add(new MenuItem(info.getName() + " --- " +info.getDescription()));
        }
    }

    /**
     * Closes all the open midi transmitters available in the system.
     */
    private void closeAllTransmitters() {
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                if (device.isOpen()) {
                    device.close();
                    LOGGER.info(device.getDeviceInfo() + " Was closed");
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args application parameters
     */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage freePlay) {
        initialize();
        openAllTransmitters();

        menu.flatModeItem.setOnAction(e -> {
            Note.notesNamingMode = FLAT_MODE;
            switchNotesNamingMode();
        });
        menu.sharpModeItem.setOnAction(e -> {
            Note.notesNamingMode = SHARP_MODE;
            switchNotesNamingMode();
        });

        Menu keyboardSize = new Menu("Keyboard Size");
        keyboardSize.getItems().addAll(keyboard88,keyboard76, keyboard61, keyboard49);
        keyboard49.setOnAction(e -> resizeKeyboard(49));
        keyboard61.setOnAction(e -> resizeKeyboard(61));
        keyboard76.setOnAction(e -> resizeKeyboard(76));
        keyboard88.setOnAction(e -> resizeKeyboard(88));
        menu.getMenus().add(keyboardSize);

        root.setStyle("-fx-background-color: #E6BF83");

        whiteKeyPane.setPickOnBounds(false);
        whiteKeyPane.setSpacing(FreePlayWindowConfig.WHITE_KEY_PANE_SPACING);

        blackKeyPane.setPickOnBounds(false);
        blackKeyPane.setPadding(new Insets(0, 0, 0, freePlayWindowConfig.getBlackKeyPaneLeftPadding()));
        blackKeyPane.setSpacing(freePlayWindowConfig.getBlackKeyPaneSpacing());

        // Pane that will contain show_notes ToggleButton and home button
        BorderPane bottomPane = new BorderPane();

        // Hbox inside which we put show_notes and home button
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.getChildren().add(showNotesButton);
        buttons.getChildren().add(homButton);
        // Assign buttons Hbox on the right side of bottomPane
        buttons.setAlignment(Pos.CENTER);
        bottomPane.setCenter(buttons);
        bottomPane.setBackground(Background.fill(Color.TRANSPARENT));
        bottomPane.setPadding(new Insets(0, 0, 20, 0));
        homButton.setVisible(true);
        showNotesButton.setVisible(true);

        // add menu bar
        bottomPane.setTop(menu);
        root.setBottom(bottomPane);

        GridPane keyPane = new GridPane();
        keyPane.setAlignment(Pos.CENTER);
        keyPane.add(whiteKeyPane, 0, 0, 2, 1);
        keyPane.add(blackKeyPane, 0, 0, 2, 1);
        root.setCenter(keyPane);

        Scene scene = new Scene(root, freePlayWindowConfig.getWindowWidth(), freePlayWindowConfig.getWindowHeight());
        freePlay.setFullScreen(false);
        freePlay.setResizable(false);
        freePlay.setTitle("Free Play");
        freePlay.setScene(scene);
        freePlay.show();

        homButton.setOnAction(e -> {
            Main mainWindow = new Main();
            try {
                mainWindow.start(new Stage());
                freePlay.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        freePlay.setOnCloseRequest(windowEvent -> {
            Main mainWindow = new Main();
            try {
                mainWindow.start(new Stage());
                freePlay.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            closeAllTransmitters();
            midiInputReceiver.close();
        });

        showNotesButton.setOnMouseClicked(e -> {
            if (showNotesButton.isSelected()) {
                displayNotesNames();
            } else {
                for (Button button : whiteKeys) {
                    button.setText("");
                }

                for (Button button : blackKeys) {
                    button.setText("");
                }
            }
        });
    }

    /**
     * Event handler for midi keyboard events.
     * Assigns the appropriate style to the keyboard keys involved
     * @param key
     */
    private void keyPressedReleased(final int key) {
        Button button = keyBoard[key];
        if (button.getStyle().contains("blue")
                || button.getStyle().contains("red")) {
            if (whiteKeys.contains(button)) {
                button.setStyle(BUTTON_ORIGINAL_STYLE);
            } else if (blackKeys.contains(button)) {
                button.setStyle("-fx-background-color: black");
            }
        } else {
            if (whiteKeys.contains(button)) {
                button.setStyle("-fx-background-color: blue");
            } else if (blackKeys.contains(button)) {
                button.setStyle("-fx-background-color: red");
            }
        }
    }

    /**
     * Class to handle midi events.
     */
    public final class MidiInputReceiver implements Receiver {

        /**
         * name of the receiver.
         */
        private final String name;

        /**
         * Construct a receiver with the String argument as name.
         * @param receiverName name of receiver
         */
        public MidiInputReceiver(final String receiverName) {
            this.name = receiverName;
        }

        /**
         * @param msg the MIDI message to send
         * @param timeStamp the time-stamp for the message, in microseconds
         */
        public void send(final MidiMessage msg, final long timeStamp) {
            byte[] aMsg = msg.getMessage();
            if ((lastKeyPressedIndex == 127 && aMsg[2] == 0)
                    || aMsg[2] == 127) {
                lastKeyPressedIndex = aMsg[2];
                return;
            }
            LOGGER.info("Message: " + aMsg[0] + ", " + aMsg[1] + ", " + aMsg[2]);
            if (aMsg[1] - 21 < 0) {
                return;
            }
            keyPressedReleased(aMsg[1] - 21);
            lastKeyPressedIndex = aMsg[2];
        }

        @Override
        public void close() {
            closeAllTransmitters();
        }
    }
}
