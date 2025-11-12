import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    private static final int INFINITY = Integer.MAX_VALUE;
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String UP = "up";
    private static final String DOWN = "down";

    public record Coordinate(int x, int y) {
    }

    public record Node(int[][] board, Coordinate emptyBlock, String move){}

    private static Stack<Node> path = new Stack<>();
    private static int[][] initialBoard;
    private static int[][] goalBoard;
    //    private static List<Node> visited = new ArrayList<>();
    private static int n = 0;
    private static Map<Integer, int[]> goalPositions;
    private static int numOfNonEmptyBlocks = 0;
    private static int emptyBlockIndex = -1;

    public static void main(String[] args) {
        Coordinate initialEmptySpace = retrieveInputValues();
        n = (int) Math.sqrt(numOfNonEmptyBlocks + 1);
        goalBoard = genGoalBoard();
        goalPositions = getGoalPositions(goalBoard);
//        System.out.println(initialEmptySpace);
//        System.out.println(n);
//        System.out.println(Arrays.deepToString(goalBoard));
//        System.out.println(goalPositions);

        int finalNode = idaStar(new Node(initialBoard, initialEmptySpace, ""));
        if (finalNode == -1) {
            System.out.println(-1);
        } else {
            System.out.println(path.size() - 1);
            for (int i = 1; i < path.size(); i++) {
                System.out.println(path.get(i).move());
            }
        }
    }

    private static int idaStar(Node root) {
        int threshold = getManhattanHeuristic(root.board());
        path.push(root);

        if (!isSolvable(root)) return -1; // not found

        while (true) {
            int t = search(path, 0, threshold);

            if (t == -1) return t; // found


            threshold = t;
        }
    }

    private static int search(Stack<Node> path, int g, int threshold) {
        Node currentNode = path.peek();
        int h = getManhattanHeuristic(currentNode.board());
        int f = g + h;

        if (f > threshold) return f;
        if (h == 0) return -1;

        int min = INFINITY;
        for (Node child : getChildren(currentNode)) {
            if (!path.contains(child)) {
                path.push(child);
                int t = search(path, g + 1, threshold);
                if (t == -1) return -1;
                if (t < min) min = t;
                path.pop();
            }
        }

        return min;
    }

    private static List<Node> getChildren(Node node) {
        List<Node> successors = new ArrayList<>();
        Coordinate emptyBlock = node.emptyBlock;

        // left
        if (node.emptyBlock().y() < n - 1) {
            int[][] nextBoard = deepCopyMatrix(node.board());
            swap(nextBoard, emptyBlock.x(), emptyBlock.y(), emptyBlock.x(), emptyBlock.y() + 1);
            successors.add(new Node(nextBoard, new Coordinate(emptyBlock.x(), emptyBlock.y() + 1), LEFT));
        }

        // right
        if (node.emptyBlock().y() > 0) {
            int[][] nextBoard = deepCopyMatrix(node.board());
            swap(nextBoard, emptyBlock.x(), emptyBlock.y(), emptyBlock.x(), emptyBlock.y() - 1);
            successors.add(new Node(nextBoard, new Coordinate(emptyBlock.x(), emptyBlock.y() - 1), RIGHT));
        }

        // up
        if (node.emptyBlock().x() > 0) {
            int[][] nextBoard = deepCopyMatrix(node.board());
            swap(nextBoard, emptyBlock.x(), emptyBlock.y(), emptyBlock.x() - 1, emptyBlock.y());
            successors.add(new Node(nextBoard, new Coordinate(emptyBlock.x() - 1, emptyBlock.y()), UP));
        }

        // down
        if (node.emptyBlock().x() < n - 1) {
            int[][] nextBoard = deepCopyMatrix(node.board());
            swap(nextBoard, emptyBlock.x(), emptyBlock.y(), emptyBlock.x() + 1, emptyBlock.y());
            successors.add(new Node(nextBoard, new Coordinate(emptyBlock.x() + 1, emptyBlock.y()), DOWN));
        }

        return successors;
    }

    private static void swap(int[][] matrix, int x1, int y1, int x2, int y2) {
        int temp = matrix[x1][y1];
        matrix[x1][y1] = matrix[x2][y2];
        matrix[x2][y2] = temp;
    }

    private static int[][] deepCopyMatrix(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return copy;
    }

    private static boolean isGoal(Node node) {
        return equals(node.board(), goalBoard);
    }

    private static int getManhattanHeuristic(int[][] node) {
        int totalDistance = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = node[i][j];

                if (value == 0) continue;

                int[] goalPos = goalPositions.get(value);
                totalDistance += Math.abs(i - goalPos[0]) + Math.abs(j - goalPos[1]);
            }
        }

        return totalDistance;
    }

    private static Map<Integer, int[]> getGoalPositions(int[][] goal) {
        Map<Integer, int[]> goalPositions = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                goalPositions.put(goal[i][j], new int[]{i, j});
            }
        }
        return goalPositions;
    }

    public static boolean equals(int[][] matrix1, int[][] matrix2) {
        // Check if both are null
        if (matrix1 == null && matrix2 == null) {
            return true;
        }

        // Check if one is null
        if (matrix1 == null || matrix2 == null) {
            return false;
        }

        // Check dimensions
        if (matrix1.length != matrix2.length) {
            return false;
        }

        // Check each row
        for (int i = 0; i < matrix1.length; i++) {
            if (matrix1[i] == null && matrix2[i] == null) {
                continue;
            }
            if (matrix1[i] == null || matrix2[i] == null) {
                return false;
            }
            if (matrix1[i].length != matrix2[i].length) {
                return false;
            }

            for (int j = 0; j < matrix1[i].length; j++) {
                if (matrix1[i][j] != matrix2[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    private static Coordinate retrieveInputValues() {
        Scanner scanner = new Scanner(System.in);

        numOfNonEmptyBlocks = scanner.nextInt();
        emptyBlockIndex = scanner.nextInt();

        Coordinate initialEmptySpace = null;

        int n = (int) Math.sqrt(numOfNonEmptyBlocks + 1);
        initialBoard = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                initialBoard[i][j] = scanner.nextInt();
                if (initialBoard[i][j] == 0) {
                    initialEmptySpace = new Coordinate(i, j);
                }
            }
        }

        return initialEmptySpace;
    }
    private static int[][] genGoalBoard() {
        int[][] goalMatrix = new int[n][n];

        int value = 1;
        boolean check = true;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (value - 1 == emptyBlockIndex && check) {
                    goalMatrix[i][j] = 0;
                    check = false;
                } else {
                    goalMatrix[i][j] = value;
                    ++value;
                }

            }
        }

        if (check) {
            goalMatrix[n - 1][n - 1] = 0;
        }

        return goalMatrix;
    }

    public static boolean isSolvable(Node node) {
        int[][] board = node.board();
        int n = board.length;
        int inversions = countInversions(board);

        if (n % 2 == 1) {
            // Odd-sized puzzle (3x3, 5x5, etc.): solvable if inversions are even
            return inversions % 2 == 0;
        } else {
            // Even-sized puzzle (4x4, 6x6, etc.): consider blank position
            int blankRow = node.emptyBlock().x();
            int blankRowFromBottom = n - blankRow;

            if (blankRowFromBottom % 2 == 0) {
                // Blank on even row from bottom: inversions must be odd
                return inversions % 2 == 1;
            } else {
                // Blank on odd row from bottom: inversions must be even
                return inversions % 2 == 0;
            }
        }
    }

    /**
     * Counts the number of inversions in the puzzle
     * An inversion is when a tile precedes another tile with a lower number
     * (excluding the blank/0 tile)
     */
    private static int countInversions(int[][] board) {
        int n = board.length;
        int[] flatBoard = new int[n * n];

        // Flatten the 2D board into 1D array, excluding 0
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != 0) {
                    flatBoard[index++] = board[i][j];
                }
            }
        }

        // Count inversions
        int inversions = 0;
        for (int i = 0; i < index - 1; i++) {
            for (int j = i + 1; j < index; j++) {
                if (flatBoard[i] > flatBoard[j]) {
                    inversions++;
                }
            }
        }

        return inversions;
    }
}
