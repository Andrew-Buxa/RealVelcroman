package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private Point tilePoint;
    private Point tilePoint2;
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private Sound velcroClimb;
    private Sound velcroClimb1;
    private Sound velcroClimb2;
    private Sound velcroWalk;
    private Sound shoeShot;
    private Sound shoeSnakeHiss;
    private Sound velcroWhip;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction pause;
    private GameAction rules;
    private GameAction options;
    private GameAction cling;
    private GameAction up;
    private GameAction down;
    private Sequence sequence;

	private boolean paused;

	private JPanel pauseMenu;
	private JButton music;


    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        //prizeSound = soundManager.getSound("sounds/prize.wav");
        //boopSound = soundManager.getSound("sounds/damageGrunt.wav");
//        velcroClimb = soundManager.getSound("sounds/velcroClimb.wav");
//        velcroClimb1 = soundManager.getSound("sounds/velcroClimb_01.wav");
//        velcroClimb2 = soundManager.getSound("sounds/velcroClimb_02.wav");
//        shoeShot = soundManager.getSound("sounds/shoeShot.wav");
//        shoeSnakeHiss = soundManager.getSound("sounds/shoeSnakeHiss.wav");
//        velcroWalk = soundManager.getSound("sounds/velcroWalk.wav");
//        velcroWhip = soundManager.getSound("sounds/VelcroWhip.wav");

        // start music
        midiPlayer = new MidiPlayer();
        sequence =
            midiPlayer.getSequence("sounds/HIP_HOP.MID");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
        
        // creates the menu
        pauseMenu = new JPanel();
        
        JButton resume = new JButton("Resume");
        resume.setFocusable(false);
        resume.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				pause.press();
			}
        	
        });
        JButton exit = new JButton("Exit");
        exit.setFocusable(false);
        exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				stop();
			}
        });
        
        JButton rules = new JButton("Rules");
        rules.setFocusable(false);
        rules.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(pauseMenu, "There are no rules other than O'Doyle Rules \n"
						+ "E: Attach to Velcro Walls \n"
						+ "->: Move left \n"
						+ "<-: Move right \n"
						+ "Space: Jump");
				
			}
        	
        });
        music = new JButton("Music Off");
        music.setFocusable(false);
        music.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (music.getText().equals("Music On")) {
					music.setText("Music Off");
					midiPlayer.setPaused(false);
					
				}else if (music.getText().equals("Music Off")) {
					music.setText("Music On");
					midiPlayer.setPaused(true);
				}
				
				
				
			}
        	
        });
        
        JButton options = new JButton("Options");
        options.setFocusable(false);
        options.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(pauseMenu, "Hello");
				
				
			}
        	
        });
        
        pauseMenu.add(resume);
        pauseMenu.add(rules);
        pauseMenu.add(music);
        pauseMenu.add(exit);
        
        Border border = 
        		BorderFactory.createLineBorder(Color.black);
        pauseMenu.setBorder(border);
        pauseMenu.setVisible(false);
        pauseMenu.setSize(pauseMenu.getPreferredSize());
        
        screen.getFullScreenWindow().getLayeredPane().add(pauseMenu,JLayeredPane.MODAL_LAYER);
    }


    /**
        Closes any resources used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        cling = new GameAction("cling", GameAction.DETECT_INITAL_PRESS_ONLY);
        up = new GameAction("up", GameAction.DETECT_INITAL_PRESS_ONLY);
        down = new GameAction("down", GameAction.DETECT_INITAL_PRESS_ONLY);
//        exit = new GameAction("exit",
//            GameAction.DETECT_INITAL_PRESS_ONLY);
        pause = new GameAction("pause", GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        //inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
        inputManager.mapToKey(cling, KeyEvent.VK_E);
        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
       // inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(pause, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(up,KeyEvent.VK_UP);
        inputManager.mapToKey(down,KeyEvent.VK_DOWN);

    }


    private void checkInput(long elapsedTime) {

    	
//      if (pause.isPressed()) {
//          stop();
//      }
      
      if (pause.isPressed()) {
      	paused = !paused;
      	pauseMenu.setVisible(paused);
      	//inputManager.resetAllGameActions();
      	
      }
      


     /**
      *  Here, we need to add in other key pressed statements, such as e (attach to wall), space (jump with effects of gravity), etc....
      */
      if (!paused) {
	        Player player = (Player)map.getPlayer();
	        
	        //Checks for an X collision
	        float dx = player.getVelocityX();
	        float oldX = player.getX();
	        float newX = oldX + dx * elapsedTime;
	        
	        //Checks for Y collision
	        float dy = player.getVelocityY();
	        float oldY = player.getY();
	        float newY = oldY + dy * elapsedTime;
	        
	        if (player.isAlive()) {
	            float velocityX = 0;
	            float velocityY=player.getVelocityY() +
	                    GRAVITY * elapsedTime;
	            
	            
	            if (moveLeft.isPressed()) {
	            	if(player.getClingingX()!=true){
	            		velocityX-=player.getMaxSpeed();
	            	}
	            }
	            if (moveRight.isPressed()) {
	            	if(player.getClingingX()!=true){
	            		velocityX+=player.getMaxSpeed();
	            	}
	            }
	            
	            if(up.isPressed()&&player.getClingingX()){
	            	velocityY=-player.getMaxSpeed();
	            	player.setVelocityY(velocityY);
	            }
	            
	            if(down.isPressed()&&player.getClingingX()){
	            	velocityY=player.getMaxSpeed();
	            	player.setVelocityY(velocityY);
	            }
	            
	            if (jump.isPressed()) {
	                player.jump(false);
	                player.setClingX(false);
	                player.setClingY(false);
	            }
	            if(cling.isPressed()){
	            	//Check for wall collision
	            	tilePoint=getTileCollision(player, newX-5, player.getY());
	            	tilePoint2=getTileCollision(player,newX+player.getWidth()+5, player.getY());
	            	if(tilePoint==null&&tilePoint2==null){
	            		//Check for Ceiling collision
	            		tilePoint=getTileCollision(player, player.getX(), newY-5);
	            		
	            		if(tilePoint==null){
	            			//Do Nothing
	            		}
	            		else{
	            			//Check to see whether collision is with a specific tile or not.
	            			//If they're clinging to a ceiling
	            			//do work to figure out of the tile that I am colliding with is velcroable
	            			//if it is then:
	            			if(map.getTileVelcro(tilePoint.x, tilePoint.y)){
	            				velocityX=0;
	            				velocityY=0;
	            				player.setVelocityY(velocityY);
		            			player.setClingY(true);
		            			player.setClingX(false);
	            			}
	            		}
	            	}
	            	else if(tilePoint2==null && tilePoint!=null){
	            		if(map.getTileVelcro(tilePoint.x, tilePoint.y)) {
	            	}
	            		velocityX=0;
      				velocityY=0;
      				player.setVelocityY(velocityY);
	            		player.setClingX(true);
	            		player.setClingY(false);
	            	}
	            	
	            	else if(tilePoint==null && tilePoint2!=null){
	            		if(map.getTileVelcro(tilePoint2.x, tilePoint2.y)){
		            		velocityX=0;
	        				velocityY=0;
	        				player.setVelocityY(velocityY);
		            		player.setClingX(true);
		            		player.setClingY(false);
	            		}
	            	}
	            		
	            		//If they're clinging to a wall
	            		//do work to figure out if the tile that I am colliding with is velcro-able
	            		//if(map.getTileVelcro(tilePoint.x, tilePoint.y)||map.getTileVelcro(tilePoint2.x, tilePoint2.y)){
	            		//	velocityX=0;
          			//	velocityY=0;
          			//	player.setVelocityY(velocityY);
		            	//	player.setClingX(true);
		            	//	player.setClingY(false);
	            		//}
	            	//}
	            }
	            player.setVelocityX(velocityX);
	           
	        }
      }

  }

    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
        if (paused) {
        	pauseMenu.paint(g);
        }
       
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
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
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        if (!paused) {
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
	            }
	            // normal update
	            sprite.update(elapsedTime);
	        }
        }
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity if they're not flying or not clung to a wall or ceiling
        if (!creature.isFlying()&&creature.getClingingX()==false&&creature.getClingingY()==false) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }
        
        else if(creature.getClingingX() || creature.getClingingY()){
        	// Do Nothing
        }

        Point tile;
        // change x if the creature isn't colliding or isn't clinging to a wall 
        if(creature.getClingingX()==false){
	        float dx = creature.getVelocityX();
	        float oldX = creature.getX();
	        float newX = oldX + dx * elapsedTime;
	        tile =
	            getTileCollision(creature, newX, creature.getY());
	        if (tile == null) {
	            creature.setX(newX);
	        }
	        else {
	            // line up with the tile boundary
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
	        }
        }
        // change y if the user is climbing wall.
        if(creature.getClingingY()==false){
	        float dy = creature.getVelocityY();
	        float oldY = creature.getY();
	        float newY = oldY + dy * elapsedTime;
	        tile = getTileCollision(creature, creature.getX(), newY);
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
	        }
    }
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
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            }
            else {
                // player dies!
                player.setState(Creature.STATE_DYING);
            }
        }
    }


    /**
        Gives the player the specified power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map = resourceManager.loadNextMap();

        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
        	JOptionPane.showMessageDialog(pauseMenu, "You win!");
        	System.exit(0);
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
       
    }

}