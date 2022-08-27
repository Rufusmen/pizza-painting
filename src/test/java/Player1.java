import java.util.Random;
import java.util.Scanner;

public class Player1 {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Random random = new Random();
        int co = in.nextInt();
        int row = in.nextInt();
        int col = in.nextInt();
        char[][] map = new char[row][col];
        for (int i = 0; i < row; i++) {
            String str = in.next();
            for (int j = 0; j < col; j++) {
                map[i][j] = str.charAt(j);
            }
        }
        int cycle = 0;
        while (true) {

            int diffs = in.nextInt();
            while (diffs-- > 0) {
                int x = in.nextInt();
                int y = in.nextInt();
                char c = in.next().charAt(0);
                //System.err.printf("%d %d %c%n",x,y,c);
                map[x][y] = c;
            }
            int pawns = in.nextInt();
            for (int i = 0; i < pawns; i++) {
                int id = in.nextInt();
                int fuel = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                System.err.println(fuel);
                for (int j = 0; j < 3; j++) {
                    String str = in.next();
                    //System.err.println(str);
                }
                if (cycle % 4 == 0) {
                    System.out.printf("SHOOT %d %d 5%n", id, cycle >= 16 ? cycle / 4 : cycle / 4 + 1);
                } else {
                    System.out.printf("MOVE %d %d%n", id, random.nextInt(4)+1);
                }

            }
            cycle++;
            if (cycle >= 20) {
                cycle -= 20;
            }
        }
    }
}
