import java.util.ArrayList;

public class AI extends Board{

    // --------------------------------- //
    // Instance variables.

    public final byte PLAYER_CODE;


    // --------------------------------- //
    // Constructor.

    public AI(){

        this((byte) 2);

    }
    public AI(byte playerCode){

        this.PLAYER_CODE = playerCode;

    }


    // --------------------------------- //
    // Methods.

    public int getBestMove(){

        int max = Short.MIN_VALUE;
        ArrayList<Integer> bestMoves = new ArrayList<>();

        for(int col = 0; col < super.WIDTH; col++)
            if(super.colIsOpen(col))
            {
                // Win at first priority.
                super.placeCoin(col, (byte) 2);
                if(super.hasWon())
                {
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);

                // Block at second priority.
                super.placeCoin(col, (byte) 1);
                if(super.hasWon())
                {
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);

                super.placeCoin(col, (byte) 2);
                int loss = minimax(false, 11, Integer.MIN_VALUE, Integer.MAX_VALUE);
                this.undoLastMove(col);

                System.out.print(loss + " ");

                if(loss > max)
                {
                    max = loss;
                    bestMoves.clear();
                    bestMoves.add(col);
                }
                else if(loss == max)
                    bestMoves.add(col);
            }
        System.out.println();

        System.out.println(bestMoves + " --> " + max);
        return bestMoves.get((int)(Math.random() * bestMoves.size()));
    }

    // Recursive decision-making.
    public int minimax(boolean aiTurn, int countDown, int alpha, int beta){

        if (super.hasWon() || countDown == 0) return this.evaluateBoard(aiTurn);
        int minMax = aiTurn? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for(int col = 0; col < super.WIDTH; col++)
            if(super.colIsOpen(col))
            {
                super.placeCoin(col, (byte) (aiTurn? 2 : 1));
                int loss = minimax(
                        !aiTurn,
                        countDown - 1,
                        alpha, beta
                );
                minMax = aiTurn?
                        Math.max(minMax, loss):
                        Math.min(minMax, loss);
                undoLastMove(col);

                if(aiTurn)
                {
                    alpha = Math.max(alpha, loss);
                }else{
                    beta = Math.min(beta, loss);
                }

                if(beta <= alpha) break;

            }
        return minMax;

    }

    // Evaluates a boards state.
    private int evaluateBoard(boolean aiTurn) {

        int score = 0;

        if(super.hasWon()){
            score += (1 + zeroSum()) * 100;
        }

        score += this.sumHorizontal();

        if(aiTurn) score *= -1;
        return score;

    }

    private int zeroSum(){

        int total = 0;
        for(byte b : super.board)
            if(b == 0)
                total++;
        return total;

    }

    // TODO: TEST THIS METHOD.
    private int countAdjacentHorizontal(){

        int total = 0;

        int streak = 0;
        for(int row = 0; row < super.HEIGHT; row++)
        {
            for(int col = 0; col < super.WIDTH; col++){

                int index = super.getIndex(row, col);
                if(super.board[index] == 0)
                {
                    total += switch(streak) {
                        case 2 -> 4;
                        case 3 -> 10;
                        default -> 0;
                    };
                    streak = 0;
                }
                else if(super.board[index] == 2)
                {
                    streak++;
                }

            }
        }

        return total;

    }

    public int sumHorizontal(){

        int totalPoints = 0;
        for(int row = 0; row < this.HEIGHT; row++) {
            int zeroCount = 0;
            int twoCount  = 0;
            for (int col = 3; col < this.WIDTH; col++) {

                for (int offset = 0; offset < 4; offset++)
                    switch(this.board[getIndex(row, col - offset)]){
                        case 0 -> zeroCount++;
                        case 2 -> twoCount++;
                    }

            }

            if (zeroCount == 1) {
                switch(twoCount){
                    case 2 -> totalPoints ++;
                    case 3 -> totalPoints += 5;
                }
            }
            else if (zeroCount == 2) {
                totalPoints += 2;
            }

        }
        return totalPoints;

    }


    // --------------------------------- //
    // Helper Methods.

    // Undoes the last move done on a column.
    private void undoLastMove(int col){

        for(int i = super.HEIGHT - 1; i >= 0; i--)
        {
            int index = super.getIndex(i, col);
            if(super.board[index] != 0)
            {
                super.board[index] = 0;
                return;
            }
        }

    }




}
