public class Board{

    // --------------------------------- //
    // Instance variables.

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String[] PLAYER_COLORS = new String[]{
            "\u001B[31m", "\u001b[33m", "\u001b[32m", "\u001B[35m", "\u001b[36m",
    };

    // Where coin data is stored.
    public byte[] board;
    private final int height;
    private final int width;


    // --------------------------------- //
    // Constructor.

    public Board() {

        this(6, 7);

    }
    public Board(int height, int width) {

        // Make sure dimensions are each larger than 4, and width is limited to 10.
        this.height = Math.max(height, 4);
        this.width  = Math.max(Math.min(width, 10), 4);
        this.board  = new byte[this.height * this.width];

    }


    // --------------------------------- //
    // Game methods.

    // Places a coin at the first available spot.
    public void placeCoin(int col, byte player){

        for(int row = 0; row < this.height; row++)
            if(this.board[getIndex(row, col)] == 0)
            {
                this.board[getIndex(row, col)] = player;
                return;
            }

    }

    // Checks whether a game has been won.
    public boolean hasWon(byte player){

        return this.hasWonVertical(player)      ||
               this.hasWonHorizontal(player)    ||
               this.hasWonLeftDiagonal(player)  ||
               this.hasWonRightDiagonal(player) ;

    }


    // --------------------------------- //
    // Print methods.

    // Prints out the byte board as you would expect a board of connect four to appear.
    public  void print(){

        printBar();
        for(int i = this.height - 1; i >= 0; i--) {
            for (int j = 0; j < this.width; j++) {
                System.out.print(" " + formatCoinColor(this.board[getIndex(i, j)]) + " ");
            }
            System.out.println();
        }
        printBar();
        printColNumbers();

    }
    private void printBar(){

        // = Each number + Two spaces for every area between numbers + One at the start + One at the end
        // = x + 2(x - 1) + 1 + 1
        // = x + 2x - 2 + 2
        // = 3x
        // Amount of bars is three times the board's width.
        System.out.println("_".repeat(Math.max(0, 3 * this.width)));

    }
    private void printColNumbers(){

        StringBuilder numbers = new StringBuilder(" ");
        for(int i = 1; i <= this.width; i++) numbers.append(i).append("  ");
        System.out.println(numbers);

    }


    // --------------------------------- //
    // Helper methods.

    // Takes in the byte value of a coin and returns its string representation.
    private String formatCoinColor(byte coin){

        if(coin == 0) return "•";
        return PLAYER_COLORS[coin - 1] + "○" + ANSI_RESET;

    }

    // Converts a (row, col) position on the board to an index for the byte array.
    public int getIndex(int row, int col){

        return row * this.width + col;

    }

    // Sets a specified value in the board.
    public void setValue(int row, int col, byte setTo){

        this.board[getIndex(row, col)] = setTo;

    }

    // Checks whether a game has been won in a few different ways.
    // TODO: eventually think about how to merge all these methods into just one loop.
    private boolean hasWonHorizontal(byte player){

        for(int row = 0; row < this.height; row++)
            for (int col = 3; col < this.width; col++) {

                boolean victory = true;

                for(int offset = 0; offset < 4; offset++)
                    victory = victory && player == this.board[getIndex(row, col - offset)];

                if(victory) return true;

            }

        return false;

    }
    private boolean hasWonVertical(byte player){

        for(int row = 3; row < this.height; row++){
            for(int col = 0; col < this.width; col++){

                boolean victory = true;

                for(int offset = 0; offset < 4; offset++)
                    victory = victory && player == this.board[getIndex(row - offset, col)];

                if(victory) return true;

            }
        }
        return false;
    }
    private boolean hasWonRightDiagonal(byte player){

        for(int row = 3; row < this.height; row++) {
            for (int col = 3; col < this.width; col++) {

                boolean victory = true;

                for(int offset = 0; offset < 4; offset++)
                    victory = victory && player == this.board[getIndex(row - offset, col - offset)];

                if(victory) return true;
            }
        }
        return false;
    }
    private boolean hasWonLeftDiagonal(byte player){

        for(int row = 3; row < this.height; row++) {
            for (int col = this.width - 4; col >= 0; col--) {

                boolean victory = true;

                for(int offset = 0; offset < 4; offset++)
                    victory = victory && player == this.board[getIndex(row - offset, col + offset)];

                if(victory) return true;
            }
        }
        return false;
    }


    // --------------------------------- //


}
