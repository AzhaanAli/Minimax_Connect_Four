public class Main {


    // --------------------------------- //
    // Main method.

    public static void main(String[] args) {

        startGame(
                6,
                7,
                7
        );

    }


    // --------------------------------- //
    // Helper Methods.

    // Begins a new game.
    // Parameters include dimensions of the board, max AI recursive depth, and whether you have the first turn.
    public static void startGame(){

        startGame(6, 7);

    }
    public static void startGame(int boardHeight, int boardWidth){

        startGame(boardHeight, boardWidth, 7);

    }
    public static void startGame(int boardHeight, int boardWidth, int aiRecursiveDepth){

        // 50-50 chance on who opens first.
        startGame(boardHeight, boardWidth, aiRecursiveDepth, Math.random() > .5);

    }
    public static void startGame(int boardHeight, int boardWidth, int aiRecursiveDepth, boolean startFirst){

        AI ai = new AI(boardHeight, boardWidth, aiRecursiveDepth);

        ai.print();

        while(!ai.hasWon() && ai.zeroSum() != 0)
        {
            if(startFirst) ai.promptUserTurn();
            else ai.placeCoin(ai.getBestMove(), (byte) 2);

            ai.print();
            startFirst = !startFirst;
        }

        if(ai.zeroSum() == 0) System.out.println("Game has ended in a draw.");
        else System.out.println("\n" + (startFirst? "\u001b[32mAi" : "Player") + " has won!");

    }


    // --------------------------------- //


}