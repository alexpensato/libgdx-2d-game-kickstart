package com.pensatocode.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.pensatocode.sfs.SfsGame;
import com.pensatocode.sfs.objects.Fighter;
import com.pensatocode.sfs.resources.Assets;
import com.pensatocode.sfs.resources.GlobalVariables;

public class GameScreen implements Screen, InputProcessor {

    private final SfsGame game;

    /**
     *    OrthographicCamera is a camera with orthographic projection, ideal for 2D games.
     */
    private final OrthographicCamera camera;

    /**
     *     ExtendedViewport is a viewport that keeps the aspect ratio by scaling the world up to take the whole screen,
     *  but without stretching the screen. It allows a player with a bigger screen to see more of the game world.
     *  It is similar to ScreenViewport, but it allows the world to be bigger than the screen.
     *     Other viewports options are:
     *     - ScreenViewport is a viewport that keeps the aspect ratio by scaling the world up to take the whole screen,
     *  but without stretching the screen. It allows a player with a bigger screen to see more of the game world;
     *     - FillViewport is a viewport that keeps the aspect ratio by scaling the world up to take the whole screen,
     *  but without stretching the screen;
     *     - FitViewport is a viewport that keeps the aspect ratio by scaling the world down to fit the screen,
     *  and adding black bars (letterboxing) for the remaining space;
     *     - StretchViewport is a viewport that keeps the aspect ratio by scaling the world up to fit the screen,
     *  stretching it if necessary.
     */
    private final ExtendViewport viewport;

    // game
    private GlobalVariables.Difficulty difficulty = GlobalVariables.Difficulty.EASY;

    // rounds
    private int roundsWon = 0;
    private int roundsLost = 0;
    private static final float MAX_ROUND_TIME = 99.99f;
    private float roundTime = MAX_ROUND_TIME;

    // fonts
    private BitmapFont smallFont;
    private BitmapFont mediumFont;
    private BitmapFont largeFont;
    private static final Color DEFAULT_FONT_COLOR = Color.WHITE;

    // HUD
    private static final Color HEALTH_BAR_COLOR = Color.RED;
    private static final Color HEALTH_BAR_BACKGROUND_COLOR = GlobalVariables.GOLD;

    // background/ring
    private Texture backgroundTexture;
    private Texture frontRopesTexture;
    private static final float RING_MIN_X = 7f;
    private static final float RING_MAX_X = 60f;
    private static final float RING_MIN_Y = 4f;
    private static final float RING_MAX_Y = 22f;

//    private static final float RING_SLOPE = (RING_MAX_Y - RING_MIN_Y) / (RING_MAX_X - RING_MIN_X);
    private static final float RING_SLOPE = 3.16f;

    // fighters
    private static final float PLAYER_START_POSITION_X = 16f;
    private static final float OPPONENT_START_POSITION_X = 51f;
    private static final float FIGHTER_START_POSITION_Y = 15f;
    private static final float FIGHTER_CONTACT_DISTANCE_X = 7.5f;
    private static final float FIGHTER_CONTACT_DISTANCE_Y = 1.5f;

    public GameScreen(SfsGame newGame) {
        game = newGame;

        // Set up the camera
        camera = new OrthographicCamera();

        // Using a viewport is not mandatory, but it's a good practice
        // You can achieve similar results by using camera.zoom, camera.translate and camera.position
        // Another alternative to avoid stretching and cropping is:
        // viewport = new FitViewport(GlobalVariables.WORLD_WIDTH, GlobalVariables.WORLD_HEIGHT, camera);
        viewport = new ExtendViewport(
                GlobalVariables.WORLD_WIDTH,
                GlobalVariables.MIN_WORLD_HEIGHT,
                GlobalVariables.WORLD_WIDTH,
                GlobalVariables.WORLD_HEIGHT,
                camera
        );

        // create the game area
        createGameArea();

        // set up the fonts
        setUpFonts();

        // initialize the fighters
        game.player().getReady(PLAYER_START_POSITION_X, FIGHTER_START_POSITION_Y);
        game.opponent().getReady(OPPONENT_START_POSITION_X, FIGHTER_START_POSITION_Y);
    }

    private void createGameArea() {
        this.backgroundTexture = game.assets().manager().get(Assets.BACKGROUND_TEXTURE);
        this.frontRopesTexture = game.assets().manager().get(Assets.FRONT_ROPES_TEXTURE);
    }

    private void setUpFonts() {
        smallFont = game.assets().manager().get(Assets.SMALL_FONT);
        smallFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        smallFont.setColor(DEFAULT_FONT_COLOR);
        smallFont.setUseIntegerPositions(false);

        mediumFont = game.assets().manager().get(Assets.MEDIUM_FONT);
        mediumFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        mediumFont.setColor(DEFAULT_FONT_COLOR);
        mediumFont.setUseIntegerPositions(false);

        largeFont = game.assets().manager().get(Assets.LARGE_FONT);
        largeFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        largeFont.setColor(DEFAULT_FONT_COLOR);
        largeFont.setUseIntegerPositions(false);
    }

    @Override
    public void show() {
        // process user input
        Gdx.input.setInputProcessor(this);

        //game.opponent().block();
    }

    @Override
    public void render(float delta) {
        // clear the screen with black. the arguments are RGBA, so it's black with full opacity.
        // this color is the background color of the game and will be visible
        // when the background texture is not fully opaque or when the viewport is letterboxed
        ScreenUtils.clear(0, 0, 0, 1);

        // update the game
        update(delta);

        // set the sprite batch to use the camera's coordinate system
        game.batch().setProjectionMatrix(viewport.getCamera().combined);

        // begin drawing
        game.batch().begin();

        // draw background
        game.batch().setColor(Color.WHITE);
        game.batch().draw(
                backgroundTexture,
                0,
                0,
                backgroundTexture.getWidth() * GlobalVariables.WORLD_SCALE,
                backgroundTexture.getHeight() * GlobalVariables.WORLD_SCALE
        );

        // draw the fighters
        renderFighters();

        // draw the front ropes
        game.batch().setColor(Color.WHITE);
        game.batch().draw(
                frontRopesTexture,
                0,
                0,
                frontRopesTexture.getWidth() * GlobalVariables.WORLD_SCALE,
                frontRopesTexture.getHeight() * GlobalVariables.WORLD_SCALE
        );

        // draw the HUD
        renderHUD();

        // end drawing
        game.batch().end();
    }

    private void renderFighters() {
        // use the y coordinate of the fighters' position to determine which one is in front
        if (game.player().getPosition().y > game.opponent().getPosition().y) {
            // draw the player
            game.player().render(game.batch());
            // draw the opponent
            game.opponent().render(game.batch());
        } else {
            // draw the opponent
            game.opponent().render(game.batch());
            // draw the player
            game.player().render(game.batch());
        }
    }

    private void renderHUD() {
        float HUDMargin = 1f;

        // draw the rounds won to lost ratio
        smallFont.draw(game.batch(), "WINS: " + roundsWon + " - " + roundsLost,
                HUDMargin,
                GlobalVariables.WORLD_HEIGHT - HUDMargin);

        // draw the difficulty setting
        String text = "DIFFICULTY: ";
        switch (difficulty) {
            case EASY:
                text += "EASY";
                break;
            case MEDIUM:
                text += "MEDIUM";
                break;
            case HARD: default:
                text += "HARD";
                break;
        }
        smallFont.draw(game.batch(), text,
                viewport.getWorldWidth() - HUDMargin,
                viewport.getWorldHeight() - HUDMargin,
                0, Align.right, false);
    }

    private void update(float deltaTime) {
        // update the fighters
        game.player().update(deltaTime);
        game.opponent().update(deltaTime);

        // make sure the fighters are facing each other
        if (game.player().getPosition().x <= game.opponent().getPosition().x) {
            game.player().faceRight();
            game.opponent().faceLeft();
        } else {
            game.player().faceLeft();
            game.opponent().faceRight();
        }

        // keep the fighter within the bound of the ring
        keepFighterInRing(game.player().getPosition());
        keepFighterInRing(game.opponent().getPosition());

        // check if the fighters are within contact distance
        if (areWithinContactDistance(game.player().getPosition(), game.opponent().getPosition())) {
            if (game.player().isAttackActive()) {
                // if the fighters are within contact distance and the player is attacking,
                // the opponent gets hit
                game.opponent().getHit(Fighter.HIT_STRENGTH);
                System.out.println("Opponent hit: " + game.opponent().getLife());

                // flag that contact has been made to deactivate the player's attack
                game.player().makeContact();

                // check if the opponent is knocked out
                if (game.opponent().hasLost()) {
                    // if opponent has lost, player wins
                    game.player().win();
                }
            }
        }
    }

    private void keepFighterInRing(Vector2 position) {
        if (position.y < RING_MIN_Y) {
            position.y = RING_MIN_Y;
        } else if (position.y > RING_MAX_Y) {
            position.y = RING_MAX_Y;
        }

        if (position.x < position.y / RING_SLOPE + RING_MIN_X) {
            position.x = position.y / RING_SLOPE + RING_MIN_X;
        } else if (position.x > position.y / -RING_SLOPE + RING_MAX_X) {
            position.x = position.y / -RING_SLOPE + RING_MAX_X;
        }
    }

    private boolean areWithinContactDistance(Vector2 position1, Vector2 position2) {
        // determine if the fighters are within contact distance
        float xDistance = Math.abs(position1.x - position2.x);
        float yDistance = Math.abs(position1.y - position2.y);
        return xDistance <= FIGHTER_CONTACT_DISTANCE_X && yDistance <= FIGHTER_CONTACT_DISTANCE_Y;
    }

    @Override
    public void resize(int width, int height) {
        // update the viewport with the new screen size
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        // check if player has pressed a horizontal movement key
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            game.player().moveLeft();
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            game.player().moveRight();
        }
        // check if player has pressed a vertical movement key
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            game.player().moveUp();
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            game.player().moveDown();
        }

        // check if the player has pressed a block or attack key
        if (keycode == Input.Keys.I) {
            game.player().block();
        } else if (keycode == Input.Keys.Y) {
            game.player().punch();
        } else if (keycode == Input.Keys.U) {
            game.player().kick();
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        // if player has released the movement key, stop moving in that direction
        // check if player has pressed a horizontal movement key
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            game.player().stopMovingLeft();
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            game.player().stopMovingRight();
        }
        // check if player has pressed a vertical movement key
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            game.player().stopMovingUp();
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            game.player().stopMovingDown();
        }

        // if player has released the block key, stop blocking
        if (keycode == Input.Keys.I) {
            game.player().stopBlocking();
        }

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
