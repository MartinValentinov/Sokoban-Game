import java.util.*;

import static java.lang.System.exit;

public class SokobanGame {

    static final char WALL = '#';
    static final char EMPTY = '.';
    static final char PLAYER = '@';
    static final char BOX = 'B';
    static final char TARGET = '*';
    static final char BOX_ON_TARGET = 'O';

    private int width, height, elemLimit;
    private int playerX, playerY;
    private int moves = 0;
    private int boxesOnTarget = 0;

    private char[][] board;
    private boolean[][] isTarget;
    private boolean[][] isBox;
    private boolean[][] isBoxOnTarget;

    private final Stack<GameState> undoStack = new Stack<>();

    private final Random rand = new Random();

    public void initialize(int w, int h, int k) {
        this.width = w;
        this.height = h;
        this.elemLimit = k;

        this.board = new char[h][w];
        this.isTarget = new boolean[h][w];
        this.isBox = new boolean[h][w];
        this.isBoxOnTarget = new boolean[h][w];

        for (int y = 0; y < h; y ++) {
            for (int x = 0; x < w; x ++) {
                if (y == 0 || y == h -1 || x == 0 || x == w - 1) {
                    this.board[y][x] = WALL;
                } else {
                    this.board[y][x] = EMPTY;
                }
            }
        }

        this.playerX = w / 2;
        this.playerY = h / 2;

        this.board[playerY][playerX] = PLAYER;

        placeTargets();
        placeBoxes();
    }

    private void placeTargets() {
        int placedTargets = 0;

        while (placedTargets < this.elemLimit) {
            int x = this.rand.nextInt(width - 2) + 1;
            int y = this.rand.nextInt(height - 2) + 1;
            if (this.board[y][x] == EMPTY) {
                this.board[y][x] = TARGET;
                this.isTarget[y][x] = true;
                placedTargets ++;
            }
        }
    }

    private boolean isPossible(int x, int y) {
        if (x == 1 || x == this.width - 2) {
            return !isBox[y - 1][x] && !isBox[y + 1][x];
        }
        if (y == 1 || y == this.height - 2) {
            return !isBox[y][x - 1] && !isBox[y][x + 1];
        }
        return !(isBox[y - 1][x] && isBox[y - 1][x - 1] && isBox[y][x - 1]) ||
                !(isBox[y - 1][x] && isBox[y - 1][x + 1] && isBox[y][x + 1]) ||
                !(isBox[y][x - 1] && isBox[y + 1][x - 1] && isBox[y + 1][x]) ||
                !(isBox[y][x + 1] && isBox[y + 1][x + 1] && isBox[y + 1][x]);
    }

    private boolean cornerStuck(int x, int y) {
        if ((x == 1 && y == 2 && isBox[y - 1][x + 1]) ||
                (x == 2 && y == 1 && isBox[y + 1][x - 1])) {
            return true;
        }

        if ((x == width - 2 && y == 2 && isBox[y - 1][x - 1]) ||
                (x == width - 3 && y == 1 && isBox[y + 1][x + 1])) {
            return true;
        }

        if ((x == 1 && y == height - 3 && isBox[y + 1][x + 1]) ||
                (x == 2 && y == height - 2 && isBox[y - 1][x - 1])) {
            return true;
        }

        if ((x == width - 2 && y == height - 3 && isBox[y + 1][x - 1]) ||
                (x == width - 3 && y == height - 2 && isBox[y - 1][x + 1])) {
            return true;
        }

        return false;
    }


    private void placeBoxes() {
        List<int[]> freeSpace = new ArrayList<>();

        for (int y = 1; y < this.height - 1; y ++) {
            for (int x = 1; x < this.width - 1; x ++) {
                if (board[y][x] == EMPTY) {
                    freeSpace.add(new int[]{x, y});
                }
            }
        }

        int placedBoxes = prioritiseWalls(freeSpace);

        //        for (Iterator<int[]> it = freeSpace.iterator(); it.hasNext();) {
        //            int[] pos = it.next();
        //            if (pos[0] == 1 || pos[0] == width - 2 || pos[1] == 1 || pos[1] == height - 2) {
        //                it.remove();
        //            }
        //        }
        // This replaces iterators, hm. You really learn something nex every day

        freeSpace.removeIf(pos -> pos[0] == 1 || pos[0] == width - 2 || pos[1] == 1 || pos[1] == height - 2);

        while (!freeSpace.isEmpty() && placedBoxes < this.elemLimit) {
            int randIdx = rand.nextInt(freeSpace.size());
            int[] curPos = freeSpace.get(randIdx);
            freeSpace.remove(randIdx);

            int x = curPos[0];
            int y = curPos[1];

            if (this.board[y][x] != EMPTY || !isPossible(x, y)) {
                continue;
            }

            board[y][x] = BOX;
            isBox[y][x] = true;
            placedBoxes ++;
        }
    }

    private int prioritiseWalls(List<int[]> freeSpace) {
        int placedBoxes = 0;

        int targetsOnLeftWall = 0, targetsOnRightWall = 0;
        int targetsOnDownWall = 0, targetsOnUpWall = 0;

        for (int i = 0; i < this.height; i ++) {
            if (isTarget[i][1]) targetsOnLeftWall ++;
            if (isTarget[i][this.width - 2]) targetsOnRightWall ++;
        }

        for (int i = 0; i < this.width; i ++) {
            if (isTarget[1][i]) targetsOnUpWall ++;
            if (isTarget[this.height - 2][i]) targetsOnDownWall ++;
        }

        if (isTarget[this.height - 2][1]) targetsOnLeftWall --;
        if (isTarget[this.height - 2][this.width - 2]) targetsOnDownWall --;
        if (isTarget[1][this.width - 2]) targetsOnRightWall --;
        if (isTarget[1][1]) targetsOnUpWall --;

        int boxesOnLeftWall = 0, boxesOnRightWall = 0;
        int boxesOnUpWall = 0, boxesOnDownWall = 0;

        boxesOnLeftWall = helper(freeSpace, 1, true, targetsOnLeftWall);
        boxesOnUpWall = helper(freeSpace, 1, false, targetsOnUpWall);
        boxesOnRightWall = helper(freeSpace, width - 2, true, targetsOnRightWall);
        boxesOnDownWall = helper(freeSpace, height - 2, false, targetsOnDownWall);

        placedBoxes = boxesOnLeftWall + boxesOnRightWall + boxesOnUpWall + boxesOnDownWall;
        return placedBoxes;
    }

    private int helper(List<int[]> freeSpace, int fixedCoordinate, boolean vertical, int targetCount) {
        List<int[]> potential = new ArrayList<>();
        for (int[] pos : freeSpace) {
            int x = vertical ? fixedCoordinate : pos[0];
            int y = vertical ? pos[1] : fixedCoordinate;

            boolean isCorner = (x == 1 && y == 1) ||
                    (x == width - 2 && y == 1) ||
                    (x == 1 && y == height - 2) ||
                    (x == width - 2 && y == height - 2);

            if (isCorner || cornerStuck(x, y)) continue;

            if ((vertical && pos[0] == fixedCoordinate) || (!vertical && pos[1] == fixedCoordinate)) {
                potential.add(pos);
            }
        }

        int placed = 0;
        while (placed < targetCount && !potential.isEmpty()) {
            int idx = rand.nextInt(potential.size());
            int[] cur = potential.get(idx);
            int x = vertical ? fixedCoordinate : cur[0];
            int y = vertical ? cur[1] : fixedCoordinate;

            if (isPossible(x, y)) {
                board[y][x] = BOX;
                isBox[y][x] = true;
                placed++;
            }

            potential.remove(idx);
            freeSpace.remove(cur);
        }

        return placed;
    }

    public void move(String cmd) {
        undoStack.push(new GameState(playerX, playerY, moves, boxesOnTarget, isBox, isBoxOnTarget));

        cmd = cmd.toLowerCase();
        int dx = 0, dy = 0;
        switch (cmd) {
            case "up": case "w": dy = -1; break;
            case "down": case "s": dy = 1; break;
            case "left": case "a": dx = -1; break;
            case "right": case "d": dx = 1; break;
            case "q": exit(1);
            default: return;
        }

        int new_x = playerX + dx;
        int new_y = playerY + dy;

        if (board[new_y][new_x] == WALL) {
            System.out.println("Invalid move");
            return;
        }

        if (isBox[new_y][new_x] || isBoxOnTarget[new_y][new_x]) {
            int after_x = new_x + dx;
            int after_y = new_y + dy;

            if (board[after_y][after_x] == WALL || isBox[after_y][after_x] || isBoxOnTarget[after_y][after_x]) {
                System.out.println("Invalid move");
                return;
            }

            if (isBoxOnTarget[new_y][new_x]) {
                isBox[after_y][after_x] = true;
                isBoxOnTarget[new_y][new_x] = false;
                board[after_y][after_x] = BOX;
                board[new_y][new_x] = TARGET;
                boxesOnTarget --;
            }

            board[after_y][after_x] = BOX;
            isBox[new_y][new_x] = false;
            isBox[after_y][after_x] = true;
            board[new_y][new_x] = PLAYER;

            if (isTarget[after_y][after_x] && !isBoxOnTarget[after_y][after_x]) {
                board[after_y][after_x] = BOX_ON_TARGET;
                board[new_y][new_x] = EMPTY;
                isBox[new_y][new_x] = false;
                isBox[after_y][after_x] = false;
                boxesOnTarget ++;
                isBoxOnTarget[after_y][after_x] = true;
            }
        }

        if (isTarget[playerY][playerX]) {
            board[playerY][playerX] = TARGET;
        } else if (isBoxOnTarget[playerY][playerX]) {
            board[playerY][playerX] = BOX_ON_TARGET;
        } else board[playerY][playerX] = EMPTY;

        playerY = new_y;
        playerX = new_x;
        board[playerY][playerX] = PLAYER;
        moves ++;

        updateDisplay();

    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo");
            return;
        }

        GameState prevState = undoStack.pop();

        this.playerX = prevState.playerX;
        this.playerY = prevState.playerY;
        this.moves = prevState.moves;
        this.boxesOnTarget = prevState.boxesOnTarget;
        this.isBox = prevState.isBox;
        this.isBoxOnTarget = prevState.isBoxOnTarget;

        updateDisplay();
    }

    public void printBoard() {
        for (int y = 0; y < this.height; y ++) {
            for (int x = 0; x < this.width; x ++) {
                System.out.print(board[y][x]);
            }
            System.out.println();
        }
        System.out.printf("Move: %d", this.moves);
        System.out.println();
    }

    private void updateDisplay() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                board[y][x] = EMPTY;
                if (isTarget[y][x]) board[y][x] = TARGET;
                if (isBoxOnTarget[y][x]) board[y][x] = BOX_ON_TARGET;
                if (isBox[y][x]) board[y][x] = BOX;
            }
        }
        board[playerY][playerX] = PLAYER;
    }

    public boolean checkWin() {
        if(this.boxesOnTarget == this.elemLimit) {
            printBoard();
            System.out.printf("You win in %d moves!", this.moves);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int W, H, K;

        while (true) {
            System.out.print("Enter width (>4): ");
            W = scanner.nextInt();
            if (W >= 5) break;
            System.out.println("Width must be at least 5!");
        }
        while (true) {
            System.out.print("Enter height (>4): ");
            H = scanner.nextInt();
            if (H >= 5) break;
            System.out.println("Height must be at least 5!");
        }
        while (true) {
            System.out.print("Enter boxes limit: ");
            K = scanner.nextInt();
            if (K < ((W - 2) * (H - 2) - 2) / 2) break;
            System.out.println("Invalid boxes amount");
        }

        SokobanGame game = new SokobanGame();
        game.initialize(W, H, K);

        scanner.nextLine();

        while (!game.checkWin()) {
            game.printAllTestsSideBySide();
            System.out.print("Move (w/a/s/d, q to quit and u to undo): ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.equals("q") || cmd.equals("quit")) {
                System.out.println("Game exited.");
                break;
            }

            if (cmd.equals("u")) {
                game.undo();
                continue;
            }

            game.move(cmd);
        }

        scanner.close();
    }

    public void printAllTestsSideBySide() {
        System.out.println("BOARD\t\t\tisTarget\t\tisBox\t\tisBoxOnTarget");
        System.out.println("------------------------------------------------------------");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(board[y][x]);
            }

            System.out.print("\t\t\t");

            for (int x = 0; x < width; x++) {
                System.out.print(isTarget[y][x] ? "T" : ".");
            }

            System.out.print("\t\t\t");

            for (int x = 0; x < width; x++) {
                System.out.print(isBox[y][x] ? "B" : ".");
            }

            System.out.print("\t\t\t");

            for (int x = 0; x < width; x++) {
                System.out.print(isBoxOnTarget[y][x] ? "O" : ".");
            }

            System.out.println();
        }
    }
}