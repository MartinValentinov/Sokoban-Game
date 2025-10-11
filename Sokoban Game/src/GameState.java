public class GameState {
    int playerX, playerY, moves, boxesOnTarget;
    boolean[][] isBox;
    boolean[][] isBoxOnTarget;

    GameState(int playerX, int playerY, int moves, int boxesOnTarget,
              boolean[][] isBox, boolean[][] isBoxOnTarget) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.moves = moves;
        this.boxesOnTarget = boxesOnTarget;
        this.isBox = deepCpy(isBox);
        this.isBoxOnTarget = deepCpy(isBoxOnTarget);
    }

    private static boolean[][] deepCpy(boolean[][] arr) {
        boolean[][] cpy = new boolean[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i ++) {
            System.arraycopy(arr[i], 0, cpy[i], 0, arr[i].length);
        }
        return cpy;
    }
}
