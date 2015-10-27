package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;

    private boolean onGround;
    
    private int health = 20;
    
    public static float distx = 0;
    public static float disty = 0;
    
    private int shootct;
    
    
    private boolean isadd; //State Machine
    private long addingtime; //add with elapsed time and compare with timv
    
    
    
    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        ShootSwitch(false);
        shootct = 0;
        health = 55;
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
    
    /*public void setHealth(){
    	health = 20;
    }*/
    
    public boolean isadding(){
    	return isadd;
    }
    
    
    public long gethealthtime(){
    	return addingtime;
    }
    
    public void sethealthtime(long time){
    	addingtime = time;
    }
    
    public void getHealth() {
        //return health;
    	System.out.println("Current Health is: " + health);
    }
    public int getaHealth() {
        return health;
    }
    
    public void lossHealth(int h){
    	health = health - h;
    	System.out.println("");
    	if(health<=0){
    		setState(STATE_DYING);
    	}
    }
    
    public void addHealth(int h){
    	health += h;
    	if(health > 40){
    		health = 40;
    	}
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
    
    
    
    public void shootState(boolean b){
    	//System.out.println("Debug Use");
    	if(getState()==STATE_DYING||getState()==STATE_DEAD){
    		b=false;
    		shootct = 0;
    	}
    	ShootSwitch(b);
    	if(!b)shootct = 0;
    }
    
    public int getcount(){
    	return shootct;
    }
    
    public void resetcountt(){
    	shootct = 0;
    }
    
    public void countup(){
    	shootct++;
    }

    public float getMaxSpeed() {
        return 0.5f;
    }

    public long timv(){
    	return 550;
    }
}
