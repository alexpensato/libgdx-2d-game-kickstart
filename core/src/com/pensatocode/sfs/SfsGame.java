package com.pensatocode.sfs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pensatocode.sfs.resources.Assets;
import com.pensatocode.sfs.screens.GameScreen;

public class SfsGame extends Game {
    // It's good practice to use a single SpriteBatch per game.
    // It's also good practice to dispose of it when it's no longer needed.
    private SpriteBatch batch;
	private Assets assets;
    private GameScreen gameScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
		assets = new Assets();

		// load all assets
		assets.load();
		assets.manager().finishLoading();

        // initialize the game screen and switch to it
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        gameScreen.dispose();
        batch.dispose();
		assets.dispose();
    }

    public SpriteBatch batch() {
        return batch;
    }

    public Assets assets() {
        return assets;
    }

    public GameScreen gameScreen() {
        return gameScreen;
    }
}
