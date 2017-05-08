package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;
    private static final int DIE_TIME = 1000;
    private boolean onGround;
    private Animation idleRight;
    private Animation idleLeft;
    private Animation climb;
    private Animation left;
    private Animation right;
    private Animation deadLeft;
    private Animation deadRight;

    private int state;
    private long stateTime;

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }

    public void setIdle(Animation idleLeft, Animation idleRight) {
    	this.idleRight = idleRight;
    	this.idleLeft = idleLeft;
    }

    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }
    
    /**
    Updates the animaton for this creature.
	*/
	public void update(long elapsedTime) {
	    // select the correct Animation
	    Animation newAnim = anim;
	    if (getVelocityX() <= 0) {
	    	if (newAnim == left) {
	    		newAnim = idleLeft;
	    	}
	    }
	    else if (getVelocityX() >= 0) {
	    	if (newAnim == right) {
	    		newAnim = idleRight;
	    	}
	    }
	    if (state == STATE_DYING && newAnim == left) {
	        newAnim = deadLeft;
	    }
	    else if (state == STATE_DYING && newAnim == right) {
	        newAnim = deadRight;
	    }
	
	    // update the Animation
	    if (anim != newAnim) {
	        anim = newAnim;
	        anim.start();
	    }
	    else {
	        anim.update(elapsedTime);
	    }
	
	    // update to "dead" state
	    stateTime += elapsedTime;
	    if (state == STATE_DYING && stateTime >= DIE_TIME) {
	        setState(STATE_DEAD);
	    }
	}


    public float getMaxSpeed() {
        return 0.5f;
    }

}
