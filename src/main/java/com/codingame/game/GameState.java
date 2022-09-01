package com.codingame.game;

import com.codingame.game.Action.ActionType;
import com.codingame.game.board.Board;
import com.codingame.game.board.Entity;
import com.codingame.game.board.Entity.Type;
import com.codingame.game.board.Pawn;
import com.codingame.game.util.Vector2;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main class containing game state and game logic
 */
public class GameState {

    @Inject
    private Board board;

    @Inject
    private GraphicEntityModule graphicEntityModule;
    private final List<Pawn> pawns = new ArrayList<>();
    private Type[][] prevView = new Type[0][0];

    private static final int WALL_ID = 42;

    private int maxShootDist;

    /**
     * Initializes all graphical entities
     *
     * @param pawnLineColor color of pawn lines
     * @param cellLineColor color of cell lines
     * @param c1            color of player1
     * @param c2            color of player2
     */
    public void drawInit(int pawnLineColor, int cellLineColor, int c1, int c2) {
        int cellSize = getCellSize();
        int bigOrigX = (1080 - board.rows * cellSize) / 2;
        int bigOrigY = 10;
        int c1D = 0xd1c004;
        int c2D = 0x0880bf;
        board.drawInit(bigOrigX, bigOrigY, cellSize, cellLineColor, c1, c2);
        pawns.forEach(pawn -> pawn.drawInit(bigOrigX, bigOrigY, cellSize, pawnLineColor, graphicEntityModule, c1D, c2D));
    }

    public void initTooltip(TooltipModule tooltip) {
        board.initTooltip(tooltip);
    }


    /**
     * @return list of string with board description for players' input.
     */
    public List<String> initialBoardInput() {
        List<String> inputView = new ArrayList<>();
        inputView.add(String.format("%d %d %d", board.rows, board.cols, maxShootDist));
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
        prevView = view;
        return inputView;
    }

    /**
     * @return list of strings with changes on board
     */
    public List<String> boardInput() {
        List<String> inputView = new ArrayList<>();
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
        for (int i = 0; i < prevView.length; i++) {
            for (int j = 0; j < prevView[i].length; j++) {
                if (prevView[i][j] != view[i][j]) {
                    inputView.add(String.format("%d %d %c", i, j, Entity.typeToChar(view[i][j])));
                }
            }
        }
        prevView = view;
        return inputView;
    }

    /**
     * @return map with players scores.
     */
    public Map<Integer, Integer> getScore() {
        Map<Integer, Integer> score = new HashMap<>();
        int score1 = 0;
        int score2 = 0;
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
            for (Type type : line) {
                switch (type) {
                    case COLOR1:
                        score1++;
                        break;
                    case COLOR2:
                        score2++;
                        break;
                    default:
                }
            }
        }
        score.put(0, score1);
        score.put(1, score2);
        return score;

    }

    /**
     * @param player id of a player
     * @return pawns information for given player
     */
    public List<String> pawnInput(int player) {
        List<String> inputView = new ArrayList<>();
        List<Pawn> myPawns = pawns.stream().filter(pawn -> pawn.getOwner() == player).collect(Collectors.toList());
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

    /**
     * @param random random number generator
     */
    public void init(Random random) {
        board.init(random);
        int pawnsNo = 1 + random.nextInt(3);
        maxShootDist = 5 + random.nextInt(6);
        int id = 0;
        int fuel = random.nextInt(20) + 20;
        while (pawnsNo-- > 0) {
            Vector2 startPos = new Vector2(2 + random.nextInt(board.rows - 4), random.nextInt(10));
            while (!board.isValidField(startPos) || !isValidPawnPlacement(startPos, 3)) {
                startPos = new Vector2(2 + random.nextInt(board.rows - 4), random.nextInt(10));
            }
            pawns.add(new Pawn(id++).init(3, 0, startPos, fuel));
            pawns.add(new Pawn(id++).init(3, 1, new Vector2(board.rows - 1 - startPos.getX(), board.cols - 1 - startPos.getY()), fuel));
        }
    }

    /**
     * @param player id of a player
     * @return number of pawns owned by given player
     */
    public int getPawnsCnt(int player) {
        return (int) pawns.stream().filter(pawn -> pawn.getOwner() == player).count();
    }

    /**
     * Resolves all actions. MOVE actions are resolved first. In case of collisions, movement of colliding pawns is rolled back.
     *
     * @param actions list of actions that should be resolved.
     */
    public void resolveActions(List<Action> actions) {
        pawns.forEach(p -> p.usedThisTurn = false);
        List<Vector2> prevPositions = pawns.stream().map(pawn -> pawn.position.clone()).collect(Collectors.toList());
        actions.stream().filter(action -> action.type.equals(ActionType.MOVE)).forEach(action -> {
            Pawn pawn = pawns.get(action.pawn);
            if (pawn.getOwner() == action.player.getIndex() && !pawn.usedThisTurn) {
                pawn.usedThisTurn = true;
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
            if (pawn.getOwner() == action.player.getIndex() && !pawn.usedThisTurn) {
                pawn.usedThisTurn = true;
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
            Pawn p = isOnPawn(v);
            if (p != null) {
                p.colorPawn(1, v);
            } else {
                board.color(1, v);
            }
        });
        color2.forEach(v -> {
            Pawn p = isOnPawn(v);
            if (p != null) {
                p.colorPawn(2, v);
            } else {
                board.color(2, v);
            }
        });
        pawns.forEach(p -> {
            p.checkOwnership();
            board.refill(p);
        });
    }

    private Pawn isOnPawn(Vector2 v) {
        return pawns.stream().filter(p -> p.isOnPawn(v.clone())).findFirst().orElse(null);
    }

    private List<Vector2> shoot(Pawn pawn, Action action) {
        List<Vector2> res = new ArrayList<>();
        Vector2 actual = pawn.position.clone();
        int range = action.range;
        int offset = pawn.offset + 1;
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
        range = Math.min(Math.min(range,pawn.fuel),maxShootDist);
        pawn.fuel-=range;
        while (range-- > 0 && board.isValidField(actual)) {
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

    private boolean isValidPawnPlacement(Vector2 position, int size) {
        if (checkPawnsCollisions(position)) {
            return false;
        }
        int[][] view = new int[board.rows][board.cols];
        for (int i = 0; i < board.rows; ++i) {
            for (int j = 0; j < board.cols; ++j) {
                view[i][j] = board.cells[i][j].type.equals(Type.WALL) ? WALL_ID : -1;
            }
        }
        for (int i = position.getX() - size / 2, ip = 0; ip < size; ++i, ++ip) {
            for (int j = position.getY() - size / 2, jp = 0; jp < size; ++j, ++jp) {
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

    private boolean checkPawnsCollisions(Vector2 position) {
        return pawns.stream().anyMatch(pawn -> Math.abs(pawn.position.getY() - position.getY()) < pawn.size
            || Math.abs(pawn.position.getX() - position.getX()) < pawn.size);
    }

    public int getCellSize() {
        return Math.min(1000 / board.rows, 1500 / board.cols);
    }


    /**
     * Updates all graphical entities
     */
    public void draw() {
        board.draw();
        pawns.forEach(Pawn::draw);
    }

    public void updateTooltip(TooltipModule tooltips) {
        pawns.forEach(p -> tooltips.setTooltipText(p.getGroup(), p.toString()));
    }
}
