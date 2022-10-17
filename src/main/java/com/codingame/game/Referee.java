package com.codingame.game;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main class of the game.
 */
public class Referee extends AbstractReferee {

    @Inject
    private MultiplayerGameManager<Player> gameManager;
    @Inject
    private GraphicEntityModule graphicEntityModule;
    @Inject
    private Provider<GameState> gameStateProvider;

    @Inject
    private TooltipModule tooltips;
    @Inject
    private EndScreenModule endScreenModule;

    private GameState state;

    private final Map<Integer,Text> texts = new HashMap<>();

    @Override
    public void init() {
        Random random = new Random(gameManager.getSeed());
        state = gameStateProvider.get();
        state.init(random);
        drawBackground();
        drawHud();
        drawGrids();

        state.initTooltip(tooltips);

        sendInitialInput();

        gameManager.setFrameDuration(500);
        gameManager.setTurnMaxTime(50);
        gameManager.setMaxTurns(250);

    }

    private void drawBackground() {
        graphicEntityModule.createSprite()
            .setImage("Background.jpg")
            .setAnchor(0);
        graphicEntityModule.createSprite()
            .setImage("logo_painting.png")
            .setX(1530)
            .setY(220)
            .setAnchor(0);
    }

    private void drawGrids() {
        state.drawInit(0, 0,0xf7ef05, 0x22a1e4);
    }

    private void drawHud() {
        for (Player player : gameManager.getPlayers()) {
            int x = 1920 - 240;
            int y = player.getIndex() == 0 ? 500 : 1080 - 240;

            graphicEntityModule
                .createRectangle()
                .setWidth(140)
                .setHeight(140)
                .setX(x - 70)
                .setY(y - 70)
                .setLineWidth(0)
                .setFillColor(player.getColorToken());

            graphicEntityModule
                .createRectangle()
                .setWidth(120)
                .setHeight(120)
                .setX(x - 60)
                .setY(y - 60)
                .setLineWidth(0)
                .setFillColor(0xffffff);

            Text text = graphicEntityModule.createText(player.getNicknameToken() + "\nScore: 0")
                .setX(x + 10)
                .setY(y + 140)
                .setZIndex(20)
                .setFontSize(40)
                .setFillColor(0)
                .setAnchor(0.5);
            texts.put(player.getIndex(),text);

            Sprite avatar = graphicEntityModule.createSprite()
                .setX(x)
                .setY(y)
                .setZIndex(20)
                .setImage(player.getAvatarToken())
                .setAnchor(0.5)
                .setBaseHeight(116)
                .setBaseWidth(116);

            player.hud = graphicEntityModule.createGroup(text, avatar);
        }
    }

    private void sendInitialInput() {
        List<String> board = state.initialBoardInput();
        for (Player p : gameManager.getActivePlayers()) {
            p.sendInputLine((p.getIndex() + 1) + " " + state.initialConsts());
            board.forEach(p::sendInputLine);
        }
    }

    private void sendPlayerInputs() {
        List<String> board = state.boardInput();
        for (Player p : gameManager.getActivePlayers()) {
            p.setPawns(state.getPawnsCnt(p.getIndex()));
            if(p.pawns == 0){
                p.setScore(0);
                onEnd();
            }
            p.sendInputLine(String.valueOf(board.size()));
            board.forEach(p::sendInputLine);
            state.pawnInput(p.getIndex()).forEach(p::sendInputLine);
            p.execute();
        }
    }


    private void setWinner(Player player) {
        gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
    }


    @Override
    public void gameTurn(int turn) {

        sendPlayerInputs();

        // Read inputs
        List<Action> actions = new ArrayList<>();
        for (Player player : gameManager.getActivePlayers()) {
            try {
                actions.addAll(player.getActions());
            } catch (NumberFormatException e) {
                player.deactivate("Wrong output!");
                player.setScore(-1);
                endGame();
            } catch (TimeoutException e) {
                gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
                player.deactivate(player.getNicknameToken() + " timeout!");
                player.setScore(-1);
                endGame();
            }
        }
        state.resolveActions(actions);
        state.updateTooltip(tooltips);
        state.draw();
        updateScore();
    }

    private void updateScore() {
        Map<Integer, Integer> score = state.getScore();
        Player p0 = gameManager.getPlayers().get(0);
        Player p1 = gameManager.getPlayers().get(1);
        p0.setScore(score.get(0));
        p1.setScore(score.get(1));
        texts.get(0).setText(p0.getNicknameToken() + "\nScore: " + score.get(0));
        texts.get(1).setText(p1.getNicknameToken() + "\nScore: " + score.get(1));
    }

    @Override
    public void onEnd(){
        endGame();
        endScreenModule.setTitleRankingsSprite("logoend.png");
        endScreenModule.setScores(gameManager.getPlayers().stream().mapToInt(p -> p.getScore()).toArray());
    }

    private void endGame() {

        gameManager.endGame();

        Player p0 = gameManager.getPlayers().get(0);
        Player p1 = gameManager.getPlayers().get(1);
        if (p0.getScore() > p1.getScore()) {
            p1.hud.setAlpha(0.3);
            setWinner(p0);
        }
        if (p0.getScore() < p1.getScore()) {
            p0.hud.setAlpha(0.3);
            setWinner(p1);
        }
    }
}
