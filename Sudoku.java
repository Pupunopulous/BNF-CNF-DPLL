import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Sudoku {
    // Stores the resulting DPLL statements
    private static final List<String> resultDPLL = new ArrayList<>();

    // Stores the board as CNF clauses
    private static List<String> cnfClauses = new ArrayList<>();
    private static List<String> bnfClauses = new ArrayList<>();

    // Converts the resuting DPLL solution back into a Sudoku board
    public static void parseBack(List<String> result) {
        int[][] sudokuBoard = new int[9][9];
        for (String square : result) {
            String[] currVal = square.split("_");
            int row = -1, col = -1, number = -1;
            for (String val : currVal) {
                int NUM = Integer.parseInt(String.valueOf(val.charAt(1)));
                if (val.charAt(0) == 'r') row = NUM - 1;
                if (val.charAt(0) == 'c') col = NUM - 1;
                if (val.charAt(0) == 'n') number = NUM;
            }
            sudokuBoard[row][col] = number;
        }
        System.out.println("\nThe solution of the Sudoku board is:");
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(sudokuBoard[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Solves the CNF clauses through DPLL
    public static int dpllSolver(State dpll, int verbose, boolean quick) {
        if (dpll.getSentences().size() == 0) {
            for (Map.Entry<String, String> entry : dpll.getAssignment().entrySet()) {
                if (entry.getValue().equals("unbound")) {
                    entry.setValue("false");
                    // Print statements if verbose mode is enabled
                    if (verbose == 1) {
                        System.out.println("unbound " + entry.getKey() + " = " + entry.getValue());
                    }
                }
            }
            dpll.save(resultDPLL);
            parseBack(resultDPLL);
            System.exit(0);
        }

        for (String[] a : dpll.getSentences()) {
            if (a.length == 0) {
                if (verbose == 1) {
                    System.out.println("Contradiction!");
                }
                return -1;
            }
        }

        /* Use for debugging
        if (verbose == 1) {
            dpll.printSentences();
        }
        */

        // Handle unit clauses
        for (int i = 0; i < dpll.getSentences().size(); i++) {
            String[] currString = dpll.getSentences().get(i);
            if (currString.length == 1) {
                ArrayList<String[]> sentences = new ArrayList<>();
                String stringWithoutSign = "";
                Map<String, String> assignment = quick ? new HashMap<>(dpll.getAssignment()) : new TreeMap<>(dpll.getAssignment());
                if (currString[0].charAt(0) == '!') {
                    assignment.replace(currString[0].substring(1), "false");
                    stringWithoutSign = currString[0].substring(1);
                    // Print statements if verbose mode is enabled
                    if (verbose == 1) {
                        System.out.println("easy case (unit clause): " + currString[0].substring(1) + " = " + "false");
                    }
                } else {
                    assignment.replace(currString[0], "true");
                    stringWithoutSign = currString[0];
                    // Print statements if verbose mode is enabled
                    if (verbose == 1) {
                        System.out.println("easy case (unit clause): " + currString[0] + " = " + "true");
                    }
                }

                for (int j = 0; j < dpll.getSentences().size(); j++) {
                    if (containsString(currString[0], dpll.getSentences().get(j))) {
                        continue;
                    } else if (assignment.get(stringWithoutSign).equals("true")) {
                        if (containsString("!" + currString[0], dpll.getSentences().get(j))) {
                            List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                            list.remove("!" + currString[0]);
                            String[] modified = list.toArray(new String[0]);
                            sentences.add(modified);
                        } else {
                            sentences.add(dpll.getSentences().get(j));
                        }
                    } else if (assignment.get(stringWithoutSign).equals("false")) {
                        if (containsString(currString[0].substring(1), dpll.getSentences().get(j)) && (!containsString(currString[0], dpll.getSentences().get(j)))) {
                            List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                            list.remove(currString[0].substring(1));
                            String[] modified = list.toArray(new String[0]);
                            sentences.add(modified);
                        } else {
                            sentences.add(dpll.getSentences().get(j));
                        }
                    }
                }
                State newDpll = new State(sentences, assignment);
                dpllSolver(newDpll, verbose, quick);
                return -1;
            }
        }

        // Handle pure literals
        for (Map.Entry<String, String> entry : dpll.getAssignment().entrySet()) {
            if (entry.getValue().equals("unbound")) {
                int isPureForTrue = 1;
                int isPureForFalse = 1;
                for (int i = 0; i < dpll.getSentences().size(); i++) {
                    if (containsString("!" + entry.getKey(), dpll.getSentences().get(i))) {
                        isPureForTrue = 0;
                    } else if (containsString(entry.getKey(), dpll.getSentences().get(i))) {
                        isPureForFalse = 0;
                    }
                }

                if (isPureForTrue == 1 || isPureForFalse == 1) {
                    String stringWithSign = "";
                    ArrayList<String[]> sentences = new ArrayList<>();
                    Map<String, String> assignment = quick ? new HashMap<>(dpll.getAssignment()) : new TreeMap<>(dpll.getAssignment());
                    if (isPureForTrue == 1) {
                        assignment.replace(entry.getKey(), "true");
                        stringWithSign = entry.getKey();
                        // Print statements if verbose mode is enabled
                        if (verbose == 1) {
                            System.out.println("easy case (pure literal): " + entry.getKey() + " = " + "true");
                        }
                    } else {
                        assignment.replace(entry.getKey(), "false");
                        stringWithSign = "!" + entry.getKey();
                        // Print statements if verbose mode is enabled
                        if (verbose == 1) {
                            System.out.println("easy case (pure literal): " + entry.getKey() + " = " + "false");
                        }
                    }

                    for (int j = 0; j < dpll.getSentences().size(); j++) {
                        if (containsString(stringWithSign, dpll.getSentences().get(j))) {
                            continue;
                        } else if (isPureForTrue == 1) {
                            if (containsString(stringWithSign, dpll.getSentences().get(j))) {
                                List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                                list.remove("!" + stringWithSign);
                                String[] modified = list.toArray(new String[0]);
                                sentences.add(modified);
                            } else {
                                sentences.add(dpll.getSentences().get(j));
                            }
                        } else {
                            if (containsString(stringWithSign.substring(1), dpll.getSentences().get(j)) && (!containsString(stringWithSign, dpll.getSentences().get(j)))) {
                                List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                                list.remove(stringWithSign.substring(1));
                                String[] modified = list.toArray(new String[0]);
                                sentences.add(modified);
                            } else {
                                sentences.add(dpll.getSentences().get(j));
                            }
                        }
                    }
                    State newDpll = new State(sentences, assignment);
                    dpllSolver(newDpll, verbose, quick);
                    return -1;
                }
            }
        }

        String guessAtom = "";
        ArrayList<String[]> sentencesHard = new ArrayList<>();
        Map<String, String> assignmentHard = quick ? new HashMap<>(dpll.getAssignment()) : new TreeMap<>(dpll.getAssignment());

        for (Map.Entry<String, String> entry : dpll.getAssignment().entrySet()) {
            if (entry.getValue().equals("unbound")) {
                guessAtom = entry.getKey();
                assignmentHard.replace(guessAtom, "true");
                break;
            }
        }
        // Print statements if verbose mode is enabled
        if (verbose == 1) {
            System.out.println("hard case, guess: " + guessAtom + " = " + assignmentHard.get(guessAtom));
        }

        for (int j = 0; j < dpll.getSentences().size(); j++) {
            if (containsString(guessAtom, dpll.getSentences().get(j))) {
                continue;
            } else if (assignmentHard.get(guessAtom).equals("true")) {
                if (containsString("!" + guessAtom, dpll.getSentences().get(j))) {
                    List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                    list.remove("!" + guessAtom);
                    String[] modified = list.toArray(new String[0]);
                    sentencesHard.add(modified);
                } else {
                    sentencesHard.add(dpll.getSentences().get(j));
                }
            }
        }
        State dpllGuessTrue = new State(sentencesHard, assignmentHard);
        dpllSolver(dpllGuessTrue, verbose, quick);

        List<String[]> sentencesHardFalse = new ArrayList<>();
        assignmentHard.replace(guessAtom, "false");
        // Print statements if verbose mode is enabled
        if (verbose == 1) {
            System.out.println("backtrack: hard case, guess: " + guessAtom + " = " + assignmentHard.get(guessAtom));
        }

        for (int j = 0; j < dpll.getSentences().size(); j++) {
            if (containsString("!" + guessAtom, dpll.getSentences().get(j))) {
                continue;
            } else if (assignmentHard.get(guessAtom).equals("false")) {
                if (containsString(guessAtom, dpll.getSentences().get(j)) && (!containsString("!" + guessAtom, dpll.getSentences().get(j)))) {
                    List<String> list = new ArrayList<>(Arrays.asList(dpll.getSentences().get(j)));
                    list.remove(guessAtom);
                    String[] modified = list.toArray(new String[0]);
                    sentencesHardFalse.add(modified);
                } else {
                    sentencesHardFalse.add(dpll.getSentences().get(j));
                }
            }
        }
        State dpllGuessFalse = new State(sentencesHardFalse, assignmentHard);

        // Recursively call solver with new set of statements
        dpllSolver(dpllGuessFalse, verbose, quick);
        return -1;
    }

    // Converts the given board into BNF and CNF clauses
    public static void boardToBNF_CNF(String board) {
        String[] squares = board.split(" ");
        for (String curr : squares) {
            String[] assignment = curr.split("=");
            if ((Integer.parseInt(String.valueOf(assignment[0].charAt(0))) > 9 || Integer.parseInt(String.valueOf(assignment[0].charAt(0))) < 1) ||
                    (Integer.parseInt(String.valueOf(assignment[0].charAt(1))) > 9 || Integer.parseInt(String.valueOf(assignment[0].charAt(1))) < 1) ||
                    (Integer.parseInt(String.valueOf(assignment[1])) > 9 || Integer.parseInt(String.valueOf(assignment[0])) < 1)) {
                System.out.println("Invalid board input. Sudoku values out of range.");
                System.exit(0);
            }
            // Format "n1_r1_c1"
            bnfClauses.add("n" + assignment[1] + "_" + "r" + assignment[0].charAt(0) + "_" + "c" + assignment[0].charAt(1));
        }
        createBNF();
    }

    // Function to create BNF clauses from a given Sudoku board
    public static void createBNF() {
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                StringBuilder bnf = new StringBuilder();
                int n, count;
                for (n = 1; n <= 9; n++) {
                    count = 7;
                    bnf.setLength(0);
                    // Same number cannot exist in another row
                    bnf.append("n").append(n).append("_r").append(r).append("_c").append(c).append(" => !(");
                    for (int otherRow = 1; otherRow <= 9; otherRow++) {
                        if (r != otherRow) {
                            bnf.append("n").append(n).append("_r").append(otherRow).append("_c").append(c);
                            if (count != 0) {
                                bnf.append(" v ");
                                count--;
                            }
                        }
                    }
                    bnf.append(")");
                    bnfClauses.add(bnf.toString());

                    count = 7;
                    bnf.setLength(0);
                    // Same number cannot exist in another column
                    bnf.append("n").append(n).append("_r").append(r).append("_c").append(c).append(" => !(");
                    for (int otherCol = 1; otherCol <= 9; otherCol++) {
                        if (c != otherCol) {
                            bnf.append("n").append(n).append("_r").append(r).append("_c").append(otherCol);
                            if (count != 0) {
                                bnf.append(" v ");
                                count--;
                            }
                        }
                    }
                    bnf.append(")");
                    bnfClauses.add(bnf.toString());

                    int minR, minC;
                    if ((float) r / 3 <= 1) minR = 1;
                    else if ((float) r / 3 <= 2) minR = 4;
                    else minR = 7;
                    if ((float) c / 3 <= 1) minC = 1;
                    else if ((float) c / 3 <= 2) minC = 4;
                    else minC = 7;

                    count = 3;
                    bnf.setLength(0);
                    // Same number cannot exist in the current 3x3 sub-square
                    bnf.append("n").append(n).append("_r").append(r).append("_c").append(c).append(" => !(");
                    for (int i = minR; i < minR + 3; i++)
                        for (int j = minC; j < minC + 3; j++)
                            if (i != r && j != c) {
                                bnf.append("n").append(n).append("_r").append(i).append("_c").append(j);
                                if (count != 0) {
                                    bnf.append(" v ");
                                    count--;
                                }
                            }
                    bnf.append(")");
                    bnfClauses.add(bnf.toString());
                }

                count = 8;
                bnf.setLength(0);
                // 1-9 choices for each box on the board
                for (n = 1; n <= 9; n++) {
                    bnf.append("n").append(n).append("_r").append(r).append("_c").append(c);
                    if (count != 0) {
                        bnf.append(" v ");
                        count--;
                    }
                }
                bnfClauses.add(bnf.toString());
            }
        }
        for (String bnf : bnfClauses) {
            // Convert all BNF clauses to CNF
            cnfClauses.addAll(BnfToCnf.runBNF2CNF(bnf));
        }
    }

    // Helper function to set up clauses by creating "States"
    public static State setUpDpll(List<String> input, boolean quick) {
        List<String> elements = new ArrayList<>();
        for (String value : input) {
            String[] strings = value.trim().split(" ");
            for (String s : strings) {
                if (s.charAt(0) == '!') s = s.substring(1);
                if (!elements.contains(s)) elements.add(s);
            }
        }
        // System.out.println(elements);

        // Set each unit to unbound at first
        Map<String, String> assignment = quick ? new HashMap<>() : new TreeMap<>();
        for (String element : elements) {
            assignment.put(element, "unbound");
        }

        List<String[]> sentences = new ArrayList<>();
        for (String s : input) {
            String[] temp = s.split(" ");
            sentences.add(temp);
        }
        return new State(sentences, assignment);
    }

    // Helper function to check if a list contains a given string
    public static Boolean containsString(String s, String[] list) {
        for (String a : list) {
            if (a.equals(s)) {
                return true;
            }
        }
        return false;
    }

    // Helper function to read a file for BNF to CNF conversion
    public static List<String> readFile(String file) {
        List<String> content = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            // Read each line in the file
            while ((line = reader.readLine()) != null) {
                // Trim whitespace
                line = line.trim();
                content.add(line);
            }
        } catch (Exception e) {
            System.out.println("File not found in current directory.");
            System.exit(0);
        }
        return content;
    }

    // Driver code
    public static void main(String[] args) {
        try {
            int verbose = 0;
            String mode = "solve";
            StringBuilder inputFile = new StringBuilder();
            String BNFFile = "";
            for (int arg = 0; arg < args.length; arg++) {
                if (args[arg].contains(".txt")) {
                    for (String arguments : args) {
                        if (arguments.equals("quick") || arguments.equals("solve")) {
                            System.out.println("Input file can only be specified in CNF mode.");
                            System.exit(0);
                        }
                    }
                    BNFFile = args[arg];
                }
                // Add input clauses rc=v
                if (args[arg].contains("=")) inputFile.append(args[arg]).append(" ");
                // Verbose mode?
                if (args[arg].equals("-v")) verbose = 1;
                // Quick/lexicographic mode
                if (args[arg].equals("-mode")) {
                    if (arg + 1 != args.length) {
                        switch (args[arg + 1]) {
                            case "quick" -> mode = "quick";
                            case "solve" -> mode = "solve";
                            case "bnf" -> mode = "bnf";
                        }
                    }
                }
            }

            // Solve Sudoku according to given input mode (DEFAULT = solve, slower)
            switch (mode) {
                case "quick" -> {
                    BufferedWriter bnfWriter = new BufferedWriter(new FileWriter("BNF_clauses.txt"));
                    BufferedWriter cnfWriter = new BufferedWriter(new FileWriter("CNF_clauses.txt"));
                    // Convert board to BNF clauses
                    boardToBNF_CNF(inputFile.toString());

                    // Solve CNF clauses using DPLL
                    State dpll = setUpDpll(cnfClauses, true);
                    // Write clauses to a file if verbose is enabled
                    if (verbose == 1) {
                        for (String bnf : bnfClauses) {
                            bnfWriter.write(bnf);
                            bnfWriter.newLine();
                            bnfWriter.write(System.lineSeparator());
                        }
                        for (String cnf : cnfClauses) {
                            cnfWriter.write(cnf);
                            cnfWriter.newLine();
                            cnfWriter.write(System.lineSeparator());
                        }
                    }
                    bnfWriter.close();
                    cnfWriter.close();

                    int result = dpllSolver(dpll, verbose, true);
                    if (result == -1) {
                        System.out.println("No Valid Assignment");
                    } else {
                        System.out.println(resultDPLL);
                    }
                }
                case "solve" -> {
                    FileWriter bnfWriter = new FileWriter("BNF_clauses.txt");
                    FileWriter cnfWriter = new FileWriter("CNF_clauses.txt");
                    // Convert board to BNF clauses
                    boardToBNF_CNF(inputFile.toString());

                    // Solve CNF clauses using DPLL
                    State dpll = setUpDpll(cnfClauses, false);
                    // Write clauses to a file if verbose is enabled
                    if (verbose == 1) {
                        for (String bnf : bnfClauses) {
                            bnfWriter.write(bnf);
                            bnfWriter.write(System.lineSeparator());
                        }
                        for (String cnf : cnfClauses) {
                            cnfWriter.write(cnf);
                            cnfWriter.write(System.lineSeparator());
                        }
                    }
                    bnfWriter.close();
                    cnfWriter.close();

                    int result = dpllSolver(dpll, verbose, false);
                    if (result == -1) {
                        System.out.println("No Valid Assignment");
                    } else {
                        System.out.println(resultDPLL);
                    }
                }
                case "bnf" -> {
                    List<String> content = readFile(BNFFile);
                    if (content.isEmpty()) {
                        System.out.println("Input file is empty.");
                        System.exit(0);
                    }
                    List<String> cnfClauses = new ArrayList<>();
                    for (String line : content) {
                        List<String> converted = BnfToCnf.runBNF2CNF(line);
                        cnfClauses.addAll(converted);
                    }
                    System.out.println("CNF clauses:");
                    for (String clause : cnfClauses) System.out.println(clause);
                }
                default -> System.out.println("Argument format error. Please check README file for details.");
            }
        } catch (Exception e) {
            // Command line errors
            System.out.println("Insufficient arguments. Please check README file for more details.");
        }
    }

}