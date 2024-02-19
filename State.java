import java.util.*;

// Helper class to store CNF clauses and their assignments
public class State {
    List<String[]> sentences;
    Map<String, String> assignment;

    // Constructor call
    public State(List<String[]> sentences, Map<String, String> assignment) {
        this.sentences = sentences;
        this.assignment = assignment;
    }

    // Getters
    public List<String[]> getSentences() {
        return this.sentences;
    }
    public Map<String, String> getAssignment() {
        return this.assignment;
    }

    // Debug code sentences
    public void printSentences() {
        for (String[] a : sentences) {
            for (String b : a) {
                System.out.print(b + " ");
            }
            System.out.println();
        }
    }

    // Saves the clauses that are TRUE after DPLL for further mapping
    public void save(List<String> result) {
        for (Map.Entry<String, String> entry : this.getAssignment().entrySet()) {
            if (entry.getValue().equals("true")) result.add(entry.getKey());
        }
    }
    public String toString() {
        return this.sentences + "\n" + this.assignment;
    }
}