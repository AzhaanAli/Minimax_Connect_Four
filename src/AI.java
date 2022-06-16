import java.util.ArrayList;

public class AI extends Board{


    // --------------------------------- //
    // Instance variables.

    // Controls the color of coin the AI uses.
    public final byte PLAYER_CODE;

    // Controls the max recursive depth of the AI.
    public int difficulty;

    // Counts the occurrences of each best move.
    // This is helpful, as alpha-beta pruning becomes more useful the sooner better moves are found.
    // Therefore, if there is a connection between the distribution of best moves and future best moves,
    // then this array can be used to prioritize checking certain columns before others,
    // pruning the decision-tree and resulting in much greater efficiency.
    private final int[] bestMoveDistribution;

    // Gives the AI the ability to temporarily increase its computational depth when in danger.
    private boolean emergencyComputations = false;

    // Lets the AI increase its difficulty when it begins to compute a moves faster.
    // If a move takes more than 4 seconds to compute, the difficulty will decrement.
    private final boolean dynamicDifficulty = true;

    // --------------------------------- //
    // Constructor.

    public AI(){

        this(6, 7, 7);

    }
    public AI(int difficulty){

        this(6, 7, difficulty);

    }
    public AI(int height, int width){

        this(height, width, 6);

    }
    public AI(int height, int width, int difficulty){

        super(height, width);
        this.difficulty = difficulty;
        this.PLAYER_CODE = (byte) 2;
        this.bestMoveDistribution = new int[super.WIDTH];

    }


    // --------------------------------- //
    // Methods.

    public int getBestMove(){

        // Count the amount of zeros on the screen to know the turn of the game.
        int zeros = zeroSum();
        // Special case for turn 1.
        if(zeros == super.board.length)
        {
            int half = (super.WIDTH - 1) / 2;
            return super.WIDTH % 2 == 0? half + (int) (Math.random() * 2): half;
        }

        long startTime = System.currentTimeMillis();

        // Color the AI's text.
        System.out.print("\u001b[32m");

        // Variables to note AI attitude.
        boolean willWin = false;
        int averageLoss = 0;
        int lossCount = 0;
        int playerTraps = 0;


        int baseDifficulty = this.difficulty + this.filledColumns();

        // Get the order in which moves will be checked.
        int[] checkOrder = getDistributionOrder();

        // Loop over all possible moves and collect a list of moves which all have the same max value.
        ArrayList<Integer> bestMoves = new ArrayList<>();
        int max = Short.MIN_VALUE;

        // Check to see whether the AI can win or must defend before making a standard move.
        for(int col = 0; col < super.WIDTH; col++)
            if (super.colIsOpen(col)) {
                // Win at first priority.
                super.placeCoin(col, this.PLAYER_CODE);
                if (super.hasWon()) {
                    System.out.println("The AI is striking for a win.");
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);
            }
        for(int col = 0; col < super.WIDTH; col++)
            if (super.colIsOpen(col)) {
                // Block at second priority.
                super.placeCoin(col, (byte) 1);
                if (super.hasWon()) {
                    System.out.println("The AI is on the defense.");
                    this.undoLastMove(col);
                    return col;
                }
                this.undoLastMove(col);
            }

        // Call minimax method and evaluate possible moves.
        System.out.print("Thinking");
        for(int i = 0; i < super.WIDTH; i++) {

            int col = checkOrder[i];

            if (super.colIsOpen(col)) {

                super.placeCoin(col, (byte) 2);
                int loss = minimax(
                        false,
                        emergencyComputations? Math.max(baseDifficulty, 7) : baseDifficulty,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE,
                        zeros
                );
                this.undoLastMove(col);

                // Update AI attitudes.
                if(loss >= 100) willWin = true;
                else if(loss <= -100) playerTraps++;
                else
                {
                    averageLoss += loss;
                    lossCount++;
                }

                System.out.print(". ");

                if (loss > max)
                {
                    max = loss;
                    bestMoves.clear();
                    bestMoves.add(col);
                }
                else if (loss == max) bestMoves.add(col);
            }
        }

        // Update best move distribution array.
        for(int col : bestMoves) bestMoveDistribution[col]++;

        // Interpret attitudes.
        System.out.println();
        if(lossCount != 0) averageLoss /= lossCount;
        if (willWin) System.out.println("The AI sees an opening.");
        else if(playerTraps >= 3) System.out.println("The AI is being very cautious.");
        else if(averageLoss <= -20) System.out.println("The AI trying to plan.");

        this.emergencyComputations = playerTraps >= 3 || averageLoss <= -20;

        if(this.dynamicDifficulty)
        {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime <= 400 && this.difficulty >= 4)
            {
                // Difficulty should be gradually increased.
                // Increase will be defined by 400 --> 2 and 100 --> 4.
                // (400, 2) and (100, 4)
                // Equation will follow y = mx + b.
                // m = (2 - 4) / (400 - 100) = -1/150
                // 2 = -400 / 150 + b --> b = 4 + 2/3
                // y = 4 + 2/3 - x / 150
                this.difficulty += (4.6666F - elapsedTime / 150);
                System.out.println("The AI is closing in.");
            }
            else if (elapsedTime <= 1000)   this.difficulty++;
            else if (elapsedTime >= 7500 && this.difficulty > 6) this.difficulty = 6;
            else if (elapsedTime >= 4000 && this.difficulty > 5) this.difficulty--;

        }

//        System.out.println("ALL SEEING: " + (double) Math.min(this.difficulty, zeros) / zeros * 100 + " %");

        // Choose a random best move.
        return bestMoves.get((int)(Math.random() * bestMoves.size()));

    }

    // TODO: add more comments to this method.
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
                    {
                        byte position = this.board[getIndex(row, col - offset)];
                        String value;
                        switch (position)
                        {
                            case 0: value = " "; break;
                            case 1: value = "1"; break;
                            case 2: value = "2"; break;
                            default: throw new IllegalStateException("Unexpected value.");
                        }
                        identity.append(value);
                    }
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally vertical points.
                identity = new StringBuilder();
                if (row >= 3) {
                    for (int offset = 0; offset < 4; offset++)
                    {
                        byte position = this.board[getIndex(row - offset, col)];
                        String value;
                        switch (position)
                        {
                            case 0: value = " "; break;
                            case 1: value = "1"; break;
                            case 2: value = "2"; break;
                            default: throw new IllegalStateException("Unexpected value.");
                        }
                        identity.append(value);
                    }
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally right diagonal points.
                identity = new StringBuilder();
                if (row >= 3 && col >= 3) {
                    for (int offset = 0; offset < 4; offset++) {
                        byte position = this.board[getIndex(row - offset, col - offset)];
                        String value;
                        switch (position)
                        {
                            case 0: value = " "; break;
                            case 1: value = "1"; break;
                            case 2: value = "2"; break;
                            default: throw new IllegalStateException("Unexpected value.");
                        }
                        identity.append(value);
                    }
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

                // Tally left diagonal points.
                identity = new StringBuilder();
                if (row >= 3 && col <= this.WIDTH - 4) {
                    for (int offset = 0; offset < 4; offset++)
                    {
                        byte position = this.board[getIndex(row - offset, col + offset)];
                        String value;
                        switch (position)
                        {
                            case 0: value = " "; break;
                            case 1: value = "1"; break;
                            case 2: value = "2"; break;
                            default: throw new IllegalStateException("Unexpected value.");
                        }
                        identity.append(value);
                    }
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn);
                }

            }
        return totalPoints;

    }

    // This method takes in an identity string representing the "form" of a group of four coins on the board.
    // All forms are not created equal in the aid of victory, therefore they deserve different scores.
    public int scoreIdentityStr(String identity, boolean aiTurn){

        int playerPoints;
        int aiPoints;

        switch(identity){
            default: playerPoints = 0; break;

            // Two cases.
            case " 11 ": playerPoints = 5; break;
            case "1  1": playerPoints = 3; break;
            case "11  ": playerPoints = 2; break;
            case "11 2": playerPoints = 1; break;
            case "  11": playerPoints = 2; break;
            case "2 11": playerPoints = 1; break;

            // Three cases.
            case "111 ": playerPoints = 7; break;
            case " 111": playerPoints = 7; break;
            case "1 11": playerPoints = 7; break;
            case "11 1": playerPoints = 7; break;
        };
        switch(identity){
            default: aiPoints = 0; break;

            // Two cases.
            case " 22 ": aiPoints = 5; break;
            case "2  2": aiPoints = 3; break;
            case "22  ": aiPoints = 2; break;
            case "22 1": aiPoints = 1; break;
            case "  22": aiPoints = 2; break;
            case "1 22": aiPoints = 1; break;

            // Three cases.
            case "222 ": aiPoints = 7; break;
            case " 222": aiPoints = 7; break;
            case "2 22": aiPoints = 7; break;
            case "22 2": aiPoints = 7; break;
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

    // Converts a number N to the Nth most common occurrence of the best move distribution.
    private int[] getDistributionOrder(){

        // The Identity array contains all column positions and will be
        // shuffled to resemble the order of the best move distribution array.
        int[] identities = new int[super.WIDTH];
        for(int i = 0; i < identities.length; i++)
            identities[i] = i;

        // Bubble sort.
        for (int n = 0; n < this.bestMoveDistribution.length; n++)
            for (int j = 0; j < this.bestMoveDistribution.length - n - 1; j++)
                if (this.bestMoveDistribution[j] < this.bestMoveDistribution[j + 1]) {
                    int swapString = this.bestMoveDistribution[j];
                    this.bestMoveDistribution[j] = this.bestMoveDistribution[j + 1];
                    this.bestMoveDistribution[j + 1] = swapString;
                    int swapInt = identities[j];
                    identities[j] = identities[j + 1];
                    identities[j + 1] = swapInt;
                }
        return identities;

    }


    // --------------------------------- //


}
