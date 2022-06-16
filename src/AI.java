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

        int zeros = zeroSum();
//        System.out.println("DANGER EVALUATION: " + evaluateBoard(true, zeros));
        System.out.print("Thinking");

        int max = Short.MIN_VALUE;
        ArrayList<Integer> bestMoves = new ArrayList<>();

        for(int col = 0; col < super.WIDTH; col++)
            if(super.colIsOpen(col))
            {
                // Win at first priority.
                super.placeCoin(col, (byte) 2);
                if(super.hasWon())
                {
                    System.out.println();
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);

                // Block at second priority.
                super.placeCoin(col, (byte) 1);
                if(super.hasWon())
                {
                    System.out.println();
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);

                super.placeCoin(col, (byte) 2);
                int loss = minimax(false, 8, Integer.MIN_VALUE, Integer.MAX_VALUE, zeros);
                this.undoLastMove(col);

                System.out.print(" " + loss);
//                System.out.print(".");

                if(loss > max)
                {
                    max = loss;
                    bestMoves.clear();
                    bestMoves.add(col);
                }
                else if(loss == max)
                    bestMoves.add(col);
            }
        System.out.println("\n\n\n\n");
        return bestMoves.get((int)(Math.random() * bestMoves.size()));
    }

    // Recursive decision-making.
    public int minimax(boolean aiTurn, int countDown, int alpha, int beta, int zeros){

        if (super.hasWon() || countDown == 0) return this.evaluateBoard(aiTurn, zeros);
        int minMax = aiTurn? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        for(int col = 0; col < super.WIDTH; col++)
            if(super.colIsOpen(col))
            {
                super.placeCoin(col, (byte) (aiTurn? 2 : 1));
                int loss = minimax(
                        !aiTurn,
                        countDown - 1,
                        alpha, beta,
                        zeros - 1
                );
                minMax = aiTurn?
                        Math.max(minMax, loss):
                        Math.min(minMax, loss);
                undoLastMove(col);

                // Alpha-Beta pruning.
                if(aiTurn) alpha = Math.max(alpha, loss);
                else beta = Math.min(beta, loss);
                if(beta <= alpha) break;

            }

        return minMax;

    }

    // Evaluates a boards state.
    private int evaluateBoard(boolean aiTurn, int zeros) {

        int score = 0;

        if(super.hasWon()){
            score += zeros * 100;
        }

        score += this.sumVertical(aiTurn);
        score += this.sumHorizontal(aiTurn);
        score += this.sumLeftDiagonal(aiTurn);
        score += this.sumRightDiagonal(aiTurn);

        if(aiTurn) score *= -1;
        return score;

    }

    public int zeroSum(){

        int total = 0;
        for(byte b : super.board)
            if(b == 0)
                total++;
        return total;

    }



    // TODO: make switch case a method.
    // TODO: combine all into one loop.
    public int sumHorizontal(boolean aiTurn){

        int totalPoints = 0;
        for(int row = 0; row < this.HEIGHT; row++)
            for (int col = 3; col < this.WIDTH; col++)
            {
                StringBuilder identity = new StringBuilder();
                for (int offset = 0; offset < 4; offset++)
                    identity.append(switch (this.board[getIndex(row, col - offset)]) {
                        case 0 -> " ";
                        case 1 -> "1";
                        case 2 -> "2";
                        default -> throw new IllegalStateException("Unexpected value.");
                    });
                totalPoints += scoreIdentityStr(identity.toString(), aiTurn);


            }
        return totalPoints;

    }
    public int sumVertical(boolean aiTurn){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
            for(int col = 0; col < this.WIDTH; col++)
            {
                StringBuilder identity = new StringBuilder();
                for (int offset = 0; offset < 4; offset++)
                    identity.append(switch (this.board[getIndex(row - offset, col)]) {
                        case 0 -> " ";
                        case 1 -> "1";
                        case 2 -> "2";
                        default -> throw new IllegalStateException("Unexpected value.");
                    });
                totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
            }
        return totalPoints;

    }
    public int sumRightDiagonal(boolean aiTurn){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
            for (int col = 3; col < this.WIDTH; col++)
            {
                StringBuilder identity = new StringBuilder();
                for (int offset = 0; offset < 4; offset++)
                    identity.append(switch (this.board[getIndex(row - offset, col - offset)]) {
                        case 0 -> " ";
                        case 1 -> "1";
                        case 2 -> "2";
                        default -> throw new IllegalStateException("Unexpected value.");
                    });
                totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
            }
        return totalPoints;

    }
    public int sumLeftDiagonal(boolean aiTurn){

        int totalPoints = 0;
        for(int row = 3; row < this.HEIGHT; row++)
            for (int col = this.WIDTH - 4; col >= 0; col--)
            {
                StringBuilder identity = new StringBuilder();
                for (int offset = 0; offset < 4; offset++)
                    identity.append(switch (this.board[getIndex(row - offset, col + offset)]) {
                        case 0 -> " ";
                        case 1 -> "1";
                        case 2 -> "2";
                        default -> throw new IllegalStateException("Unexpected value.");
                    });
                totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
            }
        return totalPoints;

    }

    public int scoreIdentityStr(String identity, boolean aiTurn){

        int playerPoints = switch(identity){
            default -> 0;

            // Two cases.
            case " 11 " -> 5;
            case "1  1" -> 3;
            case "11  " -> 2;
            case "11 2" -> 1;
            case "  11" -> 2;
            case "2 11" -> 1;

            // Three cases.
            case "111 " -> 7;
            case " 111" -> 7;
            case "1 11" -> 7;
            case "11 1" -> 7;
        };
        int aiPoints = switch(identity){
            default -> 0;

            // Two cases.
            case " 22 " -> 5;
            case "2  2" -> 3;
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

        if(aiTurn) return playerPoints - aiPoints;
        else return aiPoints - playerPoints;

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
