package com.codingame.game;

import static com.codingame.game.Util.convert;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import java.util.Arrays;

public class Pawn extends Entity {

    public int size;
    public Vector2 position;
    public PawnColors[][] pawnColors;
    public int id;
    public int fuel;
    private Group group;
    private int cellSize;
    private int origY;
    private int origX;
    public int offset;
    private int color1;
    private int color2;

    public Pawn(int id) {
        this.id = id;
    }

    public Pawn init(int size, int player, Vector2 position) {
        this.position = position;
        this.fuel = 10;
        owner = player;
        type = Type.PAWN;
        this.size = size;
        offset=size/2;
        pawnColors = new PawnColors[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pawnColors[i][j] = new PawnColors(player+1);
            }
        }
        return this;
    }

    public void checkOwnership() {
        int ownerColorCnt = Arrays.stream(pawnColors).mapToInt(a -> (int) Arrays.stream(a).filter(b -> b.color == owner+1).count()).sum();
        if (ownerColorCnt <= (size * size / 2)) {
            switchOwner();
        }
    }

    public void drawInit(int origX, int origY, int cellSize, int lineColor, GraphicEntityModule graphicEntityModule,int c1,int c2) {
        group = graphicEntityModule.createGroup();
        this.origX = origX;
        this.origY = origY;
        this.cellSize = cellSize;
        color1=c1;
        color2=c2;
        for (int i = position.getX() - offset, ip = 0; ip < size; ++i, ++ip) {
            for (int j = position.getY() - offset, jp = 0; jp < size; ++j, ++jp) {
                Rectangle rectangle = graphicEntityModule.createRectangle()
                    .setY(convert(origX, cellSize, i))
                    .setX(convert(origY, cellSize, j))
                    .setWidth(cellSize)
                    .setHeight(cellSize)
                    .setLineWidth(5)
                    .setLineColor(lineColor);
                switch (pawnColors[ip][jp].color) {
                    case 1:
                        rectangle.setFillColor(c1);
                        break;
                    case 2:
                        rectangle.setFillColor(c2);
                        break;
                    default:
                }
                pawnColors[ip][jp].rectangle = rectangle;
                group.add(rectangle);
            }
        }
    }

    public void draw() {
        for (int i = position.getX() - offset, ip = 0; ip < size; ++i, ++ip) {
            for (int j = position.getY() - offset, jp = 0; jp < size; ++j, ++jp) {
                Rectangle rec = pawnColors[ip][jp].rectangle;
                rec.setY(convert(origX, cellSize, i))
                    .setX(convert(origY, cellSize, j));
                switch (pawnColors[ip][jp].color) {
                    case 1:
                        rec.setFillColor(color1);
                        break;
                    case 2:
                        rec.setFillColor(color2);
                        break;
                    default:
                }
            }
        }
    }

    public void move(int direction) {
        switch (direction) {
            case 1:
                position.sub(1, 0);
                break;
            case 2:
                position.add(1, 0);
                break;
            case 3:
                position.sub(0, 1);
                break;
            case 4:
                position.add(0, 1);
                break;
        }
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void colorPawn(int color, Vector2 vector2) {
        vector2.sub(position);
        vector2.add(offset,offset);
        pawnColors[vector2.getX()][vector2.getY()].color = color;
    }

    public boolean isOnPawn(Vector2 v) {
        v.sub(this.position);
        v.abs();
        return Math.max(v.getX(), v.getY()) <= offset;
    }
}

