package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;

    private boolean onGround,isClingingX =false,isClingingY=false;

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
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
     * returns a boolean variable showing whether or not the player is currently clinging to a wall
     */
    public boolean getClingingX(){
    	return isClingingX;
    }
    
    public void setClingX(boolean clinger){
    	isClingingX=clinger;
    }
    
    /**
     * Sets the boolean cariable of isClingingX to whatever the paramater is.  Which should determine whether or not the player i
     * clingign to a wall.
     * @param clinger
     */
    public void setClingY(boolean clinger){
    	isClingingY=clinger;
    }
    
    public boolean getClingingY(){
    	return isClingingY;
    }
    
    /**
     * Sets the boolean cariable of isClingingX to whatever the paramater is.  Which should determine whether or not the player i
     * clingign to a wall.
     * @param clinger
     */
   
    
    /**
     * Makes the player cling to the closest viable ceiling, can only be clinging to one wall or a ceiling at a time.
     */
    public void clingCeiling(){
    	setVelocityY(0);
    }

    public float getMaxSpeed() {
        return 0.5f;
    }

}