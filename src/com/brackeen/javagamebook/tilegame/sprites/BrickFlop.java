package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Brickflop is a Creature that moves slowly up and down in one position.
*/
public class BrickFlop extends Creature {

    public BrickFlop(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.15f;
    }

}
