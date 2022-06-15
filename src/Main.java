public class Main {




    // --------------------------------- //
    // Main method.

    public static void main(String[] args) {

        byte[] board = new byte[]{
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 2, 0,
        //      1  2  3  4  5  6  7
        };

        AI ai = new AI();

        boolean aiTurn = true;
        ai.print();

        while(!ai.hasWon())
        {
            if(aiTurn) ai.placeCoin(ai.getBestMove(), (byte) 2);
            else ai.promptUserTurn();

            ai.print();
            aiTurn = !aiTurn;
        }



    }





}