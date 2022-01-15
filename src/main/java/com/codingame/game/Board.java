package com.codingame.game;

import static com.codingame.game.Util.convert;

import com.codingame.game.Entity.Type;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import com.google.inject.Inject;

public class Board {

    @Inject
    private GraphicEntityModule graphicEntityModule;

    public Entity[][] cells;
    public int rows;
    public int cols;

    private Group entity;

    private int origX;
    private int origY;
    private int cellSize;


    public void init(){
        rows = 9;
        cols =11;
        cells = new Entity[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                cells[i][j] = new Entity();
            }
        }

        cells[0][0].type = Type.WALL;

    }

    public boolean isValidField(Vector2 v){
        int y = v.getY(),x = v.getX();
        return x>0 && y>0 && x<rows && y<cols && !cells[x][y].type.equals(Type.WALL);
    }

    public void drawInit(int origX, int origY, int cellSize, int lineColor) {
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
                        rectangle.setFillColor(0xffff);
                        break;
                    case COLOR1:
                        rectangle.setFillColor(0x00ffff);
                        break;
                    case COLOR2:
                        rectangle.setFillColor(0xff00ff);
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
                switch (cells[i][j].type){
                    case EMPTY:
                        cells[i][j].rectangle.setFillColor(0xffffff);
                        break;
                    case WALL:
                        cells[i][j].rectangle.setFillColor(0xffff);
                        break;
                    case COLOR1:
                        cells[i][j].rectangle.setFillColor(0x00ffff);
                        break;
                    case COLOR2:
                        cells[i][j].rectangle.setFillColor(0xff00ff);
                        break;
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

}
