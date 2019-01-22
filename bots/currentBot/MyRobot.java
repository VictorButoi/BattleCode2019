package bc19;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int startingCastleId;
	public Pair startingCastle; //when pilgrim first made, it will do a getVisibleRobots and figure out the castle's coordinates
	public boolean atDestination = false;
	public ArrayList<Pair> directions = new ArrayList<Pair>(Arrays.asList(new Pair(0,-1),new Pair(0,1),new Pair(1,0),new Pair(-1,0),new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1)));
	public ArrayList<Pair> directionsDiagonal = new ArrayList<Pair>(Arrays.asList(new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1)));
	public ArrayList<Pair> movementDirections = new ArrayList<Pair>();
	public ArrayList<Pair> horiSymmDirections = new ArrayList<Pair>(Arrays.asList(new Pair(0,-2),new Pair(0,2),new Pair(0,-1),new Pair(0,1),new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1),new Pair(2,0),new Pair(-2,0),new Pair(1,0),new Pair(-1,0)));
	public ArrayList<Pair> vertSymmDirections = new ArrayList<Pair>(Arrays.asList(new Pair(2,0),new Pair(-2,0),new Pair(1,0),new Pair(-1,0),new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1),new Pair(0,-2),new Pair(0,2),new Pair(0,-1),new Pair(0,1)));
	public ArrayList<Pair> attackDirections = new ArrayList<Pair>(Arrays.asList(new Pair(2,0),new Pair(-2,0),new Pair(1,0),new Pair(-1,0),new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1),new Pair(0,-2),new Pair(0,2),new Pair(0,-1),new Pair(0,1)));
	public boolean calculated = false;
	public Pair startingLocation;
	public Pair target;
	public LinkedList<Pair> workerOrders = new LinkedList<Pair>();
	public LinkedList<Pair> fuelOrders = new LinkedList<Pair>();
	public LinkedList<Pair> karboniteOrders = new LinkedList<Pair>();
	public LinkedList<Pair> copyFuel = new LinkedList<Pair>();
	public LinkedList<Pair> copyKarbonite = new LinkedList<Pair>();
	public LinkedList<Pair> workerPath = new LinkedList<Pair>();
	public LinkedList<Pair> teamCastles = new LinkedList<Pair>();
	public LinkedList<Pair> enemyCastles = new LinkedList<Pair>();
	public LinkedList<Integer> teamCastlesID = new LinkedList<Integer>();
	public LinkedList<Integer> enemyRobotID = new LinkedList<Integer>();
	public boolean broadcasted = false;
	public boolean broadcastedSymmetry = false;
	public boolean scouted = false;
	public boolean horiSymm = true; //true = 2, false = 1
	public Pair targetEnemyCastle;
	public boolean start;
	public boolean adjustedSymm = false;
	public boolean isFirst = true;
	public int numberCastles;
	public boolean finishedSetup;
	public Robot[] castleArr;
	public int counter = 1;
	public boolean finishInitial;
	public int enemyIndex;
	public boolean isClosest;
	public int pilgrimsBuilt;
	public boolean hasAttacked = false;
	public int enemyTargetID;
	public boolean back = false;
	public boolean full;
	public int indexShortest;
	public int pilgrimsMade;
	public Pair closeChurch;
	public boolean churchNearby = false;
	public Pair latticeSpot;
	public boolean settled = false;
	public boolean castleNearby;
	public boolean miningKarb;
	public boolean churchBuilt;
	
	//Pairs are X,Y
    public Action turn() {
    	turn++;

    	if (me.unit == SPECS.CASTLE) {
			Robot currentRobot = this.me;
    		Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1){ // check if first castle in turn
				//log("before castle initial");
				castleInitial();
				numberCastles = teamCastlesID.size()+1;
				//log("pilgrims to be made per castle: " + ((fuelOrders.size()+karboniteOrders.size())/numberCastles));
				teamCastles.add(currentLocation);
				if (numberCastles == 1){
					finishedSetup = true;
					isClosest = true;
				}
			}
			if (!finishedSetup){
				iniCastlesClosest(currentLocation);
			}
			Robot[] nearbyRobots = getVisibleRobots();
			
			if ((pilgrimsMade < (((fuelOrders.size()+karboniteOrders.size())/numberCastles))) && karbonite >= 25){ //DO MATH WITH NUMBER OF CASTLES, too many pilgrims, not enough build matts to defend with prophets cant build
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						pilgrimsMade++;
						return buildUnit(SPECS.PILGRIM,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}

			else if (this.karbonite >= 150 && this.fuel > 600 && turn > 60){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						int sig = 1;
						this.signal(sig,4);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
				
			}
		
		}

  

    	else if (me.unit == SPECS.PILGRIM) {
			
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) {
				initializationGeneral();
			}
			if (target == null){ //check which resource desired, populate workerOrders
				
				int[][] occupiedMap = getVisibleRobotMap();
				Pair tempKarb = findShortest(karboniteOrders,true,true);
				Pair tempFuel = findShortest(fuelOrders,true,false);
				if (tempKarb == null && tempFuel != null){
					target = tempFuel;
				}
				else if (tempFuel == null && tempKarb != null){
					target = tempKarb;
				}
				else if(tempKarb != null && tempFuel != null){
					target = shorterTwo(tempKarb,tempFuel,currentLocation);
				}
				else if (tempKarb == null && tempFuel == null){
					karboniteOrders = copyKarbonite;
					fuelOrders = copyFuel;
					workerPath = searchPathCastleAdvanced(currentLocation,teamCastles.get(0));
					Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
					target = null;
					return move(toGo.x - this.me.x, toGo.y - this.me.y);
					
				}
				while (occupiedMap[target.y][target.x] > 0){ //do an if counter = 10, just return null
					tempKarb = findShortest(karboniteOrders,true,true);
					tempFuel = findShortest(fuelOrders,true,false);
					if (tempKarb == null && tempFuel != null){
						target = tempFuel;
					}
					else if (tempFuel == null && tempKarb != null){
						target = tempKarb;
					}
					else if(tempKarb != null && tempFuel != null){
						target = shorterTwo(tempKarb,tempFuel,currentLocation);
					}
					else if (tempKarb == null && tempFuel == null){
						karboniteOrders = copyKarbonite;
						fuelOrders = copyFuel;
						workerPath = searchPathCastleAdvanced(currentLocation,teamCastles.get(0));
						Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
						target = null;
						return move(toGo.x - this.me.x, toGo.y - this.me.y);
					}
				}
				//log("my turn: " + this.me.turn);
				//log("picked target: " + target.x + "," + target.y);
				workerPath = searchPathResource(currentLocation,target);
				Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
				//log("before move");
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
				
			}

			if(!atDestination){
				if (currentLocation.x == target.x && currentLocation.y == target.y){
					atDestination = true;
				}
				else{
					int[][] occupiedMap = getVisibleRobotMap();
					if (occupiedMap[target.y][target.x] <= 0){
						//log("going to next spot");
						workerPath = searchPathResource(currentLocation,target);
						Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
						return move(toGo.x - this.me.x, toGo.y - this.me.y);
					}
					else{ //only occurs when very little spots left, maybe here go to enemy side resource tiles?
						//log("occupied target tile");
						//log("occupied target tile");
						target = null;
						return null;
					}
				}
			}

			if (full){ //going back to castle/church
				Pair deposit;
				if (churchNearby){
					deposit = closeChurch;
					workerPath = searchPathCastleAdvanced(currentLocation,closeChurch);
				}
				else{
					deposit = teamCastles.get(0);
					workerPath = searchPathCastleAdvanced(currentLocation,teamCastles.get(0));
				}
				
				if (workerPath.size() == 0){
					full = false;
					atDestination = false;
					//log("giving");
					return give(deposit.x-currentLocation.x, deposit.y-currentLocation.y,this.me.karbonite,this.me.fuel);
				}
				else{
					Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
					return move(toGo.x - this.me.x, toGo.y - this.me.y);
				}
			}
			
			if (atDestination && !full){
				Robot robotPilgrim = this.me;
				Robot[] checkChurches = getVisibleRobots();
				if(!castleNearby && closeChurch == null && !churchNearby){
					for (int i = 0; i < checkChurches.length; i++){
						if (checkChurches[i].team == this.me.team && checkChurches[i].unit == 1){
							double dist = Math.pow(calculateDistance(new Pair(checkChurches[i].x,checkChurches[i].y),currentLocation),2);
							if (dist <= 36){
								churchNearby = true;
								closeChurch = new Pair(checkChurches[i].x,checkChurches[i].y);
							}
						}
						else if(checkChurches[i].team == this.me.team && checkChurches[i].unit == 0){
							double dist = Math.pow(calculateDistance(new Pair(checkChurches[i].x,checkChurches[i].y),currentLocation),2);
							if (dist <= 36){
								churchNearby = false;
								castleNearby = true;
								closeChurch = null;
								break;
							}
						}
					}
				}
				if (this.fuel > 300 && this.karbonite > 50 && !churchNearby && !castleNearby){
					for (int d = 0; d < directions.size(); d++){
						if (isPassable(this.map, currentLocation, directions.get(d), getVisibleRobotMap(), true)){
							return buildUnit(1,directions.get(d).x,directions.get(d).y);
						}
					}
				}
				if ((robotPilgrim.fuel < 100 && fuelMap[this.me.y][this.me.x]) || (robotPilgrim.karbonite < 10 && karboniteMap[this.me.y][this.me.x])){
					if (robotPilgrim.fuel == 90 || robotPilgrim.karbonite == 8){
						full = true;
					}
					return mine();
				}
			}
		}

		else if (me.unit == SPECS.PROPHET){
			if (turn == 1){
				initializationGeneral();
			}
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if(latticeSpot == null){
				double lowestDistance = 1000;
				Pair lowestPair = null;	
				for (int dx = -8; dx < 9; dx++){
					for (int dy = -8; dy < 9; dy++){
						Pair tempPair = new Pair(this.me.x + dx, this.me.y + dy);
						if(((dx*dx)+(dy*dy)) > SPECS.UNITS[this.me.unit].VISION_RADIUS){
							continue;
						}
						double tempDistance = Math.pow(calculateDistance(currentLocation,tempPair),2);
						int modOp = (tempPair.x + tempPair.y)% 2;
						if(isPassable(this.map,currentLocation,new Pair(dx,dy),getVisibleRobotMap(),true) && tempDistance < lowestDistance && modOp == 0){
							lowestPair = tempPair;
							lowestDistance = tempDistance;
						}
					}
			 	}
				latticeSpot = lowestPair;
				if (latticeSpot == null){
					Pair randomMovement = null;
					randomMovement = directionsDiagonal.get((int)Math.floor(Math.random() * directionsDiagonal.size()));
					while (!isPassable(this.map,currentLocation,randomMovement,getVisibleRobotMap(),false)){
						randomMovement = directionsDiagonal.get((int)Math.floor(Math.random() * directionsDiagonal.size()));
					}
					return move(randomMovement.x, randomMovement.y);
				}
			}
			
			if(!settled){
				if (this.me.x == latticeSpot.x && this.me.y == latticeSpot.y){
					settled = true;
				}
				else{
					int[][] occupyMap = getVisibleRobotMap();
					if (occupyMap[latticeSpot.y][latticeSpot.x] <= 0){
						workerPath = searchPathResource(currentLocation,latticeSpot);
						Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
						return move(toGo.x-currentLocation.x,toGo.y-currentLocation.y);
					}
					else{
						latticeSpot = null;
						return null;
					}
				}
			}
				
			/*
				for(int j = 0; j < passableDirections.size(); j++){
					int toGoX = passableDirections.get(j).x;
					int toGoY = passableDirections.get(j).y;

					if((this.me.x + toGoX + this.me.y + toGoY) % 2 == 0){
						settled = true;
						return move(toGoX, toGoY);
					}
				}
				if(!settled && passableDirections.size() != 0){
					Pair randomMovement = passableDirections.get((int)Math.floor(Math.random() * passableDirections.size()));
					return move(randomMovement.x, randomMovement.y);
				}
			}
			*/
        }

        else if (me.unit == SPECS.CRUSADER){		
		}	

        else if (me.unit == SPECS.PREACHER){			
        }
		
		else if (me.unit == SPECS.CHURCH){
			if (this.karbonite >= 150 && this.fuel > 600 && turn > 60){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						int sig = 1;
						this.signal(sig,4);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
				
			}
		}
	}
	public Pair getClosestCastleLoc(){

	}

	public boolean sniperAttack(Robot near, double dist, Pair currentLoc){ //hardcoded for mages might need to consider case where more than 1 enemy in self splash range
		boolean shouldAttack = false;
		if((near.team != this.me.team) && (this.SPECS.UNITS[4].ATTACK_RADIUS[0] <= dist) && (this.SPECS.UNITS[4].ATTACK_RADIUS[1] >= dist)){
			shouldAttack = true;
		}
		return shouldAttack;
	}
	
	public Pair shorterTwo(Pair a, Pair b, Pair currentLocation){
		double distA = Math.pow(calculateDistance(a,currentLocation),2);
		double distB = Math.pow(calculateDistance(b,currentLocation),2);
		if (distA <= distB){
			fuelOrders.add(b);
			return a;
		}
		else{
			karboniteOrders.add(a);
			return b;
		}
	}
	
	public int shiftLoc(Pair loc){
		int locX = loc.x << 10;
		int locY = loc.y << 4;
		int signal = locX+locY;
		return signal;
	}
	
	public Pair unshiftSignal(int signal){
		int locX = signal >> 10;
		int maskX = 63;
		int locY = (signal >> 4) & maskX;
		return new Pair(locX,locY);
	}
	
	public boolean determineLast(int signal){
		int isLast = signal & 1;
		if (isLast == 1){
			return true;
		}
		return false;
	}
	
	public Action retreat(Pair enemySpot, Pair currentLoc){
		int dx = -3*(enemySpot.x - currentLoc.x);
		int dy = -3*(enemySpot.y - currentLoc.y);
		int newX = currentLoc.x + dx;
		int newY = currentLoc.y + dy;
		Pair newLoc = new Pair(newX,newY);
		workerPath = searchPathCastleAdvanced(currentLoc, newLoc);
		Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
		return move(toGo.x-currentLoc.x,toGo.y-currentLoc.y);
	}
	
	public Action moveBackwards(Pair enemySpot,Pair currentLoc){ //CHANGE THIS
		Pair locCastle = teamCastles.get(0);
		Pair possiblePoint = null;
		boolean findNew = false;
		int yUse;
		int xUse;
		
		for (int z = 0; z < directions.size(); z++){
			Pair adjTile = new Pair(locCastle.x + directions.get(z).x, locCastle.y + directions.get(z).y);
			if (adjTile.x == currentLoc.x && adjTile.y == currentLoc.y){
				findNew = true;
				break;
			}
		}
		
		if(findNew){
			if(horiSymm){
				int center = map.length/2; //NEED TO CHANGE THIS
				if (locCastle.y < center){
					for (int i = locCastle.y - 4; i >= 0; i--){
						yUse = i;
						possiblePoint = new Pair(locCastle.x,yUse);
						if (isPassable(this.map,possiblePoint,null,getVisibleRobotMap(),false)){
							workerPath = searchPathAvoid(currentLoc,possiblePoint, enemySpot);
							break;
						}
					}
				}
				else{
					for (int i = locCastle.y + 4; i < map.length; i++){
						yUse = i;
						possiblePoint = new Pair(locCastle.x,yUse);
						if (isPassable(this.map,possiblePoint,null,getVisibleRobotMap(),false)){
							workerPath = searchPathAvoid(currentLoc,possiblePoint, enemySpot);
							break;
						}
					}
				}
			}
			else{
				int center = map.length/2;
				if (locCastle.x < center){
					for (int i = 0; i < map.length; i++){
						xUse = i;
						possiblePoint = new Pair(xUse,locCastle.y);
						if (isPassable(this.map,possiblePoint,null,getVisibleRobotMap(),false)){
							workerPath = searchPathAvoid(currentLoc,possiblePoint, enemySpot);
							break;
						}
					}
				}
				else{
					for (int i = map.length-1; i >= center; i--){
						xUse = i;
						possiblePoint = new Pair(xUse,locCastle.y);
						if (isPassable(this.map,possiblePoint,null,getVisibleRobotMap(),false)){
							workerPath = searchPathAvoid(currentLoc,possiblePoint, enemySpot);
							break;
						}
					}
				}
			}
		}
		else{
			workerPath = searchPathAvoid(currentLoc,locCastle, enemySpot);
		}
	
		if (workerPath == null){
			return moveMe(this.me.unit, currentLoc, enemyCastles.get(0));
		}
		
		
	}
	
	public void iniCastlesClosest(Pair loc){
		if (turn == 1){
			this.castleTalk(loc.x);
			for (int i = 0; i < teamCastlesID.size(); i++){
				Robot pickCastle = getRobot(teamCastlesID.get(i));
				if(pickCastle.castle_talk != 0){
					int x = pickCastle.castle_talk;
					teamCastles.add(i+1,new Pair(x,0));
				}
			}
		}
		if (turn == 2){
			for (int i = 0; i < teamCastlesID.size(); i++){
				Robot pickCastle = getRobot(teamCastlesID.get(i));
				if(pickCastle.castle_talk != 0 && (pickCastle.turn < this.me.turn)){
					int x = pickCastle.castle_talk;
					teamCastles.add(i+1,new Pair(x,0));
					this.castleTalk(loc.y);
				}
			}
			for (int i = 0; i < teamCastlesID.size(); i++){
				Robot pickCastle = getRobot(teamCastlesID.get(i));
				if(pickCastle.castle_talk != 0 && (pickCastle.turn == this.me.turn)){
					int y = pickCastle.castle_talk;
					teamCastles.get(i+1).y = y;
					this.castleTalk(loc.y);
				}
			}
		}
		if (turn == 3){
			//log("here");
			for (int i = 0; i < teamCastlesID.size(); i++){
				Robot pickCastle = getRobot(teamCastlesID.get(i));
				if(pickCastle.castle_talk != 0&& (pickCastle.turn < this.me.turn)){
					int y = pickCastle.castle_talk;
					teamCastles.get(i+1).y = y;
				}
			}
			determineClosest();
			finishedSetup = true;
		}
	}
	
	public void determineClosest(){
		//log("determining Closest");
		int distMe;
		int center = this.map.length/2;
		//log("center: " + center);
		if(horiSymm){
			distMe = Math.abs(this.me.y - center);
		}
		else{
			distMe = Math.abs(this.me.x - center);
		}
		for (int i = 1; i < teamCastles.size(); i++){
			if(horiSymm){
				int distOther = Math.abs(teamCastles.get(i).y - center);
				//log("index: " + i + " " + teamCastles.get(i).y);
				if (distMe < distOther){									//this doesn't work if all 3 are same axis, check for it
					isClosest = true;
				}
				else if(distMe == distOther && (this.me.x < teamCastles.get(i).x)){
					isClosest = true;
				}
				else{
					isClosest = false;
					break;
				}
			}
			else{
				int distOther = Math.abs(teamCastles.get(i).x - center);
				//log("V-distMe: " + distMe);
				//log("V-distOther: " + distOther);
				if (distMe < distOther){
					isClosest = true;
				}
				else if(distMe == distOther && (this.me.y < teamCastles.get(i).y)){
					isClosest = true;
				}
				else{
					isClosest = false;
					break;
				}
			}
		}
	}
	
	public void initializationGeneral(){
		determineSymmetry();
		Robot[] nearby = getVisibleRobots();
		for (int i = 0; i < nearby.length; i++){
			if (nearby[i].unit == 0){
				startingCastle = new Pair(nearby[i].x, nearby[i].y);
				teamCastles.add(startingCastle);
				enemyCastles.add(determineEnemyCastle(this.map,startingCastle,horiSymm));
			}
		}
		calculateLocations();
		copyKarbonite = karboniteOrders;
		copyFuel = fuelOrders;
		if(horiSymm){
			movementDirections = horiSymmDirections;
		}
		else{
			movementDirections = vertSymmDirections;
		}
	}
	/*
	public void initialization(){
		determineSymmetry(); //changed
		Robot[] nearby = getVisibleRobots();
		for (int i = 0; i < nearby.length; i++){
			if (nearby[i].unit == 0){
				startingCastle = new Pair(nearby[i].x, nearby[i].y);
				teamCastles.add(startingCastle);
				enemyCastles.add(determineEnemyCastle(this.map,startingCastle,horiSymm));
			}
			if (nearby[i].signal == 1){
				//log("NEARBY SIGNAL IS 1, 1 CASLTE, SETTING FINISH INITIAL TO TRUE");
				finishInitial = true;
			}
		}
		calculateLocations(); // might need to change
		if(horiSymm){
			movementDirections = horiSymmDirections;
		}
		else{
			movementDirections = vertSymmDirections;
		}
	}
	*/
	
	public void castleInitial(){
		Robot[] temp = getVisibleRobots();
		for (int i = 0; i < temp.length; i++){
			if (temp[i].id != this.me.id){
				teamCastlesID.add(temp[i].id);
			}
		}
		determineSymmetry();
		teamCastles.add(new Pair(this.me.x,this.me.y));
		enemyCastles.add(determineEnemyCastle(this.map,teamCastles.get(0),horiSymm));
		calculateLocations();
	}

	public Action moveMe(int id, Pair current, Pair destination){
		workerPath = searchPathCastleAdvanced(current, destination);
		for(int i = 0; i < workerPath.size(); i++){
			Pair toGo = new Pair(workerPath.get(i).x, workerPath.get(i).y);
			double dist = Math.pow(calculateDistance(toGo,current),2);
			if (dist > this.SPECS.UNITS[id].SPEED){
				toGo = new Pair(workerPath.get(i-1).x, workerPath.get(i-1).y);
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
			}
			else if (workerPath.size() == 1){
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
			}
		}
	}
	
	public Action normalMove(int id, Pair current, Pair destination){
		workerPath = searchPathCastle(current, destination);
		Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
		return move(toGo.x - this.me.x, toGo.y - this.me.y);
	}
	
	public Action diagonalMove(int id, Pair current, Pair destination){
		workerPath = searchPathDiagonal(current, destination);
		Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
		return move(toGo.x - this.me.x, toGo.y - this.me.y);
	}
	
	public Pair findShortest(LinkedList<Pair> targetArray, boolean remove, boolean isKarbonite){
		Pair returnPair;
		double distance = -1;
		for (int i = 0; i < targetArray.size(); i++){
			Pair possible = targetArray.get(i);
			double newDistance = Math.pow(calculateDistance(new Pair(this.me.x, this.me.y), possible),2);
			if(distance == -1){
				distance = newDistance;
				indexShortest = i;
				returnPair = new Pair(possible.x,possible.y);
			}
			else{
				if(newDistance < distance){
					distance = newDistance;
					indexShortest = i;
					returnPair = new Pair(possible.x,possible.y);
				}
			}
		}
		if (remove && distance != -1){
			if(isKarbonite){
				karboniteOrders.remove(indexShortest);
			}
			else{
				fuelOrders.remove(indexShortest);
			}
		}
		return returnPair;
	}
	
	public Pair findLongest(LinkedList<Pair> targetArray){
		Pair returnPair = new Pair(this.me.x,this.me.y);
		double distance = -1;
		for (int i = 0; i < targetArray.size(); i++){
			Pair possible = targetArray.get(i);
			double newDistance = calculateDistance(new Pair(this.me.x, this.me.y), possible);
			if(distance == -1){
				distance = newDistance;
				indexShortest = i;
				returnPair = new Pair(possible.x,possible.y);
			}
			else{
				if(newDistance > distance){
					distance = newDistance;
					indexShortest = i;
					returnPair = new Pair(possible.x,possible.y);
				}
			}
		}
		return returnPair;
	}

	public void calculateLocations(){
		int center = this.map.length/2;
		Pair locCastle = new Pair(teamCastles.get(0).x,teamCastles.get(0).y);
		if (horiSymm){
			if (locCastle.y < center){
				for(int i = 0; i < this.map[0].length/2; i++){
					for(int j = 0; j < this.map.length; j++){
						if(this.fuelMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							fuelOrders.add(pairToAdd);
						}
						else if (this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							karboniteOrders.add(pairToAdd);
						}
					}
				}
			}
			else{
				for(int i = center; i < this.map[0].length; i++){
					for(int j = 0; j < this.map.length; j++){
						if(this.fuelMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							fuelOrders.add(pairToAdd);
						}
						else if (this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							karboniteOrders.add(pairToAdd);
						}
					}
				}
			}
		}
		else{
			if (locCastle.x < center){
				for(int i = 0; i < this.map[0].length; i++){
					for(int j = 0; j < this.map.length/2; j++){
						if(this.fuelMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							fuelOrders.add(pairToAdd);
							//log("adding fuel: " + pairToAdd.x + "," + pairToAdd.y);
						}
						else if (this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							karboniteOrders.add(pairToAdd);
							//log("adding K: " + pairToAdd.x + "," + pairToAdd.y);
						}
					}
				}
			}
			else{
				for(int i = 0; i < this.map[0].length; i++){
					for(int j = center; j < this.map.length; j++){
						if(this.fuelMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							fuelOrders.add(pairToAdd);
						}
						else if (this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							karboniteOrders.add(pairToAdd);
						}
					}
				}
			}
		}
	}

	public LinkedList<Pair> constructShortestPath(Pair start, Pair fromEnd){
		LinkedList<Pair> path = new LinkedList<Pair>();
		while (fromEnd.pathParent != null){
			path.add(0,fromEnd);
			fromEnd = fromEnd.pathParent;
		}
		return path;
	}
	
	public LinkedList<Pair> searchPathResource(Pair start, Pair end) {
		boolean[][] closed = new boolean[map.length][map.length];
		boolean[][] openGrid = new boolean[map.length][map.length];
		LinkedList<Pair> open = new LinkedList<Pair>();
		open.add(start);
		start.pathParent = null;
		while (!open.isEmpty()) {
			Pair check = open.poll();
			openGrid[check.y][check.x] = false;
			if ((check.x == end.x) && (check.y == end.y)) {
				return constructShortestPath(start,check);
			}
			else {
				closed[check.y][check.x] = true;
				for(int i = 0; i < movementDirections.size(); i++){
					if (isPassable(this.map,check,movementDirections.get(i),getVisibleRobotMap(),false)){
						Pair neighbor = new Pair(check.x+movementDirections.get(i).x,check.y+movementDirections.get(i).y);
						if (!closed[neighbor.y][neighbor.x] && !openGrid[neighbor.y][neighbor.x]){
							neighbor.pathParent = check;
							open.add(neighbor);
							openGrid[neighbor.y][neighbor.x] = true;
						}	
						
					}
				}
			}
		}
		return null;
	}
	
	public LinkedList<Pair> searchPathDiagonal(Pair start, Pair end) {
		boolean[][] closed = new boolean[map.length][map.length];
		boolean[][] openGrid = new boolean[map.length][map.length];
		LinkedList<Pair> open = new LinkedList<Pair>();
		open.add(start);
		start.pathParent = null;
		while (!open.isEmpty()) {
			Pair check = open.poll();
			openGrid[check.y][check.x] = false;
			for (int z = 0; z < directions.size(); z++){
				Pair adjTile = new Pair(end.x + directions.get(z).x, end.y + directions.get(z).y);
				if ((check.x == adjTile.x) && (check.y == adjTile.y)) {
					return constructShortestPath(start,check);
				}
			}
			closed[check.y][check.x] = true;
			for(int i = 0; i < directionsDiagonal.size(); i++){
				if (isPassable(this.map,check,directionsDiagonal.get(i),getVisibleRobotMap(),false)){
					Pair neighbor = new Pair(check.x+directionsDiagonal.get(i).x,check.y+directionsDiagonal.get(i).y);
					if (!closed[neighbor.y][neighbor.x] && !openGrid[neighbor.y][neighbor.x]){
						neighbor.pathParent = check;
						open.add(neighbor);
						openGrid[neighbor.y][neighbor.x] = true;
					}	
				}					
			}
		}
		return null;
	}
	
	public LinkedList<Pair> searchPathAvoid(Pair start, Pair end, Pair enemyLocation) { //if in a narrow corridor,
		boolean[][] closed = new boolean[map.length][map.length];
		boolean[][] openGrid = new boolean[map.length][map.length];
		LinkedList<Pair> open = new LinkedList<Pair>();
		//log("searching avoidance path for enemylocation: " + enemyLocation.x + "," + enemyLocation.y);
		open.add(start);
		start.pathParent = null;
		while (!open.isEmpty()) {
			Pair check = open.poll();
			openGrid[check.y][check.x] = false;
			for (int z = 0; z < directions.size(); z++){
				Pair adjTile = new Pair(end.x + directions.get(z).x, end.y + directions.get(z).y);
				if ((check.x == adjTile.x) && (check.y == adjTile.y)) {
					return constructShortestPath(start,check);
				}
			}
			closed[check.y][check.x] = true;	
			for(int i = 0; i < movementDirections.size(); i++){
				if (isPassable(this.map,check,movementDirections.get(i),getVisibleRobotMap(),false)){
					Pair neighbor = new Pair(check.x+movementDirections.get(i).x,check.y+movementDirections.get(i).y);
					double dist = Math.pow(calculateDistance(neighbor,enemyLocation),2);
					if (dist <= 16){ //this.SPECS.UNITS[4].ATTACK_RADIUS[0]
						continue;
					}
					if (!closed[neighbor.y][neighbor.x] && !openGrid[neighbor.y][neighbor.x]){
						//log("close: " + close);
						neighbor.pathParent = check;
						open.add(neighbor);
						openGrid[neighbor.y][neighbor.x] = true;
					}
				}
			}
		}
		return null;
	}
	
	public LinkedList<Pair> searchPathCastleAdvanced(Pair start, Pair end) { //if in a narrow corridor,
		boolean[][] closed = new boolean[map.length][map.length];
		boolean[][] openGrid = new boolean[map.length][map.length];
		LinkedList<Pair> open = new LinkedList<Pair>();
		open.add(start);
		start.pathParent = null;
		while (!open.isEmpty()) {
			Pair check = open.poll();
			openGrid[check.y][check.x] = false;
			for (int z = 0; z < directions.size(); z++){
				Pair adjTile = new Pair(end.x + directions.get(z).x, end.y + directions.get(z).y);
				if ((check.x == adjTile.x) && (check.y == adjTile.y)) {
					return constructShortestPath(start,check);
				}
			}
			closed[check.y][check.x] = true;	
			for(int i = 0; i < movementDirections.size(); i++){
				if (isPassable(this.map,check,movementDirections.get(i),getVisibleRobotMap(),false)){
					Pair neighbor = new Pair(check.x+movementDirections.get(i).x,check.y+movementDirections.get(i).y);
					boolean close = true;	
					if (close && !closed[neighbor.y][neighbor.x] && !openGrid[neighbor.y][neighbor.x]){
						neighbor.pathParent = check;
						open.add(neighbor);
						openGrid[neighbor.y][neighbor.x] = true;
					}
				}
			}
		}
		return null;
	}

	public LinkedList<Pair> searchPathCastle(Pair start, Pair end) { //if in a narrow corridor,
		boolean[][] closed = new boolean[map.length][map.length];
		boolean[][] openGrid = new boolean[map.length][map.length];
		LinkedList<Pair> open = new LinkedList<Pair>();
		open.add(start);
		start.pathParent = null;
		while (!open.isEmpty()) {
			Pair check = open.poll();
			openGrid[check.y][check.x] = false;
			for (int z = 0; z < directions.size(); z++){
				Pair adjTile = new Pair(end.x + directions.get(z).x, end.y + directions.get(z).y);
				if ((check.x == adjTile.x) && (check.y == adjTile.y)) {
					return constructShortestPath(start,check);
				}
			}
			closed[check.y][check.x] = true;	
			for(int i = 0; i < directions.size(); i++){
				if (isPassable(this.map,check,directions.get(i),getVisibleRobotMap(),false)){
					Pair neighbor = new Pair(check.x+directions.get(i).x,check.y+directions.get(i).y);
					boolean close = true;
					
					if (close && !closed[neighbor.y][neighbor.x] && !openGrid[neighbor.y][neighbor.x]){
						neighbor.pathParent = check;
						open.add(neighbor);
						openGrid[neighbor.y][neighbor.x] = true;
					}
				}
			}
		}
		return null;
	}

	public Pair determineEnemyCastle(boolean[][] pass_map, Pair loc, boolean horizontalSymm){  
		Pair v_reflec = new Pair(pass_map.length - loc.x-1,loc.y);
		Pair h_reflec = new Pair(loc.x, pass_map.length-loc.y-1);
		if (horizontalSymm){
			if (pass_map[h_reflec.y][h_reflec.x]){
				return h_reflec;
			}
			else{
				return v_reflec;
			}
		}
		else{
			if(pass_map[v_reflec.y][v_reflec.x]){
				return v_reflec;
			}
			else{
				return h_reflec;
			}
		}
	}
	
	public void determineSymmetry(){
		Pair orig = null;
		for(int i = 0; i < this.map[0].length; i++){
			for(int j = 0; j < this.map.length; j++){
				if(this.fuelMap[i][j]){
					orig = new Pair(j,i);
					break;
				}
			}
			if (orig != null){
				break;
			}
		}
		Pair hOrigReflec = new Pair(orig.x, map.length-orig.y-1);
		if (!this.fuelMap[hOrigReflec.y][hOrigReflec.x]){
			horiSymm = false;
		}
		else{
			horiSymm = true;
		}
	}

	public boolean isPassable(boolean[][] pass_map, Pair loc, Pair dir, int[][] robot_map, boolean isChurch){
		Pair new_point = new Pair(this.me.x,this.me.y);

		if(dir != null){
			new_point = new Pair(loc.x+dir.x,loc.y+dir.y);
		}
		else{
			new_point = loc;
		}

		if (new_point.x < 0 || new_point.x >= pass_map.length)
			return false;
		if (new_point.y < 0 || new_point.y >= pass_map.length)
			return false;
		if (!pass_map[new_point.y][new_point.x])
			return false;
		if (robot_map[new_point.y][new_point.x] > 0)
			return false;
		if(isChurch){
			if (fuelMap[new_point.y][new_point.x] || karboniteMap[new_point.y][new_point.x])
				return false;
		}
		return true;			
	}
	
	public static double calculateDistance(Pair origin, Pair target){
		return Math.sqrt(Math.pow(target.x - origin.x,2) + Math.pow(target.y - origin.y,2));
	}
	public static class Pair implements Comparable<Pair>{
		private int x;
		private int y;
		private double pairDistance;
		public Pair pathParent;

		public Pair(int x, int y){
			this.x = x;
			this.y = y;
			this.pairDistance = 0;
			this.pathParent = null;
		}
		
		public void setDistance(Pair origin){
			pairDistance = calculateDistance(origin, this);
		}

		@Override public int compareTo(Pair other){
			return Double.compare(pairDistance, other.pairDistance);
		}
	}

} 