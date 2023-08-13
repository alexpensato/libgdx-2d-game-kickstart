package com.pensatocode.sfs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pensatocode.sfs.objects.Fighter;
import com.pensatocode.sfs.resources.Assets;
import com.pensatocode.sfs.resources.AudioManager;
import com.pensatocode.sfs.screens.GameScreen;

public class SfsGame extends Game {
    // It's good practice to use a single SpriteBatch per game.
    // It's also good practice to dispose of it when it's no longer needed.
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
	private Assets assets;
    private AudioManager audioManager;
    private GameScreen gameScreen;
    private Fighter player;
    private Fighter opponent;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
		assets = new Assets();

		// load all assets
		assets.load();
		assets.manager().finishLoading();

        // initialize the audio manager
        audioManager = new AudioManager(assets.manager());
        audioManager.playMusic();

        // initialize the fighters
        player = new Fighter(this, "Slim Stallone", new Color(1f, 0.2f, 0.2f, 1f));
        opponent = new Fighter(this, "Thin Diesel", new Color(0.25f, 0.7f, 1f, 1f));

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
        shapeRenderer.dispose();
		assets.dispose();
    }

    public SpriteBatch batch() {
        return batch;
    }

    public ShapeRenderer shapeRenderer() {
        return shapeRenderer;
    }

    public Assets assets() {
        return assets;
    }

    public AudioManager audioManager() {
        return audioManager;
    }

    public GameScreen gameScreen() {
        return gameScreen;
    }

    public Fighter player() {
        return player;
    }

    public Fighter opponent() {
        return opponent;
    }
}
