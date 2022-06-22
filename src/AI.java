import java.util.ArrayList;
import java.util.Arrays;

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
    private int[] checkOrder;

    // Gives the AI the ability to temporarily increase its computational depth when in danger.
    private boolean emergencyComputations = false;

    // Lets the AI increase its difficulty when it begins to compute a moves faster.
    // If a move takes more than 4 seconds to compute, the difficulty will decrement.
    private boolean dynamicDifficulty;

    // This variable enables or disables memoization depending on its state.
    private final boolean memoize;
    private final Memoizer memoizer;

    // --------------------------------- //
    // Constructor.

    public AI(){

        this(6, 7);

    }
    public AI(int height, int width){

        this(height, width, 6);

    }
    public AI(int height, int width, int difficulty){

        super(height, width);
        this.difficulty = difficulty;
        this.PLAYER_CODE = (byte) 2;
        this.bestMoveDistribution = new int[super.WIDTH];
        this.dynamicDifficulty = true;
        this.memoize = difficulty >= 6 && height == 6 && width == 7;
        this.memoizer = memoize? new Memoizer() : null;


    }


    // --------------------------------- //
    // Methods.

    // Returns what the AI believes is the safest move to make to inch toward victory.
    public int getBestMove(){

        // Color the AI's text.
        System.out.print("\u001b[32m");

        // Count the amount of zeros on the screen to know the turn of the game.
        int zeros = zeroSum();

        // Print and retrieve the all seeing values.
        double allSeeing = printAllSeeing(zeros);

        // Check to see whether the AI can win or must defend before making a standard move.
        int winningColumn = this.canWin(this.PLAYER_CODE);
        if(winningColumn != -1)
        {
            System.out.println("AI is making the winning move.");
            return winningColumn;
        }
        int losingColumn = this.canWin((byte) 1);
        if(losingColumn != -1)
        {
            System.out.println("AI is on defense.");
            return losingColumn;
        }

        // Check if the boards been memoized.
        String boardAsString = Arrays.toString(super.board);
        int remembered = rememberBestMove(boardAsString);
        if(remembered != -1) return remembered;

        // Log start time to compute and handle elapsed time after the loss list is discovered.
        long startTime = System.currentTimeMillis();

        // Get the order in which moves will be checked.
        this.checkOrder = getDistributionOrder();

        // Assign values to the best moves array and retrieve a "loss-list."
        // A loss list represents the loss evaluation for given moves.
        // The best moves array holds the indices of the best possible moves to make for the given board state.
        ArrayList<Integer> bestMoves = new ArrayList<>();
        int[] lossList = this.getLossList(bestMoves, zeros, allSeeing);

        // Adjust recursive depth to match how long the previous turn took.
        if(this.dynamicDifficulty) this.doDynamicDifficulty(zeros, startTime);

        // Limit recursive depth only to what is needed to cover the entire board.
        // This means that if recursive is greater than the amount of zeros on the board, then depth
        // should be readjusted to the amount of zeros on the board.
        this.difficulty = Math.min(this.difficulty, zeros);

        // If best moves is somehow empty, then choose the first available move.
        if(bestMoves.size() == 0)
            for(int i : checkOrder)
                if(this.colIsOpen(i))
                    return i;

        // If the recursive depth of the greater than 7, a turn is fit to be cached and can be memoized.
        else if(this.memoize && this.difficulty >= 7)
            memoizer.cacheBoard(boardAsString, Arrays.toString(lossList));

        // Choose a random column from within the best moves array.
        return bestMoves.get((int)(Math.random() * bestMoves.size()));


    }

    // The heart of decision-making.
    public int minimax(boolean aiTurn, int countDown, int alpha, int beta, int zeros){

        // If the game ends in a terminal-state OR max recursive depth is reached, evaluate the board.
        if (super.hasWon() || countDown == 0) return this.evaluateBoard(!aiTurn, zeros);

        String boardAsString = Arrays.toString(super.board);
        if(aiTurn && this.memoizer.dictionary.containsKey(boardAsString))
        {
            System.out.print("×");
            return this.memoizer.getMinMax(boardAsString, aiTurn);
        }

        // Start minMax at a minimum or maximum value depending on whether it is the minimizing or maximizing turn.
        int minMax = aiTurn? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        // Loop though every available column and update minMax accordingly to new evaluations.
        for(int i = 0; i < super.WIDTH; i++)
        {
            int col = checkOrder[i];
            // Only check the move if you're able to place a coin there.
            if(super.colIsOpen(col))
            {
                // Place a coin at the current column and get that boards evaluation.
                // Un-place that coin after evaluating the board so not to damage the normal playing board.
                super.placeCoin(col, (byte) (aiTurn? 2 : 1));
                int evaluation = minimax(
                        !aiTurn,
                        countDown - 1,
                        alpha, beta,
                        zeros - 1
                );
                undoLastMove(col);

                // Update minMax depending on whether it's the minimizing or maximizing turn.
                minMax = aiTurn?
                        Math.max(minMax, evaluation):
                        Math.min(minMax, evaluation);

                // Alpha-Beta pruning.
                // If we know there's already a better option somewhere in the tree, there is no reason to take this one.
                // This lets us avoid many unnecessary calculations.
                if(aiTurn) alpha = Math.max(alpha, evaluation);
                else beta = Math.min(beta, evaluation);
                if(beta <= alpha) break;
            }
        }
        return minMax;

    }


    // --------------------------------- //
    // Lesser Computation Methods.

    // If the AI remembers the best move for the given board, this method will return that move.
    // If not, then this method returns -1.
    public int rememberBestMove(String boardAsString){

        // Check if the boards been memoized.
        if(this.memoize)
        {
            int best = this.memoizer.getBestMove(boardAsString);
            if(best != -1)
            {
                System.out.println("The AI recognized a pattern.");
                return best;
            }
        }
        return -1;

    }

    // Prints an interpretation of the all seeing value and returns it.
    public double printAllSeeing(int zeros){

        double allSeeing = (double) Math.min(this.difficulty, zeros) / zeros;
        if(allSeeing >= 1) System.out.println("The AI has reached zenith.");
        allSeeing = Math.round(allSeeing * 10000) / 100.0;
        System.out.println("AI intelligence: " + allSeeing + " %");
        return allSeeing;

    }

    // Checks for whether a board can be won for a given player code.
    // If it can be won, the method returns the column in which the win occurs.
    // If not, the method returns -1.
    public int canWin(byte playerCode){

        for(int col = 0; col < super.WIDTH; col++)
            if(this.testWin(col, playerCode))
                return col;
        return -1;

    }

    // Adjusts recursive depth according to how long the previous move took.
    // If a move took too long to compute, lower recursive depth.
    // If a move took too little time to compute, raise recursive depth.
    public void doDynamicDifficulty(int zeros, long startTime){

        long elapsedTime = System.currentTimeMillis() - startTime;
        if(elapsedTime <= 400 && this.difficulty >= 4 && super.board.length - zeros >= 8)
        {
            this.difficulty += 2;
            System.out.println("The AI is closing in.");
        }
        else if (elapsedTime <= 1000)   this.difficulty++;
        else if (elapsedTime >= 7500 && this.difficulty > 6) this.difficulty = 6;
        else if (elapsedTime >= 4000 && this.difficulty > 5) this.difficulty--;

    }

    // Retrieves a loss list for the current board state.
    // A loss list represents the loss for each available move.
    public int[] getLossList(ArrayList<Integer> bestMoves, int zeros, double allSeeing){

        // Variables to note AI attitude.
        boolean willWin = false;
        int averageLoss = 0;
        int lossCount = 0;
        int playerTraps = 0;

        // Loop over all possible moves and collect a list of moves which all have the same max value.
        System.out.print("The AI is thinking ");
        int max = Short.MIN_VALUE;
        int baseDifficulty = this.difficulty + this.filledColumns();
        int[] lossList = new int[super.WIDTH];
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
                lossList[col] = loss;

                // Update AI attitudes.
                if(loss >= 100) willWin = true;
                else if(loss <= -100) playerTraps++;
                else
                {
                    averageLoss += loss;
                    lossCount++;
                }

                System.out.print("•");

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

        // Interpret attitude variables.
        System.out.println();
        if(lossCount != 0) averageLoss /= lossCount;
        if (willWin) System.out.println("The AI has formulated a plan.");
        else if(playerTraps >= 3) System.out.println(allSeeing == 1? "The AI accepts its defeat." : "The AI is being very cautious.");
        else if(averageLoss <= -20) System.out.println("The AI trying to plan.");

        // When in danger, increment recursive depth.
        this.emergencyComputations = this.dynamicDifficulty && playerTraps >= 3 || averageLoss <= -20;

        return lossList;

    }


    // --------------------------------- //
    // Scoring Methods.

    // Evaluates and "scores" a games terminal-state.
    // This method is the heart of the AI, as it determines what it values and thus how it tries to win.
    public int evaluateBoard(boolean aiTurn, int zeros) {

        int score = this.getNonTerminalPoints(!aiTurn, zeros);

        // If these points are for the player, make the score negative.
        // This symbolizes them working against the AI's cause.
        return aiTurn? score : -score;

    }

    // Returns the score component for a non-terminal board.
    public int getNonTerminalPoints(boolean aiTurn, int zeros){

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
                        // TODO: CHANGE TO BYTE ARRAY !!!!!!!!!
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
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn, zeros);
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
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn, zeros);
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
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn, zeros);
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
                    totalPoints += scoreIdentityStr(identity.toString(), aiTurn, zeros);
                }

            }
        return totalPoints;

    }

    // This method takes in an identity string representing the "form" of a group of four coins on the board.
    // All forms are not created equal in the aid of victory, therefore they deserve different scores.
    // If the game has been won, add a high amount of points scaled by how quick it was won.
    // The more zeros on the screen the fewer turns have passed, making it a good metric for the scalar.
    public int scoreIdentityStr(String identity, boolean aiTurn, int zeros){

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

            case "1111": playerPoints = 100 * zeros; break;
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

            case "2222": aiPoints = 100 * zeros; break;
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

    // Returns whether placing a coin at a given col will result in a victory for a given player.
    private boolean testWin(int col, byte playerCode){

        boolean victory = false;
        if (super.colIsOpen(col)) {
            super.placeCoin(col, playerCode);
            victory = super.hasWon();
            this.undoLastMove(col);
        }
        return victory;

    }


}
