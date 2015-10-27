package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

public class Bullet extends  Creature{
	private boolean owner; //true if shooted by player
	
	public Bullet(Animation left,Animation right,Animation deadleft, Animation deadright){
		super(left,right,deadleft,deadright);
		owner = true;
	}
	
	public void setOwner(boolean b){
		owner = b;
	}
	
	public boolean getOwner(){
		return owner;
	}
	
	public float getMaxSpeed() {
        return 0.8f;
    }
	
	public boolean isFlying(){
		return true;
	}
	
	public boolean isShooting(){
		return false;
	}
	
	public void ShootSwitch(boolean b){
		
	}
	
	/*public float getY(){
		return super.getY()-0.15f;
	}*/
}
