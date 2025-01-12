package sample.models.chords.generators.ninths;

import sample.models.Note;
import sample.models.exceptions.InvalidNoteException;

/**
 * Utility Class for generating Major 9th Chords.
 */
public final class Major9th {
    /**
     * largest interval in a Major 9th Chord.
     */
    private static final int MAX_INTERVAL = 14;

    /**
     * interval between the root and the third.
     */
    private static final int ROOT_TO_THIRD = 4;

    /**
     * interval between the root Note and the fifth.
     */
    private static final int ROOT_TO_FIFTH = 7;

    /**
     * interval between the root Note and the seventh.
     */
    private static final int ROOT_TO_SEVEN = 11;

    private Major9th() { }

    /**
     * Generate the Notes of the Major 9th Chord rooted
     * at the given Note.
     * @param root root Note of the Chord
     * @return an array of the Notes
     * @throws InvalidNoteException when an invalid Note is encountered
     */
    public static Note[] generateScale(final Note root)
            throws InvalidNoteException {
        return new Note[]{
                root,
                root.sharp(ROOT_TO_THIRD),
                root.sharp(ROOT_TO_FIFTH),
                root.sharp(ROOT_TO_SEVEN),
                root.sharp(MAX_INTERVAL)
        };
    }
}
