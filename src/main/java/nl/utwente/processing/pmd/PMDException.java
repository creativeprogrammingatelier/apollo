package nl.utwente.processing.pmd;

import java.util.List;

/** Something went wrong while running PMD */
public class PMDException extends Exception {
    private List<Throwable> causes;

    public PMDException(Throwable ex) {
        super(ex);
        causes = List.of(ex);
    }

    public PMDException(List<Throwable> exs) {
        super("Failed with " + exs.size() + " exceptions.", exs.isEmpty() ? null : exs.get(0));
        causes = exs;
    }

    public List<Throwable> getCauses() {
        return causes;
    }

    public void printStackTraces() {
        this.printStackTrace();
        for (Throwable ex : causes) {
            ex.printStackTrace();
        }
    }
}
