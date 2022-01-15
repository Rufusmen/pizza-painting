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
import java.util.Set;
import java.util.stream.Collectors;

public class GameState {

    @Inject
    private Board board;

    @Inject
    private GraphicEntityModule graphicEntityModule;
    private List<Pawn> pawns = new ArrayList<>();

    private static final int WALL_ID = 42;

    public void drawInit(int bigOrigX, int bigOrigY, int bigCellSize, int i, int i1) {
        board.drawInit(bigOrigX, bigOrigY, bigCellSize, i1);
        pawns.forEach(pawn -> pawn.drawInit(bigOrigX, bigOrigY, bigCellSize, i, graphicEntityModule));
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

    public List<Action> getValidActions() {
        return new ArrayList<>();
    }

    public void init() {
        board.init();
        pawns.add(new Pawn(0).init(3, 0, new Vector2(6, 6)));
        pawns.add(new Pawn(1).init(3, 1, new Vector2(3, 3)));
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
        color1.forEach(v -> {
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
                    if (i >= board.cols || i < 0 || j < 0 || j >= board.rows) {
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

    public void draw() {
        board.draw();
        pawns.forEach(Pawn::draw);
    }
}
