package com.brackeen.javagamebook.tilegame;

import java.awt.Image;

/**
 * A Wrapper class that's for Velcro Tiles
 * @author norriske
 *
 */
public class Tiletype{
	
	private boolean isVelcro;
	private Image image;
	
	public Tiletype(boolean isVelcro, Image image){
		this.isVelcro=isVelcro;
		this.image=image;
	}
	
	public boolean isVelcro(){
		return isVelcro;
	}
	
	public void setVelcro(boolean setVelcro){
		this.isVelcro=setVelcro;
	}
	
	public Image getImage(){
		return image;
	}
	
}
