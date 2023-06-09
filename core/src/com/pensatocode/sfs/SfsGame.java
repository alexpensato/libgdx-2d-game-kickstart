package com.pensatocode.sfs;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pensatocode.sfs.resources.Assets;

public class SfsGame extends ApplicationAdapter {
    SpriteBatch batch;
	public Assets assets;

    @Override
    public void create() {
        batch = new SpriteBatch();
		assets = new Assets();
		// load all assets
		assets.load();
		assets.manager.finishLoading();
    }

    @Override
    public void render() {
        ScreenUtils.clear(1, 0, 0, 1);
        batch.begin();
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
		assets.dispose();
    }
}
