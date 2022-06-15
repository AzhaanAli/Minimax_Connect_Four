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

        b.setValue(0, 0, (byte) 1);
        b.setValue(1, 1, (byte) 1);
        b.setValue(2, 2, (byte) 1);
        b.setValue(3, 3, (byte) 1);

        b.print();

        System.out.println(b.hasWon((byte) 1));

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