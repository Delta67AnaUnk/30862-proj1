package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.util.LinkedList;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;



import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.tilegame.sprites.*;
import com.brackeen.javagamebook.state.*;

public class MainGameState implements GameState {

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;


    private SoundManager soundManager;
    private MidiPlayer midiPlayer;
    private TileGameResourceManager resourceManager;
    private int width;
    private int height;

    private Point pointCache = new Point();
    private Sound prizeSound;
    private Sound boopSound;
    private Sequence music;
    private Sound KillSE;
    private Sound ShootSE;
    private Sound MultShootSE;
    private Sound SelfDeadSE;
    private Sound BombSE;
    private Sound MushSE;
    private TileMap map;
    private TileMapRenderer renderer;

    private String stateChange;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction shoot;
    private Starbuf.Pois stopbuf;

    public MainGameState(SoundManager soundManager,
        MidiPlayer midiPlayer, int width, int height)
    {
        this.soundManager = soundManager;
        this.midiPlayer = midiPlayer;
        this.width = width;
        this.height = height;
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        shoot = new GameAction("shoot");
        
        renderer = new TileMapRenderer();
        toggleDrumPlayback();
    }

    public String getName() {
        return "Main";
    }


    public String checkForStateChange() {
        return stateChange;
    }

    public void loadResources(ResourceManager resManager) {

        resourceManager = (TileGameResourceManager)resManager;

        resourceManager.loadResources();

        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        prizeSound = resourceManager.loadSound("sounds/prize.wav");
        boopSound = resourceManager.loadSound("sounds/boop2.wav");
        ShootSE = resourceManager.loadSound("sounds/page.wav");
        MultShootSE = resourceManager.loadSound("sounds/Shooting.wav");
        KillSE = resourceManager.loadSound("sounds/kl.wav");
        SelfDeadSE = resourceManager.loadSound("sounds/ahaha.wav");
        BombSE = resourceManager.loadSound("sounds/bomb.wav");
        MushSE = resourceManager.loadSound("sounds/eat.wav");
        music = resourceManager.loadSequence("sounds/music.midi");
    }

    public void start(InputManager inputManager) {
        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(shoot, KeyEvent.VK_S);

        soundManager.setPaused(false);
        midiPlayer.setPaused(false);
        midiPlayer.play(music, true);
        toggleDrumPlayback();
    }

    public void stop() {
        soundManager.setPaused(true);
        midiPlayer.setPaused(true);
    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map, width, height);
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }

    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stateChange = GameStateManager.EXIT_GAME;
            return;
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
            }
            if (jump.isPressed()) {
                player.jump(false);
            }
            if (shoot.isPressed()){
            	if(player.isShooting()==false&&(!player.isHold())){
            		player.clearcount();
            		player.setshoottime(0);
	            	if(player.isAlive()){
	            		player.ShootSwitch(true);
	            		Bullet newb = (Bullet)resourceManager.bulletSprite.clone();
	    	        	newb.setX(player.getX());
	    	        	newb.setY(player.getY()+player.getHeight()-45);
	    	        	newb.setOwner(true);
	    	        	if(player.getface()==true){
	    	        		newb.setVelocityX(-1*newb.getMaxSpeed());
	    	        	}else{
	    	        		newb.setVelocityX(newb.getMaxSpeed());
	    	        	}
	    	        	map.addTmpBullet(newb);
	    	        	soundManager.play(ShootSE);
	            	}
            	}
            }else{
            	if(!player.isHold()){
	            	if(player.getshoottime()>=player.timv()){
	            		soundManager.play(MultShootSE);
		            	player.setshoottime(0);
		            	player.holdSwitch(true);
		            	player.resetcount();
		            	stopbuf = (Starbuf.Pois)resourceManager.stopSprite.clone();
                		stopbuf.setX(player.getX());
                		stopbuf.setY(player.getY());
                		map.addTmpBullet(stopbuf);
                		
	            	}else{
	            		player.ShootSwitch(false);
	            		player.setshoottime(0);
	            	}
            	}
            	else{
            		if(player.getshoottime()>2700){
                		player.ShootSwitch(false);
                		player.holdSwitch(false);
                		player.setshoottime(0);
                		map.removeSprite(stopbuf);
                	}
            	}
            }
            player.setVelocityX(velocityX);
        }

    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    if(sprite instanceof Player){
                    	Player py = (Player)map.getPlayer();
	                    if(map.getTile(x, y)==resourceManager.getTileType('J'-'A')){
	                    	// poisoned
	                    	if(py.CanBePoison()){
		                    	if (!py.isHold()){
		                    		py.setshoottime(0);
		                    		py.holdSwitch(true);
		                    		py.ShootSwitch(true);
		                    		py.clearcount();
			                    	stopbuf = (Starbuf.Pois)resourceManager.stopSprite.clone();
			                		stopbuf.setX(py.getX());
			                		stopbuf.setY(py.getY());
			                		map.addTmpBullet(stopbuf);
		                    	}else{
		                    		py.setshoottime(py.getshoottime()-1300);
		                    	}
	                    	}
	                    	py.PoisonSwitch(false);
	                    }
	                    else if (map.getTile(x, y)==resourceManager.getTileType('K'-'A')){
	                    	// poisoned
	                    	if(py.CanBePoison()){
		                    	if (!py.isHold()){
		                    		py.setshoottime(0);
		                    		py.holdSwitch(true);
		                    		py.ShootSwitch(true);
		                    		py.clearcount();
			                    	stopbuf = (Starbuf.Pois)resourceManager.stopSprite.clone();
			                		stopbuf.setX(py.getX());
			                		stopbuf.setY(py.getY());
			                		map.addTmpBullet(stopbuf);
		                    	}else{
		                    		py.setshoottime(py.getshoottime()-1300);
		                    	}
	                    	}
	                    	py.PoisonSwitch(false);
	                    }
	                    else if (map.getTile(x, y)==resourceManager.getTileType('L'-'A')){
	                    	// bomb
	                    	py.PoisonSwitch(true);
	                    	if(!py.IsInvinc()){
	                    		map.setTile(x, y, resourceManager.getTileType(1));
	                    		py.lossHealth(10);
	                    		if(!py.isAlive()) soundManager.play(SelfDeadSE);
	                    		Starbuf.Expo stbf = (Starbuf.Expo)resourceManager.expoSprite.clone();
	                    		stbf.setX(py.getX());
	                    		stbf.setY(py.getY());
	                    		map.addTmpBullet(stbf);
	                    		soundManager.play(BombSE);
	                    	}
	                    }
	                    else if (map.getTile(x, y)==resourceManager.getTileType('M'-'A')){
	                    	// bomb
	                    	py.PoisonSwitch(true);
	                    	if(!py.IsInvinc()){
	                    		map.setTile(x, y, resourceManager.getTileType(8));
	                    		py.lossHealth(10);
	                    		if(!py.isAlive()) soundManager.play(SelfDeadSE);
	                    		Starbuf.Expo stbf = (Starbuf.Expo)resourceManager.expoSprite.clone();
	                    		stbf.setX(py.getX());
	                    		stbf.setY(py.getY());
	                    		map.addTmpBullet(stbf);
	                    		soundManager.play(BombSE);
	                    	}
	                    }else{
	                    	py.PoisonSwitch(true);
	                    }
                    }
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
            	///////if(!(otherSprite instanceof Bullet))
            		return otherSprite;
            }
        }

        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            Player p = (Player) player;
            p.holdSwitch(false);
            p.setshoottime(0);
            p.ShootSwitch(false);
            p.SetInvinc(false);
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            } else if (sprite instanceof Starbuf) {
            	Starbuf st = (Starbuf)sprite;
            	st.life-=elapsedTime;
            	if(st.life<=0){
            		i.remove();
            	}
            	if (sprite instanceof Starbuf.Pois){
            		sprite.setX(player.getX());
            		sprite.setY(player.getY());
            	}
            }
            // normal update
            sprite.update(elapsedTime);
        }
        map.DumpTmp();
        map.TmpFlush();
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {
    	Player py = (Player)map.getPlayer();
    	if(creature instanceof Player) py.UpdateInv(elapsedTime);
    	// Bullet Shot on Victim
        if (creature instanceof Bullet){
        	Creature victim = checkBulletCollision((Bullet)creature);
        	if(victim!=null){
        		creature.setState(Creature.STATE_DYING);
        		victim.setState(Creature.STATE_DYING);
        		py.addHealth(10);
        	}
        }
        
        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }
        
        if(creature instanceof Fly&&creature.isWaked()) {
        	if(creature.isShooting()){
        		if(!creature.getface()&&py.getX()<creature.getX()){
        			creature.setVelocityX(-creature.getVelocityX());
        		}else if(creature.getface()&&py.getX()>creature.getX()){
        			creature.setVelocityX(-creature.getVelocityX());
        		}
        		float Ydist = py.getY()-creature.getY();
        		creature.setVelocityY(Ydist/900);
        	}else{
        		Fly fly = (Fly)creature;
        		if(fly.isChecked()){
        			if(Math.abs(py.getX()-fly.getRecPlace())>128.0){
        				creature.ShootSwitch(true);
        			}
        		}else{
        			fly.check();
        			fly.recPlace(py.getX());
        		}
	        	if(creature.getAppearTime()>550) creature.ShootSwitch(true);
        	}
        }
        
        
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
         
        Point tile = getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
        	if (creature instanceof Bullet){
        		creature.setState(Creature.STATE_DYING);
        	}
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
            Player.distx += (newX - oldX);
            if (Player.distx > 63.9 | Player.distx < -63.9){
            	py.addHealth(1);
            	Player.distx = 0;
            }
        }
        
        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if(newY > map.getHeight()*64+32){
        	creature.setState(Creature.STATE_DYING);
        	if(creature instanceof Player&& !map.ifDropped()){
        		soundManager.play(SelfDeadSE);
        		map.Drop();
        	}
        }
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
            Player.disty += (newY - oldY);
            //System.out.println(Player.dist);
            if (Player.disty > 63.9 | Player.disty < -63.9){
            	py.addHealth(1);
            	Player.disty = 0;
            }
        }
        
        // no motion, add health
        if (dx == 0 && dy == 0){     	
        	py.sethealthtime(py.gethealthtime()+elapsedTime);
        	if (py.gethealthtime() > 10000){
            	py.addHealth(5);      // not moving
            	py.sethealthtime(0);
        	}
        }
        
        //Fly
        
        
        // Shooting 
        if (creature.isShooting()){
        	creature.setshoottime(creature.getshoottime()+elapsedTime);
        	if(creature instanceof Player){
    			if(py.isHold()&&py.getcount()>0){
    				if(py.getshoottime()>(10-py.getcount())*100){
    					Bullet pshoot = (Bullet)resourceManager.bulletSprite.clone();
    					pshoot.setX(py.getX());
    		        	pshoot.setY(py.getY()+py.getHeight()-45);
    		        	pshoot.setOwner(true);
    		        	if(py.getface()==true){
    		        		pshoot.setVelocityX(-1*pshoot.getMaxSpeed());
    		        	}
    		        	else{
    		        		pshoot.setVelocityX(pshoot.getMaxSpeed());
    		        	}
    		        	map.addTmpBullet(pshoot);
    		        	//soundManager.play(ShootSE);
    		        	py.count();
    				}
    			}
    			return;
    		}
        	boolean flag = false;
        	if(creature.getshoottime()>=creature.timv()){
        		flag = true;
        		creature.setshoottime(creature.getshoottime()-creature.timv());
        	}
        	if(flag){
        		if((creature.getVelocityX()>0)&&(creature.getX()>py.getX()))
        			flag = false;
        		else if ((creature.getVelocityX()<0)&&(creature.getX()<py.getX()))
        			flag = false;
        	}
        	if(flag){
	        	Bullet newb = (Bullet)resourceManager.bulletSprite.clone();
	        	newb.setX(creature.getX());
	        	newb.setY(creature.getY()+creature.getHeight()-45);
	        	newb.setOwner(false);
	        	if(creature.getface()==true){
	        		//System.out.println("left");
	        		newb.setVelocityX(-1*newb.getMaxSpeed());
	        	}
	        	else{
	        		//System.out.println("right");
	        		newb.setVelocityX(newb.getMaxSpeed());
	        	}
	        	map.addTmpBullet(newb);
	        	//soundManager.play(ShootSE);
        	}
        }
        return;
    }

    public Creature checkBulletCollision(Bullet bullet){
    	if(!bullet.isAlive()) return null;
    	boolean owner = bullet.getOwner();
    	// run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(bullet, otherSprite)) {
            	if(!(otherSprite instanceof Starbuf)){
	                // collision found, return the Sprite
	            	if(owner){ // user shoot
	            		if(otherSprite instanceof Fly|| otherSprite instanceof Grub){
	            			soundManager.play(KillSE);
	            			return (Creature)otherSprite;
	            		}
	            	}else{
	            		if(otherSprite instanceof Player){
	            			return (Creature)otherSprite;
	            		}
	            	}
            	}
            }
        }

        // no collision found
        return null;
    }

    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        if(player.IsInvinc()){
        	if(player.IsInvincBuf()){
        		player.RefillInvBuf();
        		Starbuf.StarB stbf = (Starbuf.StarB)resourceManager.starbufSprite.clone();
        		stbf.setX(player.getX()+player.getWidth()/2);
        		stbf.setY(player.getY()-15);
        		map.addTmpBullet(stbf);
        	}
        	
        }else{
        	
	        if(collisionSprite instanceof Bullet){
	        	
	        	Bullet bt = (Bullet)collisionSprite;
	        	if(bt.isAlive()==true){
		        	if(bt.getOwner()==false){
		        		bt.setState(Creature.STATE_DYING);
		        		player.lossHealth(5);
		        		if(!player.isAlive())soundManager.play(SelfDeadSE);
		        	}
	        	}
	        }
	        else if (collisionSprite instanceof Creature) {
	            Creature badguy = (Creature)collisionSprite;
	            if (canKill) {
	                // kill the badguy and make player bounce
	                soundManager.play(boopSound);
	                badguy.setState(Creature.STATE_DYING);
	                player.setY(badguy.getY() - player.getHeight());
	                player.jump(true);
                	player.addHealth(10);
	            }
	            else {
	                // player dies!
	            	soundManager.play(SelfDeadSE);
	                player.setState(Creature.STATE_DYING);
	            }
	        }
        }
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);
        Player py = (Player)map.getPlayer();
        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
        	py.SetInvinc(true);
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
        else if (powerUp instanceof PowerUp.Mushroom) {
        	soundManager.play(MushSE);
        	soundManager.play(prizeSound);
        	py.addHealth(5);
        }
    }

}
