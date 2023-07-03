package com.pensatocode.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.pensatocode.sfs.SfsGame;
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

        // initialize the fighters
        game.player().getReady(PLAYER_START_POSITION_X, FIGHTER_START_POSITION_Y);
        game.opponent().getReady(OPPONENT_START_POSITION_X, FIGHTER_START_POSITION_Y);
    }

    private void createGameArea() {
        this.backgroundTexture = game.assets().manager().get(Assets.BACKGROUND_TEXTURE);
        this.frontRopesTexture = game.assets().manager().get(Assets.FRONT_ROPES_TEXTURE);
    }

    @Override
    public void show() {
        // process user input
        Gdx.input.setInputProcessor(this);
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
