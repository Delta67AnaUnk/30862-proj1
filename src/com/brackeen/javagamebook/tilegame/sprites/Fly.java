package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Fly extends Creature {
	private float placerec;
	private boolean checked;

    public Fly(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        placerec = 0;
        checked = false;
    }


    public float getMaxSpeed() {
        return 0.2f;
    }

    public boolean isChecked(){
    	return checked;
    }
    
    public void check(){
    	checked = true;
    }
    
    public float getRecPlace(){
    	return placerec;
    }
    
    public void recPlace(float p){
    	placerec = p;
    }

    public boolean isFlying() {
        return isAlive();
    }

    public long timv(){
    	return 1100;
    }
    
}
