package com.pensatocode.sfs.objects;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pensatocode.sfs.SfsGame;
import com.pensatocode.sfs.resources.Assets;
import com.pensatocode.sfs.resources.GlobalVariables;

public class Fighter {
    // number of frame rows and columns in each animation sprite sheet
    private static final int FRAME_ROWS = 2;
    private static final int FRAME_COLS = 3;

    // how fast the fighter moves
    public static final float MOVEMENT_SPEED = 10f;

    // maximum life points
    public static final float MAX_LIFE = 100f;

    // amount of damage each attack does
    public static final float HIT_STRENGTH = 5f;

    // factor to decrease damage when blocking
    public static final float BLOCK_DAMAGE_FACTOR = 0.2f;

    // distinguish between fighters
    private String name;
    private Color color;

    // state of the fighter
    public enum State {
        BLOCK, HURT, IDLE, KICK, LOSE, PUNCH, WALK, WIN
    }

    private State state;
    private float stateTime;
    private State renderState;
    private float renderStateTime;
    private final Vector2 position = new Vector2();
    private final Vector2 movementDirection = new Vector2();
    private float life;
    private int facingDirection;
    private boolean madeContact;

    // animations
    private Animation<TextureRegion> blockAnimation;
    private Animation<TextureRegion> hurtAnimation;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> kickAnimation;
    private Animation<TextureRegion> loseAnimation;
    private Animation<TextureRegion> punchAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> winAnimation;

    public Fighter(SfsGame game, String name, Color color) {
        this.name = name;
        this.color = color;

        // initialize animations
        initializeBlockAnimation(game.assets().manager());
        initializeHurtAnimation(game.assets().manager());
        initializeIdleAnimation(game.assets().manager());
        initializeKickAnimation(game.assets().manager());
        initializeLoseAnimation(game.assets().manager());
        initializePunchAnimation(game.assets().manager());
        initializeWalkAnimation(game.assets().manager());
        initializeWinAnimation(game.assets().manager());
    }

    public void getReady(float positionX, float positionY) {
        // set initial state
        state = State.IDLE;
        stateTime = 0f;
        renderState = State.IDLE;
        renderStateTime = 0f;
        life = MAX_LIFE;
        facingDirection = 1;
        madeContact = false;

        // set initial position
        position.set(positionX, positionY);
        movementDirection.set(0f, 0f);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void faceLeft() {
        facingDirection = -1;
    }

    public void faceRight() {
        facingDirection = 1;
    }

    private void changeState(State newState) {
        if (state != newState) {
            state = newState;
            stateTime = 0f;
        }
    }

    private void setMovement(float x, float y) {
        movementDirection.set(x, y);
        if (state == State.WALK && x == 0f && y == 0f) {
            changeState(State.IDLE);
        } else if (state == State.IDLE && (x != 0f || y != 0f)) {
            changeState(State.WALK);
        }
    }

    public void moveLeft() {
        setMovement(-1, movementDirection.y);
    }

    public void moveRight() {
        setMovement(1, movementDirection.y);
    }

    public void moveUp() {
        setMovement(movementDirection.x, 1);
    }

    public void moveDown() {
        setMovement(movementDirection.x, -1);
    }

    public void stopMovingLeft() {
        if (movementDirection.x == -1) {
            setMovement(0, movementDirection.y);
        }
    }

    public void stopMovingRight() {
        if (movementDirection.x == 1) {
            setMovement(0, movementDirection.y);
        }
    }

    public void stopMovingUp() {
        if (movementDirection.y == 1) {
            setMovement(movementDirection.x, 0);
        }
    }

    public void stopMovingDown() {
        if (movementDirection.y == -1) {
            setMovement(movementDirection.x, 0);
        }
    }
    public void block() {
        if (state == State.IDLE || state == State.WALK) {
            changeState(State.BLOCK);
        }
    }

    public void stopBlocking() {
        if (state != State.BLOCK) {
            return;
        }
        // if the movement direction is set, start walking;
        // otherwise go to idle
        if (movementDirection.x != 0 || movementDirection.y != 0) {
            changeState(State.WALK);
        } else {
            changeState(State.IDLE);
        }
    }

    public boolean isBlocking() {
        return state == State.BLOCK;
    }

    public void punch() {
        if (state == State.IDLE || state == State.WALK) {
            changeState(State.PUNCH);
        }
    }

    public void kick() {
        if (state == State.IDLE || state == State.WALK) {
            changeState(State.KICK);
        }
    }

    public boolean isAttacking() {
        return state == State.PUNCH || state == State.KICK;
    }

    public void render(SpriteBatch batch) {
        // get the current frame of animation for the current state
        TextureRegion currentFrame;
        switch (renderState) {
            case BLOCK:
                currentFrame = blockAnimation.getKeyFrame(renderStateTime, true);
                break;
            case HURT:
                currentFrame = hurtAnimation.getKeyFrame(renderStateTime, false);
                break;
            case IDLE:
                currentFrame = idleAnimation.getKeyFrame(renderStateTime, true);
                break;
            case KICK:
                currentFrame = kickAnimation.getKeyFrame(renderStateTime, false);
                break;
            case LOSE:
                currentFrame = loseAnimation.getKeyFrame(renderStateTime, false);
                break;
            case PUNCH:
                currentFrame = punchAnimation.getKeyFrame(renderStateTime, false);
                break;
            case WALK:
                currentFrame = walkAnimation.getKeyFrame(renderStateTime, true);
                break;
            case WIN:
                currentFrame = winAnimation.getKeyFrame(renderStateTime, false);
                break;
            default:
                currentFrame = idleAnimation.getKeyFrame(renderStateTime, true);
                break;
        }

        batch.setColor(color);

        // draw the current frame
        batch.draw(
                currentFrame,
                position.x,
                position.y,
                currentFrame.getRegionWidth() * 0.5f * GlobalVariables.WORLD_SCALE,
                0,
                currentFrame.getRegionWidth() * GlobalVariables.WORLD_SCALE,
                currentFrame.getRegionHeight() * GlobalVariables.WORLD_SCALE,
                facingDirection,
                1,
                0
        );

    }

    public void update(float deltaTime) {
        // increment the state time by delta time
        stateTime += deltaTime;

        // only update the render state if delta time is not zero
        if (deltaTime > 0f) {
            renderState = state;
            renderStateTime = stateTime;
        }

        // update the state
        if (state == State.WALK) {
            // if the fighter is walking, move in the direction of the movement direction vector
            position.x += movementDirection.x * MOVEMENT_SPEED * deltaTime;
            position.y += movementDirection.y * MOVEMENT_SPEED * deltaTime;
        } else if ((state == State.PUNCH && punchAnimation.isAnimationFinished(stateTime))
        || (state == State.KICK && kickAnimation.isAnimationFinished(stateTime))) {
            // if the animation has finished and the movement direction is set, start walking;
            // otherwise, go to idle
            if (movementDirection.x != 0 || movementDirection.y != 0) {
                changeState(State.WALK);
            } else {
                changeState(State.IDLE);
            }
        }
    }

    private void initializeBlockAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.BLOCK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        blockAnimation = new Animation<>(0.05f, frames);
    }

    private void initializeHurtAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.HURT_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        hurtAnimation = new Animation<>(0.03f, frames);
    }

    private void initializeIdleAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.IDLE_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        idleAnimation = new Animation<>(0.1f, frames);
    }

    private void initializeKickAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.KICK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        kickAnimation = new Animation<>(0.05f, frames);
    }

    private void initializeLoseAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.LOSE_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        loseAnimation = new Animation<>(0.05f, frames);
    }

    private void initializePunchAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.PUNCH_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        punchAnimation = new Animation<>(0.05f, frames);
    }

    private void initializeWalkAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.WALK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        walkAnimation = new Animation<>(0.08f, frames);
    }

    private void initializeWinAnimation(AssetManager manager) {
        Texture spriteSheet = manager.get(Assets.WIN_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        winAnimation = new Animation<>(0.05f, frames);
    }

    private TextureRegion[] getAnimationFrames(Texture spriteSheet) {
        TextureRegion[][] tmp = TextureRegion.split(
                spriteSheet,
                spriteSheet.getWidth() / FRAME_COLS,
                spriteSheet.getHeight() / FRAME_ROWS);
        TextureRegion[] frames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int row = 0; row < FRAME_ROWS; row++) {
            for (int col = 0; col < FRAME_COLS; col++) {
                frames[index++] = tmp[row][col];
            }
        }
        return frames;
    }

}
