import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Memoizer {

    // --------------------------------- //
    // Instance Variables.

    public HashMap<String, String> dictionary;

    // --------------------------------- //
    // Constructor.

    public Memoizer() {

        this.dictionary = new HashMap<>();
        try {
            Scanner s = new Scanner(new File("src/memo_ai_turn.txt"));
            while (s.hasNext()) {
                String next = s.nextLine();
                if (next.contains("=")) {
                    String[] halves = next.split("=");
                    this.dictionary.put(halves[0], halves[1]);
                }

            }
        }catch (Exception ignored){}

    }


    // --------------------------------- //
    // Methods.

    // Stores a board and its loss function paths in memory.
    public void cacheBoard(String board, String lossList){

        // Save data both to the proper hashmap and to the txt files.

        this.dictionary.put(board, lossList);

        String toSave = board + "=" + lossList + "\n";
        try {
            Files.write(
                    Paths.get("src/memo_ai_turn.txt"),
                    toSave.getBytes(),
                    StandardOpenOption.APPEND
            );
        }
        catch (Exception ignored){}

    }

    // Returns the best move for a given board.
    public int getBestMove(String board){

        String moves = this.dictionary.get(board);
        if(moves != null)
        {
            moves = moves.substring(1, moves.length() - 1);
            String[] split = moves.split(", ");

            ArrayList<Integer> bestMoves = new ArrayList<>();
            int max = Integer.MIN_VALUE + 1;
            for(int col = 0; col < split.length; col++)
            {
                int loss = Integer.parseInt(split[col]);
                if (loss > max)
                {
                    max = loss;
                    bestMoves.clear();
                    bestMoves.add(col);
                }
                else if (loss == max) bestMoves.add(col);
            }
            return bestMoves.get((int)(Math.random() * bestMoves.size()));

        }
        return -1;

    }

    // Returns the max or min value of a board.
    public int getMinMax(String board, boolean max){

        int minMax = max? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        String moves = this.dictionary.get(board);
        moves = moves.substring(1, moves.length() - 1);
        String[] split = moves.split(", ");
        for(String s : split)
        {
            int num = Integer.parseInt(s);
            minMax = max? Math.max(num, minMax) : Math.min(num, minMax);
        }
        return minMax;

    }



    // --------------------------------- //


}
