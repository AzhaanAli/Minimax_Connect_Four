import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {


    public static byte[] board;


    // --------------------------------- //
    // Main method.

    public static void main(String[] args) {

        board = new byte[]{
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 2, 2, 0,
        //      1  2  3  4  5  6  7
        };

        AI ai = new AI();

        boolean aiTurn = Math.random() > .5;
        ai.print();

        while(!ai.hasWon() && ai.zeroSum() != 0)
        {
            if(aiTurn) ai.placeCoin(ai.getBestMove(), (byte) 2);
            else ai.promptUserTurn();

            ai.print();
            aiTurn = !aiTurn;
        }

        if(ai.zeroSum() == 0) System.out.println("Game has ended in a draw.");
        else System.out.println("\n" + (aiTurn? "Player" : "Ai") + " has won!");




    }


}