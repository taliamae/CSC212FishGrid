package edu.smith.cs.csc212.fishgrid;

public class FallingRock extends Rock {

	public FallingRock(World world) {
		super(world);
	}
	
	public void step() {
		//TODO: rock can still fall onto player (fix in CanSwim?)
		this.moveDown();
	}

}
