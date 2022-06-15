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
                int loss = minimax(false, 9, Integer.MIN_VALUE, Integer.MAX_VALUE);
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
        int minMax = aiTurn? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
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

        score += this.sumVertical();
        score += this.sumHorizontal();
        score += this.sumLeftDiagonal();
        score += this.sumRightDiagonal();

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



    // TODO: make switch case a method.
    // TODO: combine all into one loop.
    public int sumHorizontal(){

        int totalPoints = 0;
        for(int row = 0; row < this.HEIGHT; row++)
        {
            StringBuilder identity = new StringBuilder();
            for (int col = 3; col < this.WIDTH; col++)
            {

                for (int offset = 0; offset < 4; offset++)
                {
                    identity.append(switch (this.board[getIndex(row, col - offset)]) {
                        case 0 -> " ";
                        case 2 -> "2";
                        default -> "1";
                    });

                }

            }

            totalPoints += switch(identity.toString()){
                default -> 0;

                // Two cases.
                case " 22 " -> 5;
                case "22  " -> 2;
                case "22 1" -> 1;
                case "  22" -> 2;
                case "1 22" -> 1;

                // Three cases.
                case "222 " -> 7;
                case " 222" -> 7;
                case "2 22" -> 7;
                case "22 2" -> 7;

            };

        }
        return totalPoints;

    }
    public int sumVertical(){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
        {
            StringBuilder identity = new StringBuilder();
            for(int col = 0; col < this.WIDTH; col++)
            {

                for (int offset = 0; offset < 4; offset++)
                {
                    identity.append(switch (this.board[getIndex(row - offset, col)]) {
                        case 0 -> " ";
                        case 2 -> "2";
                        default -> "1";
                    });

                }

            }

            totalPoints += switch(identity.toString()){
                default -> 0;

                // Two cases.
                case " 22 " -> 5;
                case "22  " -> 2;
                case "22 1" -> 1;
                case "  22" -> 2;
                case "1 22" -> 1;

                // Three cases.
                case "222 " -> 7;
                case " 222" -> 7;
                case "2 22" -> 7;
                case "22 2" -> 7;

            };

        }
        return totalPoints;

    }
    public int sumRightDiagonal(){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
        {
            StringBuilder identity = new StringBuilder();
            for (int col = 3; col < this.WIDTH; col++)
            {

                for (int offset = 0; offset < 4; offset++)
                {
                    identity.append(switch (this.board[getIndex(row - offset, col - offset)]) {
                        case 0 -> " ";
                        case 2 -> "2";
                        default -> "1";
                    });

                }

            }

            totalPoints += switch(identity.toString()){
                default -> 0;

                // Two cases.
                case " 22 " -> 5;
                case "22  " -> 2;
                case "22 1" -> 1;
                case "  22" -> 2;
                case "1 22" -> 1;

                // Three cases.
                case "222 " -> 7;
                case " 222" -> 7;
                case "2 22" -> 7;
                case "22 2" -> 7;

            };

        }
        return totalPoints;

    }
    public int sumLeftDiagonal(){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
        {
            StringBuilder identity = new StringBuilder();
            for (int col = this.WIDTH - 4; col >= 0; col--)
            {

                for (int offset = 0; offset < 4; offset++)
                {
                    identity.append(switch (this.board[getIndex(row - offset, col + offset)]) {
                        case 0 -> " ";
                        case 2 -> "2";
                        default -> "1";
                    });

                }

            }

            totalPoints += switch(identity.toString()){
                default -> 0;

                // Two cases.
                case " 22 " -> 5;
                case "22  " -> 2;
                case "22 1" -> 1;
                case "  22" -> 2;
                case "1 22" -> 1;

                // Three cases.
                case "222 " -> 7;
                case " 222" -> 7;
                case "2 22" -> 7;
                case "22 2" -> 7;

            };

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
