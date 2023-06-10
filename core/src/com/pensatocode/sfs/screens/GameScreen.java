package com.pensatocode.sfs.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.pensatocode.sfs.SfsGame;
import com.pensatocode.sfs.resources.Assets;
import com.pensatocode.sfs.resources.GlobalVariables;

public class GameScreen implements Screen {

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
    }

    private void createGameArea() {
        this.backgroundTexture = game.assets().manager().get(Assets.BACKGROUND_TEXTURE);
        this.frontRopesTexture = game.assets().manager().get(Assets.FRONT_ROPES_TEXTURE);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // clear the screen with black. the arguments are RGBA, so it's black with full opacity.
        // this color is the background color of the game and will be visible
        // when the background texture is not fully opaque or when the viewport is letterboxed
        ScreenUtils.clear(0, 0, 0, 1);

        // set the sprite batch to use the camera's coordinate system
        game.batch().setProjectionMatrix(viewport.getCamera().combined);

        // begin drawing
        game.batch().begin();

        // draw background
        game.batch().draw(
                backgroundTexture,
                0,
                0,
                backgroundTexture.getWidth() * GlobalVariables.WORLD_SCALE,
                backgroundTexture.getHeight() * GlobalVariables.WORLD_SCALE
        );

        // end drawing
        game.batch().end();
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
}
