package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class Referee extends AbstractReferee {
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private Provider<GameState> gameStateProvider;

    private GameState state;
    private List<Action> validActions;
    private Random random;
    
    @Override
    public void init() {
        random = new Random(gameManager.getSeed());
        state = gameStateProvider.get();
        state.init();
        drawBackground();
        drawHud();
        drawGrids();



        gameManager.setFrameDuration(600);

        if (gameManager.getLeagueLevel() == 1) {
            gameManager.setMaxTurns(9);
        } else {
            gameManager.setMaxTurns(9 * 9);
        }
        
        validActions = getValidActions();
    }

    private void drawBackground() {
        graphicEntityModule.createSprite()
                .setImage("Background.jpg")
                .setAnchor(0);
        graphicEntityModule.createSprite()
                .setImage("logo.png")
                .setX(280)
                .setY(915)
                .setAnchor(0.5);
        graphicEntityModule.createSprite()
                .setImage("logoCG.png")
                .setX(1920 - 280)
                .setY(915)
                .setAnchor(0.5);
    }

    private void drawGrids() {
        int bigCellSize = 100;
        int bigOrigX = (int) Math.round(1920 / 2 - bigCellSize);
        int bigOrigY = (int) Math.round(1080 / 2 - bigCellSize);
        state.drawInit(5, 5, bigCellSize, 0, 0xf9b700);
    }
    
    private void drawHud() {
        for (Player player : gameManager.getPlayers()) {
            int x = player.getIndex() == 0 ? 280 : 1920 - 280;
            int y = 220;

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

            Text text = graphicEntityModule.createText(player.getNicknameToken())
                    .setX(x)
                    .setY(y + 120)
                    .setZIndex(20)
                    .setFontSize(40)
                    .setFillColor(0xffffff)
                    .setAnchor(0.5);

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

    private void sendPlayerInputs() {
        List<String> board = state.boardInput();
        for (Player p : gameManager.getActivePlayers()) {
            p.setPawns(state.getPawnsCnt(p.getIndex()));
            board.forEach(p::sendInputLine);
            state.pawnInput(p.getIndex()).forEach(p::sendInputLine);
            p.execute();
        }
    }


    private void setWinner(Player player) {
        gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
        player.setScore(10);
        endGame();
    }

    private List<Action> getValidActions() {
        List<Action> validActions;
        validActions = state.getValidActions();

        Collections.shuffle(validActions, random);
        return validActions;
    }

    @Override
    public void gameTurn(int turn) {
        
        sendPlayerInputs();

        // Read inputs
        state.draw();
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
    }

    private void endGame() {
        gameManager.endGame();

        Player p0 = gameManager.getPlayers().get(0);
        Player p1 = gameManager.getPlayers().get(1);
        if (p0.getScore() > p1.getScore()) {
            p1.hud.setAlpha(0.3);
        }
        if (p0.getScore() < p1.getScore()) {
            p0.hud.setAlpha(0.3);
        }
    }
}
