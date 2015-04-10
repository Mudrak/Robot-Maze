/* FILE: finalMaze.java
*  PROGRAMMER: Erik Mudrak, Transy U 
*  COURSE: Machine Intelligence, Fall 2014
*
* Final Maze Program: Navigates ideally any maze design that has white paper over goal state
*		Wall-following movement correction with sonar
*		Goal state detection with light sensor
*		Bump sensor to turn left
*		Works out of 'dead end' situations
*
* IDEAS: 
*	At the smallest, walls are set 10in, or 25cm apart - bot is 14cm wide - note this for readings on the sonar
*	
*/

import lejos.nxt.*;		// imports all nxt classes
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

class finalMaze {
	// Constants for speed, try to keep mid = low * 1.5
	public static int low = 200;
	public static int mid = 300;
	public static int high = 550;
	 
	////	goalLight:
	// 			Stops bot when it reaches the paper goal location,
	//			Light sensor must be 1 cm above ground, very finnicky
	public boolean goalLight (NXTRegulatedMotor rightMotor, NXTRegulatedMotor leftMotor) {
		//		Checks the light sensor for a white piece of paper, signifying that the goal has been reached
		// 			When pressed against material:
		//				Cardboard: 22
		//				Paper: 28
		// 			When ~1 cm from material 
		//				Cardboard: 46
		//				Paper: 55
		LightSensor ls = new LightSensor(SensorPort.S3);
		//LCD.drawInt(ls.readValue(),4 ,0 ,0);
		int longRead = ls.readValue();
		if (ls.readValue() > 52) {
			rightMotor.stop();
			leftMotor.stop();
			LCD.drawString("GOAL DETECTED!",0,0);
			Sound.beepSequenceUp();
			return true;
		}	
		return false;
	}
	
	//// printDist:
	//		Outputs the distance read by the sonar sensor to the bot's screen
	public void printDist (UltrasonicSensor sonar) {
		LCD.clear();
		LCD.drawInt(sonar.getDistance(), 0, 3);
		LCD.refresh();	
	}
	////  smallReverse function: 
	//		Backs off bot from wall slightly
	public void smallReverse (NXTRegulatedMotor rightMotor, NXTRegulatedMotor leftMotor) {
		DifferentialPilot pilot = new DifferentialPilot (2.1f, 4.5f, leftMotor, rightMotor);
		pilot.setTravelSpeed(8);		// 10 in/sec
		pilot.travel(-2);				// 3 in backwards
	}
	
	//// wallFollow:  -- Motor reverse for deadEnd? Also needs to make sharper turns!
	//		Uses corrective driving with controlMotor to stay in straight line 
	public void wallFollow (UltrasonicSensor sonar, NXTRegulatedMotor rightMotor, NXTRegulatedMotor leftMotor, NXTRegulatedMotor headMotor) {
		int distance = 0;
		distance = sonar.getDistance();
		leftMotor.setSpeed(mid);
		rightMotor.setSpeed(mid);
		leftMotor.forward();
		rightMotor.forward();
		
		//// NAVIGATE LEFT - away from the wall	
		if ((distance > 10) && (distance <= 20)) {
			// Eases to left: right motor has more speed	
			leftMotor.setSpeed(low);
			rightMotor.setSpeed(mid);
			leftMotor.forward();
			rightMotor.forward();
		}
		else if (distance <= 10) {
		// Very close to right wall, turn in place to the left
			leftMotor.setSpeed(low);
			rightMotor.setSpeed(high);
			leftMotor.forward(); 			
			rightMotor.forward();
		}
		//// NAVIGATE RIGHT - towards the wall
		if ((distance > 20) && (distance <= 30)) {
			// Eases to the right, left motor has more speed
			leftMotor.setSpeed(mid);
			rightMotor.setSpeed(low);
			leftMotor.forward();				
			rightMotor.forward();
		}
		else if (distance > 30) {
			// Very far, and close to other wall, turn in place to right
			leftMotor.setSpeed(high);
			rightMotor.setSpeed(low);
			leftMotor.forward();
			rightMotor.forward();
		}
		//// NAVIGATE FORWARD - otherwise keep moving
		else {
			leftMotor.setSpeed(mid);
			rightMotor.setSpeed(mid);
			leftMotor.forward();
			rightMotor.forward();
		}
	}
	
	////
	//// MAIN PROGRAM
	//		Maze navigating robot with wall following, sonar sensing with head, and light sensor testing for goal state
	
	public static void main(String[] args) { 
		//// Initializations
		//
		UltrasonicSensor sonar = new UltrasonicSensor(SensorPort.S1);
		TouchSensor bump = new TouchSensor(SensorPort.S2);
		// continuous ping mode [ON], though it should be on by default
		sonar.continuous();					
		NXTRegulatedMotor rightMotor = new NXTRegulatedMotor (MotorPort.A);
		NXTRegulatedMotor leftMotor = new NXTRegulatedMotor (MotorPort.B);
		NXTRegulatedMotor headMotor = new NXTRegulatedMotor (MotorPort.C);
		
		rightMotor.setSpeed(mid);
		leftMotor.setSpeed(mid);
		headMotor.setSpeed(high);
		finalMaze maze = new finalMaze();
		LCD.drawString("MAZE NAVIGATOR", 0, 1);
		Button.waitForAnyPress();
	 
		 headMotor.rotate(90);
		while (!(maze.goalLight(rightMotor,leftMotor))) {
			maze.printDist(sonar);
			maze.wallFollow(sonar,rightMotor,leftMotor,headMotor);
			if (bump.isPressed()) {
				maze.smallReverse(rightMotor,leftMotor);
				rightMotor.setSpeed(high);
				leftMotor.setSpeed(high);
				// turns bot to left, right wheel was not rotating properly
				leftMotor.rotate(-450);
			}		
		}
		headMotor.rotate(-90);
	}
}