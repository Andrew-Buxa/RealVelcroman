package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.tilegame.sprites.*;


/**
    The ResourceManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class ResourceManager {

    private ArrayList<Tiletype> tiles;
    private int currentMap;
    private GraphicsConfiguration gc;
    private ClassLoader cl;

    // host sprites used for cloning
    private Sprite playerSprite;
    private Sprite musicSprite;
    private Sprite coinSprite;
    private Sprite goalSprite;
    private Sprite snailSprite;
    private Sprite shoeGullSprite;
    private Sprite shoeSnakeSprite;
    private Sprite brickFlopSprite;
    private Sprite spikeSprite;
    private Sprite winShoe;

    /**
        Creates a new ResourceManager with the specified
        GraphicsConfiguration.
    */
    public ResourceManager(GraphicsConfiguration gc) {    	
        this.gc = gc;
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
    }


    /**
        Gets an image from the images/ directory.
    */
    public Image loadImage(String name) {
        String filename = "images/" + name;
        return new ImageIcon(filename).getImage();
    }


    public Image getMirrorImage(Image image) {
        return getScaledImage(image, 1, 1);
        // -1 , 1
    }


    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, 1);
        // 1 , -1
    }


    private Image getScaledImage(Image image, float x, float y) {

        // set up the transform
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
            (x-1) * image.getWidth(null) / 2,
            (y-1) * image.getHeight(null) / 2);

        // create a transparent (not translucent) image
        Image newImage = gc.createCompatibleImage(
            image.getWidth(null),
            image.getHeight(null),
            Transparency.BITMASK);

        // draw the transformed image
        Graphics2D g = (Graphics2D)newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }


    public TileMap loadNextMap() {
        TileMap map = null;
        while (map == null) {
            currentMap++;
            try {
                map = loadMap(
                    "maps/map" + currentMap + ".txt");
            }
            catch (IOException ex) {
                if (currentMap == 1) {
                    // no maps to load!
                    return null;
                }
                currentMap = 0;
                map = null;
            }
        }

        return map;
    }


    public TileMap reloadMap() {
        try {
            return loadMap(
                "maps/map" + currentMap + ".txt");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    private TileMap loadMap(String filename)
        throws IOException
    {
        ArrayList lines = new ArrayList();
        int width = 0;
        int height = 0;
        String backgroundFileName = "Background.png";
        // read every line in the text file into the list
        BufferedReader reader = new BufferedReader(
            new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
            	if (line.startsWith("+")) {
            	backgroundFileName = line.substring(1);	
            	} else {
                lines.add(line);
                width = Math.max(width, line.length());
	            }
            }
        }

        // parse the lines to create a TileEngine
        height = lines.size();
        TileMap newMap = new TileMap(width, height);
        newMap.setBackground(loadImage(backgroundFileName));
        for (int y=0; y<height; y++) {
            String line = (String)lines.get(y);
            for (int x=0; x<line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                }

                // check if the char represents a sprite
                else if (ch == 'o') {
                    addSprite(newMap, coinSprite, x, y);
                }
                else if (ch == '!') {
                    addSprite(newMap, musicSprite, x, y);
                }
                else if (ch == '*') {
                    addSprite(newMap, goalSprite, x, y);
                }
                else if (ch == '1') { // Shoe snake
                    addSprite(newMap, shoeSnakeSprite, x, y);
                }
                else if (ch == '2') { // Shoe snail
                	addSprite(newMap, snailSprite, x, y);
                } 
                else if (ch == '3') { // Shoegull
                	addSprite(newMap, shoeGullSprite, x, y);
                } 
                else if (ch == '4') { // Brick Flop
                	addSprite(newMap, brickFlopSprite, x, y);
                } 
                else if (ch == 's') {
                	addSprite(newMap, spikeSprite, x, y);
                }
                else if (ch == 'l') { // Thw win shoe
                	addSprite(newMap, winShoe, x ,y);
                } 
                else if (ch == 'p') { // Player
                	Sprite player = (Sprite)playerSprite.clone();
                	player.setX(TileMapRenderer.tilesToPixels(x));
                	player.setY(TileMapRenderer.tilesToPixels(y));
                	newMap.setPlayer(player);
                }
            }
        }

        // add the player to the map
//        Sprite player = (Sprite)playerSprite.clone();
//        player.setX(TileMapRenderer.tilesToPixels(3));
//        player.setY(0);
//        newMap.setPlayer(player);

        return newMap;
    }


    private void addSprite(TileMap map,
        Sprite hostSprite, int tileX, int tileY)
    {
        if (hostSprite != null) {
            // clone the sprite from the "host"
            Sprite sprite = (Sprite)hostSprite.clone();

            // center the sprite
            sprite.setX(
                TileMapRenderer.tilesToPixels(tileX) +
                (TileMapRenderer.tilesToPixels(1) -
                sprite.getWidth()) / 2);

            // bottom-justify the sprite
            sprite.setY(
                TileMapRenderer.tilesToPixels(tileY + 1) -
                sprite.getHeight());

            // add it to the map
            map.addSprite(sprite);
        }
    }


    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------


    public void loadTileImages() {
    	// keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ directory
    	
        tiles = new ArrayList<Tiletype>();
        char ch = 'A';
        while (true) {
            String name = "tile_" + ch + ".png";
            File file = new File("images/" + name);
            if (!file.exists()) {
                break;
            }
            if(name.equals("tile_C.png")){
            	tiles.add(new Tiletype(true, loadImage(name)));
            }
            else{
            	tiles.add(new Tiletype(false, loadImage(name)));
            }
            ch++;
        }
    }
    public ArrayList<Tiletype> getTileType(){
    	return tiles;
    }

    public void loadCreatureSprites() {

        Image[][] images = new Image[4][];

        // load left-facing images
        String idle = "images/Velcroman_Idle_000.png";
        String idle2 = "images/Velcroman_Idle_001.png";
        String idle3 = "images/Velcroman_Idle_002.png";
        String shoeGull0 = "images/Velcroman_Idle_000.png";
        String shoeGull1 = "images/Velcroman_Idle_001.png";
        String shoeGull2 = "images/Velcroman_Idle_001.png";
        String shoeSnail0 = "images/Velcroman_Idle_000.png";
        String shoeSnail1 = "images/Velcroman_Idle_001.png";
        String shoeSnail2 = "images/Velcroman_Idle_002.png";
        String shoeSnail3 = "images/Velcroman_Idle_003.png";
        String shoeSnail4 = "images/Velcroman_Idle_004.png";
        String shoeSnail5 = "images/Velcroman_Idle_005.png";
        String shoeSnail6 = "images/Velcroman_Idle_006.png";
		images[0] = new Image[] {
        	loadImage("images/Velcroman_Idle_000.png/"),
        	loadImage("Velcroman_Idle_001.png"),
			loadImage("Velcroman_Idle_002.png"),
			loadImage("Shoegull_000.png"),
			loadImage("Shoegull_001.png"),
			loadImage("Shoegull_002.png"),
			loadImage("ShoeSnail_000.png"),
			loadImage("ShoeSnail_001.png"),
			loadImage("ShoeSnail_002.png"),
			loadImage("ShoeSnail_003.png"),
			loadImage("ShoeSnail_004.png"),
			loadImage("ShoeSnail_005.png"),
			loadImage("ShoeSnail_006.png"),
        };

        images[1] = new Image[images[0].length];
        images[2] = new Image[images[0].length];
        images[3] = new Image[images[0].length];
        for (int i=0; i<images[0].length; i++) {
            // right-facing images
            images[1][i] = getMirrorImage(images[0][i]);
            // left-facing "dead" images
            images[2][i] = getFlippedImage(images[0][i]);
            // right-facing "dead" images
            images[3][i] = getFlippedImage(images[1][i]);
        }

        // create creature animations
        Animation[] playerAnim = new Animation[4];
        Animation[] flyAnim = new Animation[4];
        Animation[] grubAnim = new Animation[4];
        Animation[] spikeAnim = new Animation[4];
        
        for (int i=0; i<4; i++) {
            playerAnim[i] = createPlayerAnim(
                images[i][0], images[i][1], images[i][2]);
            flyAnim[i] = createFlyAnim(
                images[i][3], images[i][4], images[i][5]);
            grubAnim[i] = createShoeSnailAnim(
                images[i][6], images[i][7], images[i][8], images[i][9], images[i][10], images[i][11], images[i][12]);
           
        }

        // create creature sprites
        playerSprite = new Player(playerAnim[0], playerAnim[1],
            playerAnim[2], playerAnim[3]);
        shoeGullSprite = new Fly(flyAnim[0], flyAnim[1],
            flyAnim[2], flyAnim[3]);
        snailSprite = new Grub(grubAnim[0], grubAnim[1],
            grubAnim[2], grubAnim[3]);
        
    }

    private Animation createSpikeAnim(Image image) {
    	Animation anim = new Animation();
    	anim.addFrame(loadImage("spike.png"), 20);
    	
    	return anim;
    }
    private Animation createPlayerAnim(Image player1,
        Image player2, Image player3)
    {
        Animation anim = new Animation();
        anim.addFrame(loadImage("Velcroman_Walk_000.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_001.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_002.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_003.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_004.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_005.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_006.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_007.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_008.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_009.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_010.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_011.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_012.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_013.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_014.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_015.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_016.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_017.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_018.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_019.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_020.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_021.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_022.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_023.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_024.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_025.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_024.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_023.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_022.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_021.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_020.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_019.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_018.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_017.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_016.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_015.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_014.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_013.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_012.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_011.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_010.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_009.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_008.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_007.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_006.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_005.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_004.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_003.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_002.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_001.png"),20);
        anim.addFrame(loadImage("Velcroman_Walk_000.png"),20);
        
        return anim;
    }


    private Animation createFlyAnim(Image img1, Image img2,
        Image img3)
    {
        Animation anim = new Animation();
        anim.addFrame(loadImage("Shoegull_000.png"),20);
        anim.addFrame(loadImage("Shoegull_001.png"),20);
        anim.addFrame(loadImage("Shoegull_002.png"),20);
        anim.addFrame(loadImage("Shoegull_003.png"),20);
        anim.addFrame(loadImage("Shoegull_004.png"),20);
        anim.addFrame(loadImage("Shoegull_005.png"),20);
        anim.addFrame(loadImage("Shoegull_006.png"),20);
        anim.addFrame(loadImage("Shoegull_007.png"),20);
        anim.addFrame(loadImage("Shoegull_008.png"),20);
        anim.addFrame(loadImage("Shoegull_009.png"),20);
        anim.addFrame(loadImage("Shoegull_010.png"),20);
        anim.addFrame(loadImage("Shoegull_011.png"),20);
        anim.addFrame(loadImage("Shoegull_012.png"),20);
        anim.addFrame(loadImage("Shoegull_011.png"),20);
        anim.addFrame(loadImage("Shoegull_010.png"),20);
        anim.addFrame(loadImage("Shoegull_009.png"),20);
        anim.addFrame(loadImage("Shoegull_008.png"),20);
        anim.addFrame(loadImage("Shoegull_007.png"),20);
        anim.addFrame(loadImage("Shoegull_006.png"),20);
        anim.addFrame(loadImage("Shoegull_005.png"),20);
        anim.addFrame(loadImage("Shoegull_004.png"),20);
        anim.addFrame(loadImage("Shoegull_003.png"),20);
        anim.addFrame(loadImage("Shoegull_002.png"),20);
        anim.addFrame(loadImage("Shoegull_001.png"),20);
        anim.addFrame(loadImage("Shoegull_000.png"),20);
        return anim;
    }


    private Animation createShoeSnailAnim(Image img1, Image img2, Image img3, Image img4, Image img5, Image img6, Image img7) {
        Animation anim = new Animation();
        anim.addFrame(img1, 20);
        anim.addFrame(img2, 20);
        anim.addFrame(img3, 20);
        anim.addFrame(img4, 20);
        anim.addFrame(img5, 20);
        anim.addFrame(img6, 20);
        anim.addFrame(img7, 20);
        anim.addFrame(img6, 20);
        anim.addFrame(img5, 20);
        anim.addFrame(img4, 20);
        anim.addFrame(img3, 20);
        anim.addFrame(img2, 20);
        anim.addFrame(img1, 20);
        return anim;
    }
    
    private Animation createSnakeAnim(Image img1, Image img2) {
        Animation anim = new Animation();
        anim.addFrame(loadImage("shoestring_snake.png"), 20);
        
        return anim;
    }


    private void loadPowerUpSprites() {
        // create "goal" sprite
        Animation anim = new Animation();
        anim.addFrame(loadImage("heart1.png"), 150);
        anim.addFrame(loadImage("heart2.png"), 150);
        anim.addFrame(loadImage("heart3.png"), 150);
        anim.addFrame(loadImage("heart2.png"), 150);
        goalSprite = new PowerUp.Goal(anim);

        // create "star" sprite
        anim = new Animation();
        anim.addFrame(loadImage("key_1.png"), 100);
        anim.addFrame(loadImage("key_2.png"), 100);
        anim.addFrame(loadImage("key_3.png"), 100);
        anim.addFrame(loadImage("key_4.png"), 100);
        anim.addFrame(loadImage("key_5.png"), 100);
        anim.addFrame(loadImage("key_4.png"), 100);
        anim.addFrame(loadImage("key_3.png"), 100);
        anim.addFrame(loadImage("key_2.png"), 100);
        anim.addFrame(loadImage("key_1.png"), 100);
        coinSprite = new PowerUp.Star(anim);

        // create "music" sprite
        anim = new Animation();
        anim.addFrame(loadImage("shoe.png"),20);
        anim.addFrame(loadImage("shoe1.png"),20);
        anim.addFrame(loadImage("shoe2.png"),20);
        anim.addFrame(loadImage("shoe3.png"),20);
        anim.addFrame(loadImage("shoe2.png"),20);
        anim.addFrame(loadImage("shoe1.png"),20);
        anim.addFrame(loadImage("shoe.png"),20);
        musicSprite = new PowerUp.Music(anim);
        
       
        
        
    }

}
