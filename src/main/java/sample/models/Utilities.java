package sample.models;

import sample.models.exceptions.InvalidNoteException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.control.Button;

public final class Utilities {
    private Utilities() {
    }

    /**
     * number of keys in an 88 Key keyboard.
     */
    public static final int NUMBER_OF_KEYS_88 = 88;

    /**
     * number of unaltered Notes.
     */
    public static final int NUMBER_OF_UNALTERED_NOTES = 8;

    /**
     * Comparator for Notes.
     */
    public static final Comparator<Note> NOTE_COMPARATOR = new Comparator<>() {
        @Override
        public int compare(final Note note1, final Note note2) {
            int note1Num = note1.getName().charAt(note1.getName().length() - 1);
            int note2Num = note2.getName().charAt(note2.getName().length() - 1);
            if (note1Num != note2Num) {
                return note1Num - note2Num;
            }
            return NOTE_QUALITIES.indexOf(note1.noteQuality())
                    - NOTE_QUALITIES.indexOf(note2.noteQuality());
        }
    };

    /**
     * Comparator for Button representations
     * of Notes.
     */
    public static final Comparator<Button> KEY_NOTE_COMPARATOR = (o1, o2) -> {
        Note note1 = null;
        Note note2 = null;
        try {
            note1 = new Note(o1.getTooltip().getText());
            note2 = new Note(o2.getTooltip().getText());
        } catch (InvalidNoteException e) {
            e.printStackTrace();
        }
        return NOTE_COMPARATOR.compare(note1, note2);
    };

    /**
     * list containing all possible Note qualities.
     */
    public static final ArrayList<String> NOTE_QUALITIES = new ArrayList<>(
            List.of(
                    "A",
                    "Bb",
                    "B",
                    "C",
                    "Db",
                    "D",
                    "Eb",
                    "E",
                    "F",
                    "Gb",
                    "G",
                    "Ab"));

    /**
     * list containing all possible Note names
     * in order.
     */
    public static final LinkedList<String> NOTE_NAMES = generateNames();

    private static LinkedList<String> generateNames() {
        LinkedList<String> list = new LinkedList<>();
        for (int i = 1; i < NUMBER_OF_UNALTERED_NOTES; i++) {
            for (int j = 0; j < NOTE_QUALITIES.size(); j++) {
                String str = NOTE_QUALITIES.get(j) + i;
                list.add(str);
            }
        }
        list.add("A8");
        list.add("Bb8");
        list.add("B8");
        list.add("C8");
        return list;
    }
}
