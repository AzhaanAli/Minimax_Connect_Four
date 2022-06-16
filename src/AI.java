import java.util.ArrayList;

public class AI extends Board{

    // --------------------------------- //
    // Instance variables.

    // Controls the color of coin the AI uses.
    public final byte PLAYER_CODE;

    // Controls the max recursive depth of the AI.
    public int difficulty;


    // --------------------------------- //
    // Constructor.

    public AI(){

        this(6, 7, 7);

    }
    public AI(int difficulty){

        this(6, 7, difficulty);

    }
    public AI(int height, int width){

        this(height, width, 7);

    }
    public AI(int height, int width, int difficulty){

        super(height, width);
        this.difficulty = difficulty;
        this.PLAYER_CODE = (byte) 2;

    }


    // --------------------------------- //
    // Methods.

    public int getBestMove(){

        int zeros = zeroSum();
//        System.out.println("DANGER EVALUATION: " + (evaluateBoard(true, zeros)));
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
                int loss = minimax(
                        false,
                        this.difficulty + this.filledColumns(),
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE,
                        zeros
                );
                this.undoLastMove(col);

//                System.out.print(" " + loss);
                System.out.print(".");

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

        if (super.hasWon() || countDown == 0) return this.evaluateBoard(!aiTurn, zeros);

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


    // --------------------------------- //
    // Scoring Methods.

    // Evaluates and "scores" a games terminal-state.
    // This method is the heart of the AI, as it determines what it values and thus how it tries to win.
    private int evaluateBoard(boolean aiTurn, int zeros) {

        int score = 0;

        // If the game has been won, add a high amount of points scaled by how quick it was won.
        // The more zeros on the screen the fewer turns have passed, making it a good metric for the scalar.
        if(super.hasWon()) score += zeros * 100;

        // Add points gained from game non-terminal-states.
        score += this.getNonTerminalPoints(!aiTurn);

        // If these points are for the player, make the score negative.
        // This symbolizes them working against the AI's cause.
        return aiTurn? score : -score;

    }

    // Returns the score component for a non-terminal board.
    public int getNonTerminalPoints(boolean aiTurn){

        int totalPoints = 0;
        for(int row = 0; row < this.HEIGHT; row++)
            for (int col = 0; col < this.WIDTH; col++)
            {

                byte player = this.board[getIndex(row, col)];

                // Tally horizontal points.
                StringBuilder identity = new StringBuilder();
                if (col >= 3) {
                    for (int offset = 0; offset < 4; offset++)
                        identity.append(switch (this.board[getIndex(row, col - offset)]) {
                            case 0 -> " ";
                            case 1 -> "1";
                            case 2 -> "2";
                            default -> throw new IllegalStateException("Unexpected value.");
                        });
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally vertical points.
                identity = new StringBuilder();
                if (row >= 3) {
                    for (int offset = 0; offset < 4; offset++)
                        identity.append(switch (this.board[getIndex(row - offset, col)]) {
                            case 0 -> " ";
                            case 1 -> "1";
                            case 2 -> "2";
                            default -> throw new IllegalStateException("Unexpected value.");
                        });
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally right diagonal points.
                identity = new StringBuilder();
                if (row >= 3 && col >= 3) {
                    for (int offset = 0; offset < 4; offset++)
                        identity.append(switch (this.board[getIndex(row - offset, col - offset)]) {
                            case 0 -> " ";
                            case 1 -> "1";
                            case 2 -> "2";
                            default -> throw new IllegalStateException("Unexpected value.");
                        });
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally left diagonal points.
                identity = new StringBuilder();
                if (row >= 3 && col <= this.WIDTH - 4) {
                    for (int offset = 0; offset < 4; offset++)
                        identity.append(switch (this.board[getIndex(row - offset, col + offset)]) {
                            case 0 -> " ";
                            case 1 -> "1";
                            case 2 -> "2";
                            default -> throw new IllegalStateException("Unexpected value.");
                        });
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

            }
        return totalPoints;

    }

    // This method takes in an identity string representing the "form" of a group of four coins on the board.
    // All forms are not created equal in the aid of victory, therefore they deserve different scores.
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

    // This method counts the number of filled columns.
    // Filled columns are important, as for each one, the AI makes significantly fewer recursive calls.
    // By extension, this means that as the amount of filled columns increases, recursive depth can be increased.
    private int filledColumns(){

        int total = 0;
        for(int i = 0; i < super.WIDTH; i++)
            if(!super.colIsOpen(i))
                total++;
        return total;

    }

    // Counts the amount of zeros on the board.
    // Total numbers - the amount of zeros on the screen equates to the turn of the game.
    public int zeroSum(){

        int total = 0;
        for(byte b : super.board)
            if(b == 0)
                total++;
        return total;

    }





}
