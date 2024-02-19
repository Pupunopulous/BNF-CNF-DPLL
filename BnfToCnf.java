import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Node in the parse tree
class Node {
    public boolean isNot;
    public String val;
    public Node left;
    public Node right;

    public Node() {
        isNot = false;
        val = "";
        left = null;
        right = null;
    }
}

public class BnfToCnf {
    // Check if operations are valid
    public static boolean isMeaningful(char c) {
        if (Character.isLetterOrDigit(c)) return true;
        HashSet<Character> st = new HashSet<>();
        st.add('_');
        st.add('!');
        st.add('v');
        st.add('^');
        st.add('<');
        st.add('>');
        st.add('(');
        st.add(')');
        if (st.contains(c)) return true;
        else return false;
    }

    // Trim current statement
    public static void strip(StringBuilder str) {
        StringBuilder s = new StringBuilder();
        for (char c : str.toString().toCharArray()) {
            if (isMeaningful(c)) s.append(c);
        }
        str.setLength(0);
        str.append(s);
    }

    // Simplify "=>" to ">", and "<=>" to "="
    public static void simplify(StringBuilder str) {
        StringBuilder s = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == '<') {
                s.append('=');
                while (str.charAt(i) != '>') i++;
                i++;
            } else if (str.charAt(i) == '=') {
                s.append('>');
                while (str.charAt(i) != '>') i++;
                i++;
            } else {
                s.append(str.charAt(i));
                i++;
            }
        }
        str.setLength(0);
        str.append(s);
    }

    // If brackets are "[]" change them to "()"
    public static void replaceBrackets(StringBuilder str) {
        StringBuilder s = new StringBuilder();
        for (char c : str.toString().toCharArray()) {
            if (c == '[') s.append('(');
            else if (c == ']') s.append(')');
            else s.append(c);
        }
        str.setLength(0);
        str.append(s);
    }

    // Convert the given string to a clause
    public static List<String> stringToExpr(String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        replaceBrackets(stringBuilder);
        strip(stringBuilder);
        simplify(stringBuilder);

        HashSet<Character> operators = new HashSet<>();
        operators.add('!');
        operators.add('v');
        operators.add('^');
        operators.add('<');
        operators.add('>');
        operators.add('(');
        operators.add(')');
        operators.add('=');

        List<String> expr = new ArrayList<>();
        StringBuilder item = new StringBuilder();
        for (char c : stringBuilder.toString().toCharArray()) {
            if (operators.contains(c)) {
                if (item.length() > 0) {
                    expr.add(item.toString());
                    item.setLength(0);
                }
                expr.add(String.valueOf(c));
            } else {
                item.append(c);
            }
        }
        if (item.length() > 0) {
            expr.add(item.toString());
        }
        return expr;
    }

    // Check for valid operators
    public static boolean isOperator(String str) {
        if (str.length() != 1) return false;
        HashSet<Character> operators = new HashSet<>();
        operators.add('!');
        operators.add('v');
        operators.add('^');
        operators.add('<');
        operators.add('>');
        operators.add('(');
        operators.add(')');
        return operators.contains(str.charAt(0));
    }

    // Remove implications
    public static List<String> removeIff(List<String> expr) {
        while (true) {
            int i = expr.size() - 1;
            while (i >= 0) {
                if (expr.get(i).equals("=")) break;
                --i;
            }
            if (i < 0) return expr;

            // find left end
            int cnt = 0;
            int bg = i - 1;
            while (bg >= 0) {
                if (expr.get(bg).equals(")")) ++cnt;
                else if (expr.get(bg).equals("(")) --cnt;
                if (cnt < 0) break;
                --bg;
            }
            ++bg;

            // find right end
            cnt = 0;
            int ed = i + 1;
            while (ed < expr.size()) {
                if (expr.get(ed).equals("(")) ++cnt;
                else if (expr.get(ed).equals(")")) --cnt;
                if (cnt < 0) break;
                ++ed;
            }
            --ed;

            // Evaluate <=>
            List<String> ans = new ArrayList<>(expr.subList(0, bg)); // left(
            ans.add("(");
            ans.add("(");
            ans.addAll(expr.subList(bg, i)); // left, (, (, expr1
            ans.add(")");
            ans.add(">");
            ans.add("(");
            ans.addAll(expr.subList(i + 1, ed + 1)); // left, (, (, expr1, ), =>, (, expr2
            ans.add(")");
            ans.add(")");
            ans.add("^");
            ans.add("(");
            ans.add("(");
            ans.addAll(expr.subList(i + 1, ed + 1)); // left, (, (, expr1, ), =>, (, expr2, ), ), &, (, (, expr2
            ans.add(")");
            ans.add(">");
            ans.add("(");
            ans.addAll(expr.subList(bg, i)); // left, (, (, expr1, ), =>, (, expr2, ), ), &, (, expr2, =>, (, expr1
            ans.add(")");
            ans.add(")");
            ans.addAll(expr.subList(ed + 1, expr.size())); // left, (, (, expr1, ), =>, (, expr2, ), ), &, (, expr2, =>, (, expr1, ), ), )right
            expr = new ArrayList<>(ans);
        }
    }

    // Remove implications
    public static List<String> removeImply(List<String> expr) {
        while (true) {
            int i = expr.size() - 1;
            while (i >= 0) {
                if (expr.get(i).equals(">")) break;
                i--;
            }
            if (i < 0) return expr;

            int cnt = 0;
            int bg = i - 1;
            while (bg >= 0) {
                if (expr.get(bg).equals(")")) cnt++;
                else if (expr.get(bg).equals("(")) cnt--;
                if (cnt < 0) break;
                bg--;
            }
            bg++;

            int ed = i + 1;
            cnt = 0;
            while (ed < expr.size()) {
                if (expr.get(ed).equals("(")) cnt++;
                else if (expr.get(ed).equals(")")) cnt--;
                if (cnt < 0) break;
                ed++;
            }
            ed--;

            // Evaluate <=>
            List<String> ans = new ArrayList<>(expr.subList(0, bg));
            ans.add("(");
            ans.add("!");
            ans.add("(");
            ans.addAll(expr.subList(bg, i));
            ans.add(")");
            ans.add("v");
            ans.add("(");
            ans.addAll(expr.subList(i + 1, ed + 1));
            ans.add(")");
            ans.add(")");
            ans.addAll(expr.subList(ed + 1, expr.size()));
            expr = new ArrayList<>(ans);
        }
    }

    // Helper function to print the expression
    public static void printExpr(List<String> expr) {
        for (String item : expr) {
            System.out.print(item);
        }
        System.out.println();
    }

    // Helper function to copy a tree
    public static Node deepCopy(Node tree) {
        if (tree == null) return null;
        Node treeCp = new Node();
        treeCp.isNot = tree.isNot;
        treeCp.val = tree.val;
        treeCp.left = deepCopy(tree.left);
        treeCp.right = deepCopy(tree.right);
        return treeCp;
    }

    // Helper function to delete a tree
    public static void delTree(Node tree) {
        if (tree.left != null) delTree(tree.left);
        if (tree.right != null) delTree(tree.right);
        tree = null;
    }

    // Helper function to create a parse tree
    public static Node createTree(List<String> expr) {
        Node root = new Node();
        while (true) {
            int i = expr.size() - 1;
            int cnt = 0;
            while (i >= 0) {
                if (expr.get(i).equals(")")) cnt++;
                else if (expr.get(i).equals("(")) cnt--;
                else if (expr.get(i).equals("v") && cnt == 0) break;
                i--;
            }
            if (i >= 0) {
                root.val = "v";
                root.left = createTree(expr.subList(0, i));
                root.right = createTree(expr.subList(i + 1, expr.size()));
                return root;
            }

            i = expr.size() - 1;
            cnt = 0;
            while (i >= 0) {
                if (expr.get(i).equals(")")) cnt++;
                else if (expr.get(i).equals("(")) cnt--;
                else if (expr.get(i).equals("^") && cnt == 0) break;
                i--;
            }
            if (i >= 0) {
                root.val = "^";
                root.left = createTree(expr.subList(0, i));
                root.right = createTree(expr.subList(i + 1, expr.size()));
                return root;
            }

            if (expr.get(0).equals("!")) {
                expr = expr.subList(1, expr.size());
                root.isNot = !root.isNot;
                continue;
            }

            if (expr.get(0).equals("(")) {
                if (!expr.get(expr.size() - 1).equals(")")) {
                    System.out.println("Brackets do not match:");
                    printExpr(expr);
                    System.exit(0);
                }
                expr = expr.subList(1, expr.size() - 1);
                continue;
            }

            if (expr.size() != 1) {
                System.out.println("Error: Expression is not an atom:");
                printExpr(expr);
                System.exit(0);
            }
            root.val = expr.get(0);
            return root;
        }
    }

    // Remove "!"
    public static void removeNegation(Node tree, boolean notAgain) {
        if (tree == null) return;
        if (notAgain == tree.isNot) notAgain = false;
        else notAgain = true;
        if (tree.left == null && tree.right == null) tree.isNot = notAgain;
        else {
            tree.isNot = false;
            if (notAgain) {
                if (tree.val.equals("v")) tree.val = "^";
                else tree.val = "v";
            }
        }
        removeNegation(tree.left, notAgain);
        removeNegation(tree.right, notAgain);
    }

    // Check if "v" is completely distributed over "^"
    public static boolean isFullyDistributed(Node tree) {
        if (tree == null || !isOperator(tree.val)) return true;
        if (!isFullyDistributed(tree.left)) return false;
        if (!isFullyDistributed(tree.right)) return false;
        if (tree.val.equals("^")) return true;
        if (tree.left.val.equals("^") || tree.right.val.equals("^")) return false;
        return true;
    }

    // Helper function to distribute "v" over "^"
    public static void distributeOr(Node tree) {
        while (!isFullyDistributed(tree)) {
            if (tree == null || !isOperator(tree.val)) return;
            if (!isOperator(tree.left.val) && !isOperator(tree.right.val)) return;
            distributeOr(tree.left);
            distributeOr(tree.right);
            if (tree.left.val.equals("^") && tree.val.equals("v")) {
                Node pb = tree.left.right;
                Node pc = tree.right;
                tree.right = new Node();
                tree.right.isNot = tree.left.isNot;
                tree.right.val = "v";
                tree.right.left = pb;
                tree.right.right = pc;
                tree.left.right = deepCopy(pc);
                String temp = tree.val;
                tree.val = tree.left.val;
                tree.left.val = temp;
            }
            if (tree.right.val.equals("^") && tree.val.equals("v")) {
                Node pb = tree.right.left;
                Node pc = tree.left;
                tree.left = new Node();
                tree.left.isNot = tree.right.isNot;
                tree.left.val = "v";
                tree.left.right = pb;
                tree.left.left = pc;
                tree.right.left = deepCopy(pc);
                String temp = tree.val;
                tree.val = tree.right.val;
                tree.right.val = temp;
            }
        }
    }

    // Convert tree to CNF clause
    public static void tree2CNF(Node tree, List<String> lines, StringBuilder line) {
        if (tree == null) return;

        tree2CNF(tree.left, lines, line);

        if (tree.val.equals("v")) {
            line.append(' ');
        } else if (tree.val.equals("^")) {
            if (line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
            }
        } else {
            if (tree.isNot) line.append('!');
            line.append(tree.val);
        }

        tree2CNF(tree.right, lines, line);
    }
    public static List<String> treeToCNF(Node tree) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        tree2CNF(tree, lines, line);
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    // Driver code for BNF to CNF parsing
    public static List<String> runBNF2CNF(String bnfLine) {
        List<String> expr = stringToExpr(bnfLine);
        expr = removeIff(expr);
        expr = removeImply(expr);
        Node tree = createTree(expr);
        removeNegation(tree, false);
        distributeOr(tree);
        List<String> lines = treeToCNF(tree);
        delTree(tree);
        return lines;
    }

    public static void main(String[] args) {
        String bnfLine1 = "!(A v !B) <=> (!C => D) ^ E";
        String bnfLine2 = "(A <=> C) => D";
        String bnfLine3 = "!A v (C ^ E) v (!B => !D)";
        String bnfLine4 = "A => !B";
        List<String> cnfLines = runBNF2CNF(bnfLine1);
        cnfLines.addAll(runBNF2CNF(bnfLine2));
        cnfLines.addAll(runBNF2CNF(bnfLine3));
        cnfLines.addAll(runBNF2CNF(bnfLine4));

        System.out.println("CNF Representation:");
        for (String line : cnfLines) {
            System.out.println(line);
        }
    }
}
