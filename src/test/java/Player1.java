import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Player1 {
    static class Action {
        int row, col;
        public Action(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Random random = new Random(0);
        System.err.println("MOVE 1 1 1");
        while (true) {

            int opponentRow = in.nextInt();
            int opponentCol = in.nextInt();
            for(int i = 0;i<opponentRow;i++){
                String str = in.next();
                System.err.println(str);
            }
            int pawns = in.nextInt();
            for (int i = 0; i < pawns; i++) {
                int id = in.nextInt();
                int fuel = in.nextInt();
                int x = in.nextInt();
                int y =in.nextInt();
                System.err.printf("%d %d %d %d%n",id,fuel,x,y);
                for (int j = 0; j < 3; j++) {
                    String str = in.next();
                    System.err.println(str);
                }

            }
           
            System.out.println("SHOOT 0 1 4");
        }
    }
}
