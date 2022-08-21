package com.codingame.game;

import com.codingame.game.Action.ActionType;
import com.codingame.game.Entity.Type;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class GameState {

    @Inject
    private Board board;

    @Inject
    private GraphicEntityModule graphicEntityModule;
    private List<Pawn> pawns = new ArrayList<>();

    private static final int WALL_ID = 42;

    public void drawInit( int i, int i1,int c1,int c2) {
        int cellSize = getCellSize();
        int bigOrigX = (1080-board.rows*cellSize)/2;
        int bigOrigY = 10;
        board.drawInit(bigOrigX, bigOrigY, cellSize, i1,c1,c2);
        pawns.forEach(pawn -> pawn.drawInit(bigOrigX, bigOrigY, cellSize, i, graphicEntityModule,c1,c2));
    }

    public List<String> boardInput() {
        List<String> inputView = new ArrayList<>();
        inputView.add(String.format("%d %d", board.rows, board.cols));
        Type[][] view = new Type[board.rows][board.cols];
        for (int i = 0; i < board.rows; ++i) {
            for (int j = 0; j < board.cols; ++j) {
                view[i][j] = board.cells[i][j].type;
            }
        }
        pawns.forEach(pawn -> {
            for (int i = pawn.position.getX() - pawn.offset, ip = 0; ip < pawn.size; ++i, ++ip) {
                for (int j = pawn.position.getY() - pawn.offset, jp = 0; jp < pawn.size; ++j, ++jp) {
                    view[i][j] = pawn.pawnColors[ip][jp].color == 1 ? Type.COLOR1 : Type.COLOR2;
                }
            }
        });
        for (Type[] line : view
        ) {
            inputView.add(Joiner.on("").join(Arrays.stream(line).map(Entity::typeToChar).collect(Collectors.toList())));
        }
        return inputView;
    }

    public List<String> pawnInput(int player) {
        List<String> inputView = new ArrayList<>();
        List<Pawn> myPawns = pawns.stream().filter(pawn -> pawn.owner == player).collect(Collectors.toList());
        inputView.add(String.valueOf(myPawns.size()));
        myPawns.forEach(p -> {
            inputView.add(String.format("%d %d %d %d", p.id, p.fuel, p.position.getX(), p.position.getY()));
            for (int i = 0; i < p.size; i++) {
                inputView.add(Arrays.stream(p.pawnColors[i]).map(x -> x.color).collect(StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append)
                    .toString());
            }
        });
        return inputView;
    }

    public void init(Random random) {
        board.init(random);
        int pawnsNo = 1+random.nextInt(3);
        int id=0;
        while (pawnsNo-->0){
            Vector2 startPos = new Vector2( 2+random.nextInt(board.rows-4),random.nextInt(10));
            while (!board.isValidField(startPos) || !isValidPawnPlacement(startPos,3)){
                startPos = new Vector2( 2+random.nextInt(board.rows-4),random.nextInt(10));
            }
            pawns.add(new Pawn(id++).init(3,0,startPos));
            pawns.add(new Pawn(id++).init(3,1,new Vector2(board.rows-1-startPos.getX(), board.cols-1-startPos.getY())));
        }
    }

    public int getPawnsCnt(int player) {
        return (int) pawns.stream().filter(pawn -> pawn.owner == player).count();
    }

    public void resolveActions(List<Action> actions) {
        List<Vector2> prevPositions = pawns.stream().map(pawn -> pawn.position.clone()).collect(Collectors.toList());
        actions.stream().filter(action -> action.type.equals(ActionType.MOVE)).forEach(action -> {
            Pawn pawn = pawns.get(action.pawn);
            if (pawn.owner == action.player.getIndex()) {
                pawn.move(action.direction);
            }
        });
        while (true) {
            List<Integer> collisions = checkCollisions();
            if (collisions.isEmpty()) {
                break;
            }
            collisions.forEach(index -> pawns.get(index).setPosition(prevPositions.get(index)));
        }
        Set<Vector2> color1 = new HashSet<>();
        Set<Vector2> color2 = new HashSet<>();
        actions.stream().filter(action -> action.type.equals(ActionType.SHOOT)).forEach(action -> {
            Pawn pawn = pawns.get(action.pawn);
            if (pawn.owner == action.player.getIndex()) {
                if (action.player.getIndex() == 0) {
                    color1.addAll(shoot(pawn, action));
                } else {
                    color2.addAll(shoot(pawn, action));
                }
            }
        });
        Set<Vector2> intersection = new HashSet<>(color1);
        intersection.retainAll(color2);
        color1.removeAll(intersection);
        color2.removeAll(intersection);
        color1.forEach(v -> { //FIXME
            Pawn p = isOnPawn(v.clone());
            if (p != null) {
                p.colorPawn(1,v);
            } else {
                board.color(1,v);
            }
        });
        color2.forEach(v -> {
            Pawn p = isOnPawn(v.clone());
            if (p != null) {
                p.colorPawn(2,v);
            } else {
                board.color(2,v);
            }
        });
        pawns.forEach(p -> {
            p.checkOwnership();
            board.refill(p);
        });
    }

    private Pawn isOnPawn(Vector2 v) {
        return pawns.stream().filter(p -> p.isOnPawn(v)).findFirst().orElse(null);
    }

    private List<Vector2> shoot(Pawn pawn, Action action) {
        List<Vector2> res = new ArrayList<>();
        Vector2 actual = pawn.position.clone();
        int range = action.range;
        int offset = pawn.offset+1;
        Vector2 step;
        switch (action.direction) {
            case 1:
                actual.sub(offset, 0);
                step = new Vector2(-1, 0);
                break;
            case 2:
                actual.add(offset, 0);
                step = new Vector2(1, 0);
                break;
            case 3:
                actual.sub(0, offset);
                step = new Vector2(0, -1);
                break;
            case 4:
                actual.add(0, offset);
                step = new Vector2(0, 1);
                break;
            default:
                step = new Vector2(0, 0);
        }
        while (range--> 0 && pawn.fuel--> 0 && board.isValidField(actual)) {
            res.add(actual.clone());
            actual.add(step);
        }
        return res;
    }

    private List<Integer> checkCollisions() {
        Set<Integer> list = new HashSet<>();
        int[][] view = new int[board.rows][board.cols];
        for (int i = 0; i < board.rows; ++i) {
            for (int j = 0; j < board.cols; ++j) {
                view[i][j] = board.cells[i][j].type.equals(Type.WALL) ? WALL_ID : -1;
            }
        }
        for (int p = 0; p < pawns.size(); p++) {
            Pawn pawn = pawns.get(p);
            for (int i = pawn.position.getX() - pawn.offset, ip = 0; ip < pawn.size; ++i, ++ip) {
                for (int j = pawn.position.getY() - pawn.offset, jp = 0; jp < pawn.size; ++j, ++jp) {
                    if (i >= board.rows || i < 0 || j < 0 || j >= board.cols) {
                        list.add(p);
                        continue;
                    }
                    if (view[i][j] != -1) {
                        list.add(p);
                        if (view[i][j] != WALL_ID) {
                            list.add(view[i][j]);
                        }
                    }
                    view[i][j] = p;
                }
            }
        }
        return new ArrayList<>(list);
    }

    private boolean isValidPawnPlacement(Vector2 position,int size){
        if(checkPawnsCollisions(position)){
            return false;
        }
        int[][] view = new int[board.rows][board.cols];
        for (int i = 0; i < board.rows; ++i) {
            for (int j = 0; j < board.cols; ++j) {
                view[i][j] = board.cells[i][j].type.equals(Type.WALL) ? WALL_ID : -1;
            }
        }
        for (int i = position.getX() - size/2, ip = 0; ip < size; ++i, ++ip) {
            for (int j = position.getY() - size/2, jp = 0; jp < size; ++j, ++jp) {
                if (i >= board.rows || i < 0 || j < 0 || j >= board.cols) {
                    return false;
                }
                if (view[i][j] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkPawnsCollisions(Vector2 position){
        return pawns.stream().anyMatch(pawn -> Math.abs(pawn.position.getY()-position.getY()) < pawn.size || Math.abs(pawn.position.getX()-position.getX()) < pawn.size);
    }

    public int getCellSize(){
        return Math.min(1000/board.rows,1500/ board.cols);
    }

    public void draw() {
        board.draw();
        pawns.forEach(Pawn::draw);
    }

    public int getBoardSize(){
        return board.getSize();
    }
}
