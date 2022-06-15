public class Board{

    // --------------------------------- //
    // Instance variables.

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001b[33;1m";
    private static final String ANSI_RESET = "\u001B[0m";

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

        this.height = height;
        this.width = width;
        this.board = new byte[height * width];

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

        return switch(coin){
            case 0 -> "•";
            case 1 -> ANSI_RED    + "○" + ANSI_RESET;
            case 2 -> ANSI_YELLOW + "○" + ANSI_RESET;

            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

    }

    // Converts a (row, col) position on the board to an index for the byte array.
    public int getIndex(int row, int col){

        return row * this.width + col;

    }

    // Sets a specified value in the board.
    public void setValue(int row, int col, byte setTo){

        this.board[getIndex(row, col)] = setTo;

    }


    // --------------------------------- //


}
