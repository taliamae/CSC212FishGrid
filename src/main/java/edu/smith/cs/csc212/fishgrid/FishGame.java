package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	List<Fish> homeList;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
		
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	
	public static final int NUM_ROCKS = 10;
	public static final int NUM_HEARTS = 3;
	
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		homeList = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		

		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		for (int i=0; i<NUM_HEARTS; i++) {
			world.insertHeartRandomly();
		}
		
		if (this.stepsTaken > 10) {
			
		}
		
		
		world.insertSnailRandomly();
		world.insertFallingRockRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);		
		
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
			
		}		
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		// bring the fish home before we win!
		return player.getX() == home.getX() && player.getY() == home.getY() && missing.isEmpty();
	}
	
	public boolean isHome() {
		return player.getX() == home.getX() && player.getY() == home.getY();
	}
	
	public void takeHome() {
		for (WorldObject items : homeList) {
			world.remove(items);
		}
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		
		if (this.isHome()) {
			this.stepsTaken = 0;
			System.out.println(this.stepsTaken);
		}
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				if (!(wo instanceof Fish)) {
					throw new AssertionError("wo must be a Fish since it was in missing!");
				}
				// Convince Java it's a Fish (we know it is!)
				Fish justFound = (Fish) wo;
				
				// Remove from world.
				found.add(justFound);
				missing.remove(justFound);
				
				// Reset steps taken each time a fish is picked up
				stepsTaken = 0;
				
				// Increase score when you find a fish!
				if (justFound.harderFish) {
					score += 15;
				} else {
					score += 10;
				}	
			}
		}
		
		// When found fish come home, they turn into home fish and disappear off screen
		for (WorldObject items : found) {
			if (this.isHome()) {
				homeList.add((Fish) items);
				this.takeHome();
			}
		}

		for (WorldObject items : homeList) {
			found.remove(items);
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
		
		
		//If fish wander home by accident, they disappear
		for (WorldObject fish : missing) {
			if (fish.getX() == home.getX() && fish.getY() == home.getY()) {
				homeList.add((Fish) fish);
			}
			
		}
		Random rand = ThreadLocalRandom.current();
		for (int i=0; i<found.size(); i++) {
			if (this.stepsTaken > 20) {
				System.out.println(this.stepsTaken);
				//make it so that fish might get LOST
				if (rand.nextDouble() < 0.7 && i > 0) {
					missing.add(found.get(i));
					found.get(i).moveRandomly();
					System.out.println("sorry bye");
				}
			}
		}
		for (Fish fish : missing) {
			found.remove(fish);
		}
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// 30% of the time, lost fish move randomly.
			if (!lost.fastScared) {
				if (rand.nextDouble() < 0.3) {
					lost.moveRandomly();
				} 
			} else {
				if (rand.nextDouble() < 0.8) {
					lost.moveRandomly();
				}
			}
		}
		
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		List<WorldObject> atPoint = world.find(x, y);
		for (WorldObject wo : atPoint) {
			if (wo.isRock()) {
				wo.remove();
			} else {
				throw new AssertionError("Oops! You can only remove rocks.");
			}
		}
		
		

	}
	
}
