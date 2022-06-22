import java.util.Scanner;

public class Main {


    // --------------------------------- //
    // Main method.

    public static void main(String[] args) {

        startGame(6, 7, 7);

    }


    // --------------------------------- //
    // Helper Methods.

    // Begins a new game.
    // Parameters include dimensions of the board, max AI recursive depth, and whether you have the first turn.
    public static void startGame(){

        startGame(6, 7);

    }
    public static void startGame(int boardHeight, int boardWidth){

        startGame(boardHeight, boardWidth, 6);

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
            else
            {
                int bestMove = ai.getBestMove();
                ai.placeCoin(bestMove, (byte) 2);
                System.out.println("\nAI placed a coin on column " + (bestMove + 1) + ".");
                System.out.println("\u001B[0m");
            }

            ai.print();
            startFirst = !startFirst;
        }

        System.out.println("\u001b[32m");
        if(ai.zeroSum() == 0) System.out.println("Game has ended in a draw.");
        else System.out.println("\n" + (startFirst? "Ai" : "Player") + " has won!");
        System.out.print("\u001B[0m\n");


    }

    public static void randomTraining(int boardHeight, int boardWidth, int aiRecursiveDepth, boolean startFirst){

        AI ai = new AI(boardHeight, boardWidth, aiRecursiveDepth);

        ai.print();

        while(!ai.hasWon() && ai.zeroSum() != 0)
        {
            if(startFirst)
            {
                int col = -1;
                while(!ai.colIsOpen(col))
                {
                    col = (int) (7 * Math.random());
                }
                ai.placeCoin(col, (byte) 1);
//                ai.promptUserTurn();
            }
            else ai.placeCoin(ai.getBestMove(), (byte) 2);
            System.out.println("\u001B[0m\n");

            ai.print();
            startFirst = !startFirst;
        }

        System.out.println("\u001b[32m");
        if(ai.zeroSum() == 0) System.out.println("Game has ended in a draw.");
        else System.out.println("\n" + (startFirst? "Ai" : "Player") + " has won!");
        System.out.print("\u001B[0m\n");


    }

    public static void gameLoop(){

        Scanner s = new Scanner(System.in);

        int height = 7;
        int width = 7;

//        try
//        {
//            System.out.print("ENTER BOARD HEIGHT: ");
//            height = Integer.parseInt(s.nextLine());
//            System.out.print("ENTER BOARD WIDTH:  ");
//            width = Integer.parseInt(s.nextLine());
//        }
//        catch(Exception e)
//        {
//            System.out.println("Starting with default settings.");
//            height = 6;
//            width = 7;
//        }

        boolean keepPlaying = true;
        while(keepPlaying)
        {
            startGame(height, width);
            System.out.println("Would you like to play again? (\"NO\" to stop): ");
            keepPlaying = !s.nextLine().equalsIgnoreCase("NO");
        }

    }


    // --------------------------------- //


}