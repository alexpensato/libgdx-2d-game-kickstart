package com.pensatocode.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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

    private enum GameState {
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private GameState gameState;

    // rounds
    private enum RoundState {
        STARTING,
        IN_PROGRESS,
        ENDING
    }

    private RoundState roundState;
    private float roundStateTime;
    private static final float START_ROUND_DELAY = 2f;
    private static final float END_ROUND_DELAY = 2f;
    private int currentRound;
    private static final int MAX_ROUNDS = 3;
    private int roundsWon = 0;
    private int roundsLost = 0;
    private static final float MAX_ROUND_TIME = 99.99f;
    private float roundTimer = MAX_ROUND_TIME;
    private static final float CRITICAL_ROUND_TIME = 10f;
    private static final Color CRITICAL_ROUND_TIME_COLOR = Color.RED;

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

    // buttons
    private Sprite playAgainButtonSprite;
    private Sprite mainMenuButtonSprite;

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

        // create the buttons
        createButtons();
    }

    private void createGameArea() {
        this.backgroundTexture = game.assets().manager().get(Assets.BACKGROUND_TEXTURE);
        this.frontRopesTexture = game.assets().manager().get(Assets.FRONT_ROPES_TEXTURE);

        // change the texture filter to avoid blurry sprites (for background and front ropes only)
//        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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

    private void createButtons() {
        // get the gameplay button texture atlas from the asset manager
        TextureAtlas buttonTextureAtlas = game.assets().manager().get(Assets.GAMEPLAY_BUTTONS_ATLAS);

        // create the play again button sprite
        playAgainButtonSprite = new Sprite(buttonTextureAtlas.findRegion("PlayAgainButton"));
        playAgainButtonSprite.setSize(
                playAgainButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
                playAgainButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE
        );

        // create the main menu button sprite
        mainMenuButtonSprite = new Sprite(buttonTextureAtlas.findRegion("MainMenuButton"));
        mainMenuButtonSprite.setSize(
                mainMenuButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
                mainMenuButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE
        );
    }

    @Override
    public void show() {
        // process user input
        Gdx.input.setInputProcessor(this);

        // start the game
        startGame();

        //game.opponent().block();
    }

    private void startGame() {
        // set the game state
        gameState = GameState.RUNNING;
        roundsWon = 0;
        roundsLost = 0;

        // start the first round
        currentRound = 1;
        startRound();
    }

    private void startRound() {
        // initialize the fighters
        game.player().getReady(PLAYER_START_POSITION_X, FIGHTER_START_POSITION_Y);
        game.opponent().getReady(OPPONENT_START_POSITION_X, FIGHTER_START_POSITION_Y);

        // set the round state
        roundState = RoundState.STARTING;
        roundStateTime = 0f;
        roundTimer = MAX_ROUND_TIME;
    }

    private void endRound() {
        // end the round
        roundState = RoundState.ENDING;
        roundStateTime = 0f;
    }

    private void winRound() {
        // player wins the round
        game.player().win();
        game.opponent().lose();
        roundsWon++;

        // end the round
        endRound();
    }

    private void loseRound() {
        // player loses the round
        game.player().lose();
        game.opponent().win();
        roundsLost++;

        // end the round
        endRound();
    }

    @Override
    public void render(float delta) {
        // clear the screen with black. the arguments are RGBA, so it's black with full opacity.
        // this color is the background color of the game and will be visible
        // when the background texture is not fully opaque or when the viewport is letterboxed
        ScreenUtils.clear(0, 0, 0, 1);

        // update the game
        update(delta);

        // set the sprite batch amd the shape renderer to use the viewport's camera
        game.batch().setProjectionMatrix(viewport.getCamera().combined);
        game.shapeRenderer().setProjectionMatrix(viewport.getCamera().combined);

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

        // if the game is over, draw the game over overlay
        if (gameState == GameState.GAME_OVER) {
            // draw the game over overlay
            renderGameOverOverlay();
        } else {
            // if the round is starting, draw the start round text
            if (roundState == RoundState.STARTING) {
                // draw the start round text
                renderStartRoundText();
            }
        }

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

        // set up the layout sizes and positioning
        float healthBarPadding = 0.5f;
        float healthBarHeight = smallFont.getCapHeight() + healthBarPadding * 2f;
        float healthBarMaxWidth = 32f;
        float healthBarBackgroundPadding = 0.2f;
        float healthBarBackgroundHeight = healthBarHeight + healthBarBackgroundPadding * 2f;
        float healthBarBackgroundWidth = healthBarMaxWidth + healthBarBackgroundPadding * 2f;
        float healthBarBackgroundMarginTop = 0.8f;
        float healthBarBackgroundPositionY = viewport.getWorldHeight()  - HUDMargin - smallFont.getCapHeight()
                - healthBarBackgroundMarginTop - healthBarBackgroundHeight;
        float healthBarPositionY = healthBarBackgroundPositionY + healthBarBackgroundPadding;
        float fighterNamePositionY = healthBarPositionY + healthBarHeight - healthBarPadding;

        game.batch().end();
        game.shapeRenderer().begin(ShapeRenderer.ShapeType.Filled);

        // draw the player's health bar background
        game.shapeRenderer().setColor(HEALTH_BAR_BACKGROUND_COLOR);
        game.shapeRenderer().rect(
                HUDMargin,
                healthBarBackgroundPositionY,
                healthBarBackgroundWidth,
                healthBarBackgroundHeight
        );
        game.shapeRenderer().rect(
                viewport.getWorldWidth() - HUDMargin - healthBarBackgroundWidth,
                healthBarBackgroundPositionY,
                healthBarBackgroundWidth,
                healthBarBackgroundHeight
        );

        // draw the player's health bar
        game.shapeRenderer().setColor(HEALTH_BAR_COLOR);
        float healthBarWidth = healthBarMaxWidth * game.player().getLife() / Fighter.MAX_LIFE;
        game.shapeRenderer().rect(
                HUDMargin + healthBarBackgroundPadding,
                healthBarPositionY,
                healthBarWidth,
                healthBarHeight
        );
        healthBarWidth = healthBarMaxWidth * game.opponent().getLife() / Fighter.MAX_LIFE;
        game.shapeRenderer().rect(
                viewport.getWorldWidth() - HUDMargin - healthBarBackgroundPadding - healthBarWidth,
                healthBarPositionY,
                healthBarWidth,
                healthBarHeight
        );

        game.shapeRenderer().end();
        game.batch().begin();

        // draw the player's name
        smallFont.draw(game.batch(), game.player().getName(),
                HUDMargin + healthBarBackgroundPadding + healthBarPadding,
                fighterNamePositionY);
        smallFont.draw(game.batch(), game.opponent().getName(),
                viewport.getWorldWidth() - HUDMargin - healthBarBackgroundPadding - healthBarPadding,
                fighterNamePositionY, 0, Align.right, false);

        // draw the round timer
        if (roundTimer < CRITICAL_ROUND_TIME) {
            mediumFont.setColor(CRITICAL_ROUND_TIME_COLOR);
        }
        mediumFont.draw(game.batch(), String.format("%02d", (int) roundTimer),
                viewport.getWorldWidth() / 2f - mediumFont.getSpaceXadvance() * 2.3f,
                viewport.getWorldHeight() - HUDMargin,
                0, Align.left, false);
        mediumFont.setColor(DEFAULT_FONT_COLOR);
    }

    private void renderStartRoundText() {
        String text;
        if (roundStateTime < START_ROUND_DELAY * 0.5f) {
            text = "ROUND " + currentRound;
        } else {
            text = "FIGHT!";
        }
        mediumFont.draw(game.batch(), text,
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0, Align.center, false);
    }

    private void renderGameOverOverlay() {
        // cover the screen with a black rectangle
        game.batch().end();
        // enable blending to allow transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // set the blending function to blend the alpha channel
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        game.shapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer().setColor(0,0,0,0.7f);
        game.shapeRenderer().rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.shapeRenderer().end();

        // disable blending to avoid messing up other stuff
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch().begin();

        // calculate the layout dimensions
        float textMarginBottom = 2f;
        float buttonSpacing = 0.5f;
        float layoutHeight = largeFont.getCapHeight() + textMarginBottom
                + playAgainButtonSprite.getHeight() + buttonSpacing + mainMenuButtonSprite.getHeight();
        float layoutPositionY = viewport.getWorldHeight() / 2f - layoutHeight / 2f;

        // draw the buttons
        mainMenuButtonSprite.setPosition(viewport.getWorldWidth() / 2f - mainMenuButtonSprite.getWidth() / 2f,
                layoutPositionY);
        mainMenuButtonSprite.draw(game.batch());
        playAgainButtonSprite.setPosition(viewport.getWorldWidth() / 2f - playAgainButtonSprite.getWidth() / 2f,
                layoutPositionY + mainMenuButtonSprite.getHeight() + buttonSpacing);
        playAgainButtonSprite.draw(game.batch());

        // draw the text
        String text = roundsWon > roundsLost ? "YOU WIN!" : "YOU LOSE!";
        largeFont.draw(game.batch(), text,
                viewport.getWorldWidth() / 2f,
                playAgainButtonSprite.getY() + playAgainButtonSprite.getHeight()
                        + textMarginBottom + largeFont.getCapHeight(),
                0, Align.center, false);
    }

    private void update(float deltaTime) {
        if (roundState == RoundState.STARTING && roundStateTime >= START_ROUND_DELAY) {
            // if the starting delay is over, start the round
            roundState = RoundState.IN_PROGRESS;
            roundStateTime = 0f;
        } else if (roundState == RoundState.ENDING && roundStateTime >= END_ROUND_DELAY) {
            // if the end round delay has been reached and player has won or lost more than
            // half of the rounds, end the game; otherwise, start a new round
            if (roundsWon > MAX_ROUNDS / 2 || roundsLost > MAX_ROUNDS / 2) {
                gameState = GameState.GAME_OVER;
            } else {
                currentRound++;
                startRound();
            }
        } else {
            // otherwise, increment the round state time by delta time
            roundStateTime += deltaTime;
        }

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

        if (roundState == RoundState.IN_PROGRESS) {
            // if the round is in progress, decrease the round timer by delta time
            roundTimer -= deltaTime;

            if (roundTimer <= 0f) {
                // if the round timer has reached zero, end the round
                if (game.player().getLife() >= game.opponent().getLife()) {
                    winRound();
                } else {
                    loseRound();
                }
            }

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
                        // if opponent has lost, player wins the round
                        winRound();
                    }
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
        if (keycode == Input.Keys.SPACE) {
            if (gameState == GameState.RUNNING) {
                // if the game is running and the space bar is pressed, skip any round delays
                if (roundState == RoundState.STARTING) {
                    roundStateTime = START_ROUND_DELAY;
                } else if (roundState == RoundState.ENDING) {
                    roundStateTime = END_ROUND_DELAY;
                }
            } else if (gameState == GameState.GAME_OVER) {
                // if the game is over and the space bar is pressed, restart the game
                startGame();
            }
        } else {
            if (roundState == RoundState.IN_PROGRESS) {
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
            }

            // check if the player has pressed a block or attack key
            if (keycode == Input.Keys.I) {
                game.player().block();
            } else if (keycode == Input.Keys.Y) {
                game.player().punch();
            } else if (keycode == Input.Keys.U) {
                game.player().kick();
            }
        }

        // returning true indicates to libgdx that the key press has been handled by ourselves
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

    /**
     * Called when the player touches the screen with a finger,
     * or clicks it with a mouse.
     *
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button the button
     * @return boolean
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // convert the touch coordinates to world coordinates
        Vector3 position = new Vector3(screenX, screenY, 0);
        viewport.getCamera().unproject(position, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());

        if (gameState == GameState.RUNNING) {
            if (roundState == RoundState.STARTING) {
                // if the round is starting and the screen is touched, skip the start round delay
                roundStateTime = START_ROUND_DELAY;
            } else if (roundState == RoundState.ENDING) {
                // if the round is ending and the screen is touched, skip the end round delay
                roundStateTime = END_ROUND_DELAY;
            }
        } else {
            if (gameState == GameState.GAME_OVER
                    && playAgainButtonSprite.getBoundingRectangle().contains(position.x, position.y)) {
                // if the game is over and the play again button has been pressed, restart the game
                startGame();
            }
        }

        // returning true indicates to LibGdx that the event was handled by ourselves
        return true;
    }

    /**
     * Called when the player removes the finger from the screen,
     * or releases the mouse button.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @param button the button
     * @return boolean
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when the player drags a finger across the screen,
     * (or moves the mouse with a button pressed -> to be confirmed).
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return boolean
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Called when the player moves the mouse without pressing any buttons.
     *
     * @param screenX
     * @param screenY
     * @return boolean
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Called when the player scrolls the mouse wheel up or down.
     *
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return boolean
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
