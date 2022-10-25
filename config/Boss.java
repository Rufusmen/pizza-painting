import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Player {
    static char[][] map;
    static int maxDist;
    static char co;
    private static boolean tryShootDir(int fuel, int x, int y, int dirX, int dirY){
        int range = Math.min(maxDist,fuel);
        x=x+2*dirX; y = y+2*dirY;
        try {
            for (int i = 0; i < range; i++) {
                if (map[x][y] == 'X')return false;
                if(map[x][y] != co && map[x][y] != '.')return true;
                x+=dirX;y+=dirY;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    private static boolean tryShoot(int id, int fuel, int x, int y){
        if(tryShootDir(fuel,x,y,1,0)){
            System.out.printf("SHOOT %d 2 %d%n",id,fuel);
            return true;
        }
        if(tryShootDir(fuel,x,y,-1,0)){
            System.out.printf("SHOOT %d 1 %d%n",id,fuel);
            return true;
        }
        if(tryShootDir(fuel,x,y,0,1)){
            System.out.printf("SHOOT %d 4 %d%n",id,fuel);
            return true;
        }
        if(tryShootDir(fuel,x,y,0,-1)){
            System.out.printf("SHOOT %d 3 %d%n",id,fuel);
            return true;
        }
        return false;
    }

    private static boolean isValidMove(int x, int y, int dirX, int dirY){
        x+=2*dirX;
        y+=2*dirY;
        try {
            if (dirX == 0) {
                return map[x][y] != 'X' && map[x + 1][y] != 'X' && map[x - 1][y] != 'X';
            } else {
                return map[x][y] != 'X' && map[x][y + 1] != 'X' && map[x][y - 1] != 'X';
            }
        }catch (Exception e){
            return false;
        }
    }
    private static void move(int id,int x,int y){
        if(isValidMove(x,y, 0,co == '1'?1:-1)){
            System.out.printf("MOVE %d %d%n",id,co == '1'? 4:3);
            return;
        }
        if(isValidMove(x,y,1,0)){
            System.out.printf("MOVE %d 2%n",id);
            return;
        }
        if(isValidMove(x,y,-1,0)){
            System.out.printf("MOVE %d 1%n",id);
            return;
        }
        System.out.printf("MOVE %d %d%n",id,co == '1'? 3:4);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Random random = new Random();
        co = Integer.toString(in.nextInt()).charAt(0);
        int row = in.nextInt();
        int col = in.nextInt();
        maxDist = in.nextInt();
        map = new char[row][col];
        for (int i = 0; i < row; i++) {
            String str = in.next();
            for (int j = 0; j < col; j++) {
                map[i][j] = str.charAt(j);
            }
        }
        while (true) {

            int diffs = in.nextInt();
            while (diffs-- > 0) {
                int x = in.nextInt();
                int y = in.nextInt();
                char c = in.next().charAt(0);
                map[x][y] = c;
            }
            int pawns = in.nextInt();
            for (int i = 0; i < pawns; i++) {
                int id = in.nextInt();
                int fuel = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                String str = in.next();
                if(!tryShoot(id,fuel,x,y)){
                    move(id,x,y);
                }
            }

        }
    }
}
