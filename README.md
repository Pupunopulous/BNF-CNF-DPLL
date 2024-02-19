# Using first-order logic to solve sudoku puzzles

### Author: 
Rahi Krishna / rk4748

## Running the Program

The `.java` file can be executed on any operating system with JDK 8.0 or higher. Follow the steps below:

### Compile the Java file:
```shell
javac Sudoku.java
java Sudoku {arg1 arg2 arg3 ...}
```

### The following arguments are accepted (if appended after "java Sodoku"):
IMPORTANT: Order of these arguments does not matter

### Required command line arguments:
1. input - A Sudoku board must be passed in the format `rc=v ...` to obtain a solution

### Optional command line arguments:
1. `-v` - Stores the board as BNF clauses in a file "BNF_clauses.txt" and the converted CNF clauses in "CNF_clauses.txt" in the same directory
Gives a verbose DPLL output for each clause (from "CNF_clauses.txt") assignment on the command line

IMPORTANT: Must use `-mode` identifier before writing `quick` or `solve`
2. `-mode solve` - Solves the sudoku board by performing DPLL in lexicographic order
IMPORTANT: This may take upto 1 hour, depending on the complexity of the sudoku board
3. `-mode quick`
IMPORTANT: This is the quicker version, which solves the sudoku board by random DPLL assignments
This may take 2-5 minutes for complex boards
4. `$input.txt` - Takes a file and converts all BNF clauses within into CNF using the standalone converter
IMPORTANT: MUST be used with "bnf" mode

IMPORTANT: "solve" mode is default
`solve` mode will give the correct answer, but it takes a significant amount of time for complex inputs
Please allow up to 1.5 hours of runtime for selected cases. The `quick` mode will give the same answer, but by selecting random CNF clauses
If `solve` mode takes more time than at hand, please use `quick` instead

### Some example commands:
```
javac Sudoku.java // Required
java Sudoku 11=5 12=3 14=6 19=9 22=4 27=7 37=3 38=1 39=6 45=2 48=6 49=5 51=9 53=2 56=5 58=7 63=5 66=3 67=9 71=4 73=6 74=1 75=5 82=5 84=2 85=8 87=6 92=1 95=9 97=5
java Sudoku -mode quick 11=9 14=6 15=7 16=2 21=2 26=1 27=4 39=8 44=1 51=7 52=4 54=3 55=9 58=8 63=6 66=4 78=2 79=9 89=1 91=5 92=6 93=1 97=7 -v
java Sudoku -v -mode solve 11=9 14=6 15=7 16=2 21=2 26=1 27=4 39=8 44=1 51=7 52=4 54=3 55=9 58=8 63=6 66=4 78=2 79=9 89=1 91=5 92=6 93=1 97=7
java Sudoku -mode solve 11=4 12=2 14=8 15=7 16=5 17=3 19=6 21=3 22=9 23=8 25=4 26=6 27=7 29=1 33=5 37=8 39=2 41=9 43=6 44=4 45=5 56=1 58=8 61=1 62=4 67=6 75=2 76=4 77=5 78=3 82=3 84=1 85=8 88=6 93=9 97=4
java Sudoku -mode bnf input.txt
```

IMPORTANT:
Please make sure `Sudoku.java`, `BnfToCnf.java` and `State.java` are in the same folder
Input order in the command line does not matter
