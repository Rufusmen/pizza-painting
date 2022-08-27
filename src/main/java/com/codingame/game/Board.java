package com.codingame.game;

import static com.codingame.game.Util.convert;
import static com.codingame.game.util.LineSeparator.lines;

import com.codingame.game.Entity.Type;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Board {

    @Inject
    private GraphicEntityModule graphicEntityModule;

    public Entity[][] cells;
    public int rows;
    public int cols;

    private int color1;
    private int color2;

    private Group entity;

    private int origX;
    private int origY;
    private int cellSize;
    private Random random;


    public void init(Random random){
        this.random = random;
        rows = 20 + random.nextInt(10);
        cols = 30 + random.nextInt(10);
        cells = new Entity[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                cells[i][j] = new Entity();
            }
        }

        generateWalls();

    }

    public boolean isValidField(Vector2 v){
        int y = v.getY(),x = v.getX();
        return x>=0 && y>=0 && x<rows && y<cols && !cells[x][y].type.equals(Type.WALL);
    }

    public void drawInit(int origX, int origY, int cellSize, int lineColor,int c1, int c2) {
        color1 = c1;
        color2 = c2;
        this.origX = origX;
        this.origY = origY;
        this.cellSize = cellSize;
        this.entity = graphicEntityModule.createGroup();

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                Rectangle rectangle = graphicEntityModule.createRectangle()
                    .setY(convert(origX, cellSize, i))
                    .setX(convert(origY, cellSize, j))
                    .setWidth(cellSize)
                    .setHeight(cellSize)
                    .setLineWidth(5)
                    .setLineColor(lineColor);
                switch (cells[i][j].type){
                    case EMPTY:
                        rectangle.setFillColor(0xffffff);
                        break;
                    case WALL:
                        rectangle.setFillColor(0);
                        break;
                    case COLOR1:
                        rectangle.setFillColor(c1);
                        break;
                    case COLOR2:
                        rectangle.setFillColor(c2);
                        break;
                }
                cells[i][j].rectangle = rectangle;
                entity.add(rectangle);
            }
        }
    }

    public void draw()
    {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                switch (cells[i][j].type) {
                    case EMPTY:
                        cells[i][j].rectangle.setFillColor(0xffffff);
                        break;
                    case WALL:
                        cells[i][j].rectangle.setFillColor(0);
                        break;
                    case COLOR1:
                        cells[i][j].rectangle.setFillColor(color1);
                        break;
                    case COLOR2:
                        cells[i][j].rectangle.setFillColor(color2);
                }
            }
        }
    }

    public void color(int color,Vector2 v){
        cells[v.getX()][v.getY()].color(color);

    }

    public void refill(Pawn p) {
        int offset = p.size / 2;
        for (int i = p.position.getX() - offset, ip = 0; ip < p.size; ++i, ++ip) {
            for (int j = p.position.getY() - offset, jp = 0; jp < p.size; ++j, ++jp) {
                if (cells[i][j].getOwner() == p.owner) {
                    p.fuel++;
                }
            }
        }
    }

    private void generateWalls(){
        int wallsNo = random.nextInt(rows*cols/200);
        while (wallsNo-->0){
            List<List<Boolean>> wall =  lines(WallTemplate.walls.get(random.nextInt(WallTemplate.walls.size()))).map(row ->
                 row.codePoints().mapToObj(c -> (char) c).map(c -> c == 'X').collect(Collectors.toList())
            ).collect(Collectors.toList());
            setWall(rotate(random.nextInt(4),toMatrix(wall)));
        }
    }



    private Boolean[][] toMatrix(List<List<Boolean>> wall){
        Boolean[][] tmpMatrix = new Boolean[wall.size()][wall.get(0).size()];
        for(int i = 0; i < wall.size();i++){
            List<Boolean> lst = wall.get(i);
            for(int j = 0; j<lst.size();j++){
                tmpMatrix[i][j] = lst.get(j);
            }
        }
        return tmpMatrix;
    }

    private void setWall(Boolean[][] matrix){
        int posX = random.nextInt(cols/2);
        int posY = random.nextInt(rows-matrix[0].length-1);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if(matrix[i][j]){
                    cells[j+posY][i+posX].type=Type.WALL;
                    cells[rows-posY-j-1][cols-posX-i-1].type=Type.WALL;
                }
            }
        }

    }

    private Boolean[][] rotate(int rotations, Boolean[][] matrix) {
        while (rotations--> 0) {
            Boolean[][] tmpMatrix = new Boolean[matrix[0].length][matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                        tmpMatrix[j][i] = matrix[i][j];
                }
            }
            matrix = tmpMatrix;
        }
        return matrix;
    }

    public int getSize(){
        return cols*rows;
    }

}
