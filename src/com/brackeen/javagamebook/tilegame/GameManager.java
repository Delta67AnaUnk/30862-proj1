package com.brackeen.javagamebook.tilegame;

import java.awt.Graphics2D;
import java.io.File;
import java.util.Scanner;
import java.util.logging.*;

import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.sound.MidiPlayer;
import com.brackeen.javagamebook.sound.SoundManager;
import com.brackeen.javagamebook.input.InputManager;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.state.*;
import com.brackeen.javagamebook.util.TimeSmoothie;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    static final Logger log = Logger.getLogger("com.brackeen.javagamebook.tilegame");

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);
    	
    private String Mapname;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private InputManager inputManager;
    private GameStateManager gameStateManager;
    private TimeSmoothie timeSmoothie = new TimeSmoothie();

    public void init() {
    	System.out.println("Enter map name: (The Nth map of the series is maps/MAPNAME#N.txt)");
        Scanner sc = new Scanner(System.in);
        Mapname = sc.nextLine();
        if(Mapname.equals("")) Mapname = "map";
        sc.close();
        
        log.setLevel(Level.INFO);

        log.info("init sound manager");
        soundManager = new SoundManager(PLAYBACK_FORMAT, 8);

        log.info("init midi player");
        midiPlayer = new MidiPlayer();

        log.info("init gamecore");
        super.init();

        log.info("init input manager");
        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);


        log.info("init resource manager");
        resourceManager = new TileGameResourceManager(
            screen.getFullScreenWindow().getGraphicsConfiguration(),
            soundManager, midiPlayer,Mapname);


        log.info("init game states");
        gameStateManager = new GameStateManager(inputManager,
            resourceManager.loadImage("loadingsplash.jpg"));
        gameStateManager.addState(new MainGameState(
            soundManager, midiPlayer,
            screen.getWidth(), screen.getHeight()));
        gameStateManager.addState(
            new SplashGameState("gamesplash.jpg"));

        // load resources (in separate thread)
        new Thread() {
            public void run() {
                log.info("loading resources");
                gameStateManager.loadAllResources(resourceManager);
                log.info("setting to Splash state");
                gameStateManager.setState("Splash");
            }
        }.start();
    }


    /**
        Closes any resurces used by the GameManager.
    */
    public void stop() {
        log.info("stopping game");
        super.stop();
        log.info("closing midi player");
        midiPlayer.close();
        log.info("closing sound manager");
        soundManager.close();
    }

    public void update(long elapsedTime) {
        if (gameStateManager.isDone()) {
            stop();
        }
        else {
            elapsedTime = timeSmoothie.getTime(elapsedTime);
            gameStateManager.update(elapsedTime);
        }
    }


    public void draw(Graphics2D g) {
        gameStateManager.draw(g);
    }



}
