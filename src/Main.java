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

        Board b = new Board();
        b.placeCoin(0, (byte) 1);
        b.placeCoin(0, (byte) 2);
        b.placeCoin(0, (byte) 3);
        b.placeCoin(0, (byte) 4);
        b.placeCoin(1, (byte) 5);
        b.print();

    }


    // --------------------------------- //
    // Connect Four methods.

//    public boolean boardWon(byte[] board){
//
//        // Check for any horizontal wins.
//        for(int i = 0; i < )
//        {
//
//        }
//
//    }

    // --------------------------------- //
    // Helper methods.







}