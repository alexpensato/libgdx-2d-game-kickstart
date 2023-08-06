package com.pensatocode.sfs.resources;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

public class Assets {
    // asset manager
    private final AssetManager manager;

    public Assets() {
        this.manager = new AssetManager();
    }

    // scene textures
    public static final String BACKGROUND_TEXTURE = "textures/Background.png";
    public static final String FRONT_ROPES_TEXTURE = "textures/FrontRopes.png";

    // sprite sheets
    public static final String IDLE_SPRITE_SHEET = "sprites/IdleSpriteSheet.png";
    public static final String WALK_SPRITE_SHEET = "sprites/WalkSpriteSheet.png";
    public static final String PUNCH_SPRITE_SHEET = "sprites/PunchSpriteSheet.png";
    public static final String KICK_SPRITE_SHEET = "sprites/KickSpriteSheet.png";
    public static final String HURT_SPRITE_SHEET = "sprites/HurtSpriteSheet.png";
    public static final String BLOCK_SPRITE_SHEET = "sprites/BlockSpriteSheet.png";
    public static final String WIN_SPRITE_SHEET = "sprites/WinSpriteSheet.png";
    public static final String LOSE_SPRITE_SHEET = "sprites/LoseSpriteSheet.png";

    // gameplay atlas
    public static final String GAMEPLAY_BUTTONS_ATLAS = "textures/GameplayButtons.atlas";
    public static final String BLOOD_ATLAS = "textures/Blood.atlas";

    // fonts
    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String SMALL_FONT = "smallFont.ttf";
    public static final String MEDIUM_FONT = "mediumFont.ttf";
    public static final String LARGE_FONT = "largeFont.ttf";

    // audio assets
    public static final String BLOCK_SOUND = "audio/block.mp3";
    public static final String BOO_SOUND = "audio/boo.mp3";
    public static final String CHEER_SOUND = "audio/cheer.mp3";
    public static final String CLICK_SOUND = "audio/click.mp3";
    public static final String HIT_SOUND = "audio/hit.mp3";
    public static final String MUSIC = "audio/music.ogg";

    // menu assets
    public static final String MENU_ITEMS_ATLAS = "textures/MenuItems.atlas";

    public void load() {
        // load gameplay assets
        loadGameplayAssets();
        // load fonts
        loadFonts();
        // load audio assets
//        loadAudioAssets();
        // load menu assets
//        loadMenuAssets();
    }

    public void dispose() {
        manager.dispose();
    }

    public AssetManager manager() {
        return manager;
    }

    /**
     *  Loads all the assets needed for the gameplay screen.
     *
     *  LibGDX Texture Filters:
     *  Defines the algorithm to use when the texture is scaled up or down.
     *  MinFilter: Defines the algorithm to use when the texture is scaled down.
     *  MagFilter: Defines the algorithm to use when the texture is scaled up.
     *
     *  Texture filter options:
     *  Nearest (default): Uses the pixel closest to the sampling point.
     *  Linear: Uses the weighted average of the four pixels closest to the sampling point.
     *  MipMap: Uses mipmaps to select the correct level-of-detail of the texture.
     */
    private void loadGameplayAssets() {
        // set texture filters
        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;

        // Scene
        manager.load(BACKGROUND_TEXTURE, Texture.class, param);
        manager.load(FRONT_ROPES_TEXTURE, Texture.class, param);

        // Sprite Sheets
        manager.load(IDLE_SPRITE_SHEET, Texture.class, param);
        manager.load(WALK_SPRITE_SHEET, Texture.class, param);
        manager.load(PUNCH_SPRITE_SHEET, Texture.class, param);
        manager.load(KICK_SPRITE_SHEET, Texture.class, param);
        manager.load(HURT_SPRITE_SHEET, Texture.class, param);
        manager.load(BLOCK_SPRITE_SHEET, Texture.class, param);
        manager.load(WIN_SPRITE_SHEET, Texture.class, param);
        manager.load(LOSE_SPRITE_SHEET, Texture.class, param);

        // Texture Atlases
        manager.load(GAMEPLAY_BUTTONS_ATLAS, TextureAtlas.class);
        manager.load(BLOOD_ATLAS, TextureAtlas.class);
    }

    private void loadFonts() {
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        // load the small font
        FreetypeFontLoader.FreeTypeFontLoaderParameter smallFont =
                new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        smallFont.fontFileName = ROBOTO_REGULAR;
        smallFont.fontParameters.size = 32;
        smallFont.fontParameters.minFilter = Texture.TextureFilter.Linear;
        smallFont.fontParameters.magFilter = Texture.TextureFilter.Linear;
        manager.load(SMALL_FONT, BitmapFont.class, smallFont);

        // load the medium font
        FreetypeFontLoader.FreeTypeFontLoaderParameter mediumFont =
                new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        mediumFont.fontFileName = ROBOTO_REGULAR;
        mediumFont.fontParameters.size = 106;
        mediumFont.fontParameters.borderWidth = 4;
        mediumFont.fontParameters.minFilter = Texture.TextureFilter.Linear;
        mediumFont.fontParameters.magFilter = Texture.TextureFilter.Linear;
        manager.load(MEDIUM_FONT, BitmapFont.class, mediumFont);

        // load the large font
        FreetypeFontLoader.FreeTypeFontLoaderParameter largeFont =
                new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        largeFont.fontFileName = ROBOTO_REGULAR;
        largeFont.fontParameters.size = 150;
        largeFont.fontParameters.borderWidth = 6;
        largeFont.fontParameters.minFilter = Texture.TextureFilter.Linear;
        largeFont.fontParameters.magFilter = Texture.TextureFilter.Linear;
        manager.load(LARGE_FONT, BitmapFont.class, largeFont);
    }

    private void loadAudioAssets() {
        manager.load(BLOCK_SOUND, Texture.class);
        manager.load(BOO_SOUND, Texture.class);
        manager.load(CHEER_SOUND, Texture.class);
        manager.load(CLICK_SOUND, Texture.class);
        manager.load(HIT_SOUND, Texture.class);
        manager.load(MUSIC, Texture.class);
    }

    private void loadMenuAssets() {
        manager.load(MENU_ITEMS_ATLAS, TextureAtlas.class);
    }
}
