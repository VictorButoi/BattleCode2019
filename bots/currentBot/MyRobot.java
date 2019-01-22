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
	public LinkedList<Pair> resourceSpots = new LinkedList<Pair>();
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
	
	//Pairs are X,Y
    public Action turn() {
    	turn++;

    	if (me.unit == SPECS.CASTLE) {
			Robot currentRobot = this.me;
    		Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1){ // check if first castle in turn
				//log("before castle initial");
				castleInitial();
				//log("castle Initial DONE");
				numberCastles = teamCastlesID.size()+1;
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
			
			if(enemyTargetID != 0){
				boolean found = false;
				Robot pickedTarget = null;
				if (nearbyRobots.length != 0){
					for (int z = 0; z < nearbyRobots.length; z++){
						if (nearbyRobots[z].id == enemyTargetID){
							found = true;
							pickedTarget = nearbyRobots[z];
							break;
						}
					}
				}
				if (found){
					double dist = Math.pow(calculateDistance(new Pair(pickedTarget.x,pickedTarget.y),new Pair(this.me.x,this.me.y)),2);
					if(this.karbonite >= 25 && dist > 16){
						int dy = pickedTarget.y - currentLocation.y;
						int dx = pickedTarget.x - currentLocation.x;
						for(int d = 0; d < directions.size(); d++){
							Pair selectedDir = directions.get(d);
							if (Math.abs(dx) >= Math.abs(dy)){
								dy = pickedTarget.y - currentLocation.y;
								dx = pickedTarget.x - currentLocation.x;
								log("in dx check");
								if (dx > 0){
									//log("dx negative");
									dx = -1;
								}
								else{
									//log("dx positive");
									dx = 1;
								}
								if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false) && selectedDir.x == dx){
								//signal enemy location and make prophet move away, spawn opposite signal
									Pair enemyL = new Pair(pickedTarget.x,pickedTarget.y);
									enemyTargetID = pickedTarget.id;
									this.signal(shiftLoc(enemyL),16);
									return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
								}
							}
							else{
								//log("in dy check");
								if (dy > 0){
									dy = -1;
								}
								else{
									dy = 1;
								}
								if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false) && selectedDir.y == dy){
								//signal enemy location and make prophet move away, spawn opposite side of 
									Pair enemyL = new Pair(pickedTarget.x,pickedTarget.y);
									enemyTargetID = pickedTarget.id;
									this.signal(shiftLoc(enemyL),16);
									return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
								}
							}
						}
					}
					else if(this.fuel >= 10 && dist <= 16){
						return attack(pickedTarget.x - this.me.x, pickedTarget.y - this.me.y);
					}
				}
			}
			/*
			for (int i = 0; i < nearbyRobots.length; i++){
				if (nearbyRobots[i].team == this.me.team && nearbyRobots[i].signal != -1 && nearbyRobots[i].id != this.me.id){
					enemyTargetID = nearbyRobots[i].signal;
					break;
				}
			}
			*/
			
				
			for (int i = 0; i < nearbyRobots.length; i++){

				double dist = Math.pow(calculateDistance(new Pair(nearbyRobots[i].x,nearbyRobots[i].y),new Pair(this.me.x,this.me.y)),2);
				if (nearbyRobots[i].team != this.me.team && this.karbonite >= 25){
					//log("making prophet: " + dist);
					int dy = nearbyRobots[i].y - currentLocation.y;
					int dx = nearbyRobots[i].x - currentLocation.x;
					//log("delta: " + dx + "," + dy);
					for(int d = 0; d < directions.size(); d++){
						Pair selectedDir = directions.get(d);
						if (Math.abs(dx) >= Math.abs(dy)){
							dy = nearbyRobots[i].y - currentLocation.y;
					        dx = nearbyRobots[i].x - currentLocation.x;
							//log("in dx check");
							if (dx > 0){
								//log("dx negative");
								dx = -1;
							}
							else{
								//log("dx positive");
								dx = 1;
							}
							if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false) && selectedDir.x == dx){
							//signal enemy location and make prophet move away, spawn opposite signal
								Pair enemyL = new Pair(nearbyRobots[i].x,nearbyRobots[i].y);
								enemyTargetID = nearbyRobots[i].id;
								this.signal(shiftLoc(enemyL),16);
								return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
							}
						}
						else{
							//log("in dy check");
							if (dy > 0){
								dy = -1;
							}
							else{
								dy = 1;
							}
							if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false) && selectedDir.y == dy){
							//signal enemy location and make prophet move away, spawn opposite side of 
								Pair enemyL = new Pair(nearbyRobots[i].x,nearbyRobots[i].y);
								enemyTargetID = nearbyRobots[i].id;
								this.signal(shiftLoc(enemyL),16);
								return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
							}
						}
					}
				}
				else if(nearbyRobots[i].team != this.me.team && this.fuel >= 10 && (nearbyRobots[i].id == enemyTargetID)){
					log("EVERYTHING MATCHES, CHECKING DIST FOR ATTACK: " + dist);
					log("CASTLE ATTACK RANGE: " + this.SPECS.UNITS[0].ATTACK_RADIUS[1]);
					if (dist <= this.SPECS.UNITS[0].ATTACK_RADIUS[1]){
						log("CASTLE ATTACK");
						return attack(nearbyRobots[i].x - this.me.x, nearbyRobots[i].y - this.me.y);
					}
				}
			}
			
			if (turn >= 100 && karbonite >= 100 && numberCastles == 1){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						int sig = 1;
						this.signal(sig,4);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
				
			}
			
			else if (turn >= 100 && karbonite >= 100 && numberCastles == 2){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap()),false){
						Pair allySignal = teamCastles.get(1);
						int signal = shiftLoc(allySignal);
						signal = signal + 1; //final signal
						this.signal(signal,25);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}
			else if (turn >= 100 && karbonite >= 100 && numberCastles == 3){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						Pair allySignal = teamCastles.get(counter);
						int signal = shiftLoc(allySignal);
						counter++;
						if (counter == teamCastles.size()){
							counter = 1;
							signal = signal + 1;
						}
						this.signal(signal,25);
					}
				}
			}
			else if (pilgrimsMade < 5){ //DO MATH WITH NUMBER OF CASTLES, too many pilgrims, not enough build matts to defend with prophets cant build
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap(),false)){
						pilgrimsMade++;
						return buildUnit(SPECS.PILGRIM,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}
			
			/*
			if (this.karbonite != 0 && isClosest && numberCastles == 1){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap())){
						int sig = 1;
						this.signal(sig,4);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}
			
			else if (this.karbonite != 0 && isClosest && numberCastles == 2){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap())){
						Pair allySignal = teamCastles.get(1);
						int signal = shiftLoc(allySignal);
						signal = signal + 1; //final signal
						this.signal(signal,4);
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}
			
			else if (this.karbonite != 0 && isClosest && numberCastles == 3 && turn >= 3){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap())){
						if (turn == 6){
							Pair allySignal = teamCastles.get(counter);
							int signal = shiftLoc(allySignal);
							counter++;
							if (counter == teamCastles.size()){
								signal = signal + 1;
							}
							this.signal(signal,64);
						}
						return buildUnit(SPECS.PROPHET,selectedDir.x,selectedDir.y); //need better spawns
					}
				}
			}
			if (this.karbonite == 0 && isClosest && numberCastles == 3){
				if (counter < teamCastles.size()){
					Pair allySignal = teamCastles.get(counter);
					int signal = shiftLoc(allySignal);
					counter++;
					if (counter == teamCastles.size()){
						signal = signal + 1;
					}
					this.signal(signal,64);
				}
			}
			*/
		}

  

    	else if (me.unit == SPECS.PILGRIM) {
			
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) // checking to see if just spawned, if so, save castle location and closest deposit
			{
				
				initializationPilgrim();
				resourceSpots = workerOrders;
				target = findShortest(workerOrders);
				//log("searching path for target: " + target.x + "," + target.y);
				workerPath = searchPathResource(currentLocation,target);
				while (workerPath == null){
					//log("path not found, removing: " + target.x + "," + target.y);
					//log("index of removed: " + indexShortest);
					workerOrders.remove(indexShortest);
					target = findShortest(workerOrders);
					//log("next attempted path for target: " + target.x + "," + target.y);
					workerPath = searchPathResource(currentLocation,target);
				}
				Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
				//calculated = true;
			}

			Robot[] nearbyRobots = getVisibleRobots();
			//log("pilgrim befpre fpr loop");
			for (int i = 0; i < nearbyRobots.length; i++){
				if (nearbyRobots[i].team != this.me.team && nearbyRobots[i].unit != 2){
					Pair badLoc = new Pair(nearbyRobots[i].x,nearbyRobots[i].y);
					this.signal(shiftLoc(badLoc),16);
					//log("pilgrim attempting to retreat");
					return retreat(badLoc,currentLocation);
				}
				/*
				else if (nearbyRobots[i].team == this.me.team && nearbyRobots[i].signal != -1 && nearbyRobots[i].id != this.me.id && nearbyRobots[i].unit == 2){
					return retreat(unshiftSignal(nearbyRobots[i].signal),currentLocation);
				}
				*/
					
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
					workerOrders = resourceSpots;
					return give(deposit.x-currentLocation.x, deposit.y-currentLocation.y,this.me.karbonite,this.me.fuel);
				}
				else{
					Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
					return move(toGo.x - this.me.x, toGo.y - this.me.y);
				}
			}
			if (currentLocation.x == target.x && currentLocation.y == target.y){
				//log("AT DESTINATION: " + target.x + "," + target.y);
				atDestination = true;
			}

			if (!atDestination) {
				target = findShortest(workerOrders);
				workerPath = searchPathResource(currentLocation,target);
				while (workerPath == null){
					workerOrders.remove(indexShortest);
					target = findShortest(workerOrders);
					workerPath = searchPathResource(currentLocation,target);
				}
				Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
			}
			else{
				Robot robotPilgrim = this.me; //might need to add behavior if nearby enemies, ADD IF STATEMENT BEFORE MINING TO CHECK DISTANCE FROM CLOSEST CASTLE, IF FAR AWAY, MAKE CHURCH
				Robot[] checkChurches = getVisibleRobots();
				for (int i = 0; i < checkChurches.length; i++){
					if (checkChurches[i].team == this.me.team && checkChurches[i].unit == 1){
						double dist = Math.pow(calculateDistance(new Pair(checkChurches[i].x,checkChurches[i].y),currentLocation),2);
						if (dist <= 16){
							churchNearby = true;
							closeChurch = new Pair(checkChurches[i].x,checkChurches[i].y);
							break;
						}
					}
				}
				if (this.fuel > 400 && this.karbonite > 150 && !churchNearby){
					for (int d = 0; d < directions.size(); d++){
						if (isPassable(this.map, currentLocation, directions.get(d), getVisibleRobotMap(), true)){
							return buildUnit(1,directions.get(d).x,directions.get(d).y);
						}
					}
				}
				if ((robotPilgrim.fuel < 100 && fuelMap[this.me.y][this.me.x]) || (robotPilgrim.karbonite < 20 && karboniteMap[this.me.y][this.me.x])){
					//log("about to mine");
					return mine();
				}
				else{	//need something for checking churches first, if it doesnt exist, go back to castle
					//log("going back to castle because full");
					full = true;
					Pair deposit;
					if (churchNearby){
						deposit = closeChurch;
						workerPath = searchPathCastleAdvanced(currentLocation, closeChurch);
					}
					else{
						deposit = teamCastles.get(0);
						workerPath = searchPathCastleAdvanced(currentLocation,teamCastles.get(0));
					}
					if (workerPath.size() == 0){
						full = false;
						atDestination = false;
						workerOrders = resourceSpots;
						//log("about to give");
						
						return give(deposit.x-currentLocation.x, deposit.y-currentLocation.y, this.me.karbonite, this.me.fuel);
					}
					else{
						//log("not giving,moving");
						Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
						return move(toGo.x - this.me.x, toGo.y - this.me.y);
					}
				}					
			}	
    	}

        else if (me.unit == SPECS.CRUSADER){
			
		}
		
    	
        else if (me.unit == SPECS.PROPHET){  
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) // checking to see if just spawned, if so, save castle location and closest deposit
			{
				initialization();
				//return retreat(teamCastles.get(0), currentLocation);
			}
			Robot[] nearbyRobots = getVisibleRobots();
			//log("status of finishIni: " + finishInitial);
			
			if (!finishInitial){
				for (int i = 0; i < nearbyRobots.length; i++){
					int nearbySignal = nearbyRobots[i].signal;
					if (nearbySignal != -1 && nearbyRobots[i].turn >= 3 && nearbyRobots[i].unit == 0 && nearbyRobots[i].team == this.me.team){
						//log("turn that im receiving: " + turn);
						Pair allyCastle = unshiftSignal(nearbySignal);
						//log("unshiftedAllyCastleReceived: " + allyCastle.x + "," + allyCastle.y);
						Pair enemyCastle = determineEnemyCastle(map, allyCastle, horiSymm);
						//log("unshiftedENEMYCastleReceived: " + enemyCastle.x + "," + enemyCastle.y);
						teamCastles.add(allyCastle);
						enemyCastles.add(enemyCastle);
						if (determineLast(nearbySignal)){
							finishInitial = true;
						}
					}
					/*
					if (nearbyRobots[i].team != this.me.team){
						double dist = Math.pow(calculateDistance(new Pair(nearbyRobots[i].x,nearbyRobots[i].y),currentLocation),2);
						if (dist <= this.SPECS.UNITS[4].ATTACK_RADIUS[0]){
							Robot enemyBack = nearbyRobots[i];
							return moveBackwards(new Pair(enemyBack.x,enemyBack.y),currentLocation);
						}
						if (sniperAttack(nearbyRobots[i], dist, currentLocation)){					//might need priorty, super early in game so debatable
							log("BAD ATTACKBAD ATTACKBAD ATTACKBAD ATTACKBAD ATTACKBAD ATTACK");
							return attack(nearbyRobots[i].x - currentLocation.x, nearbyRobots[i].y - currentLocation.y);
						}
					}
					*/
					/*
					else if (!finishInitial){ //debatable if good
						if (nearbyRobots[i].unit == 0){
							startingCastle = new Pair(nearbyRobots[i].x, nearbyRobots[i].y);
							if (turn <= 2){
								workerPath = searchPathCastle(currentLocation, determineEnemyCastle(this.map,startingCastle,horiSymm));
							}
							else{
								workerPath = searchPathCastleAdvanced(currentLocation, determineEnemyCastle(this.map,startingCastle,horiSymm));
							}
							Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
							return move(toGo.x - this.me.x, toGo.y - this.me.y);
						}
					}
					*/
				}
			}
			
			LinkedList<Robot> attackable = new LinkedList<Robot>();
			LinkedList<Robot> enemyRobots = new LinkedList<Robot>();
			LinkedList<Robot> enemyArchers = new LinkedList<Robot>();
			
			for (int s = 0; s < nearbyRobots.length; s++){
				//if (nearbyRobots[s].team == this.me.team && nearbyRobots[s].
				if (!this.isVisible(nearbyRobots[s]))
					continue;
				//log("nearbyRobot[s]: " + nearbyRobots[s].x + "," + nearbyRobots[s].y);
				double dist = Math.pow(calculateDistance(new Pair(nearbyRobots[s].x,nearbyRobots[s].y),currentLocation),2);
				if (sniperAttack(nearbyRobots[s], dist, currentLocation)){
					attackable.add(nearbyRobots[s]);	
				}
				if (nearbyRobots[s].team != this.me.team && nearbyRobots[s].unit != 0 && nearbyRobots[s].unit != 1 && nearbyRobots[s].unit != 2){
					enemyRobots.add(nearbyRobots[s]);
					if (nearbyRobots[s].unit == 4){
						enemyArchers.add(nearbyRobots[s]);
					}
				}
				if (nearbyRobots[s].team == this.me.team && nearbyRobots[s].unit == 4 && nearbyRobots[s].signal != -1 && nearbyRobots[s].id != this.me.id){
					enemyTargetID = nearbyRobots[s].signal;
				}
			}
			if (!enemyRobots.isEmpty()){
				for (int d = 0; d < enemyRobots.size(); d++){
					Robot seen = enemyRobots.get(d);
					double distBetween = Math.pow(calculateDistance(new Pair(seen.x,seen.y),currentLocation),2);
					//log("enemy robot: " + seen.x + "," + seen. y);
					//log("distBeteween in loop: " + distBetween);
					if (distBetween <= 16 && (seen.unit == 5 || seen.unit == 3)){
						Robot enemyBack = seen;
						Pair enemyBackLoc = new Pair(enemyBack.x,enemyBack.y);
						workerPath = searchPathAvoid(currentLocation, enemyBackLoc, enemyBackLoc );
						if (workerPath != null){
							log("kiting");
							Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
							return move(toGo.x - this.me.x, toGo.y - this.me.y);
						}
						return moveBackwards(enemyBackLoc,currentLocation); //THIS WAS CHANGED
					}
				}
			}
			
			if (enemyTargetID != 0){
				boolean found = false;
				Robot pickedTarget = null;
				if (!attackable.isEmpty()){
					for (int z = 0; z < attackable.size(); z++){
						if (attackable.get(z).id == enemyTargetID){
							found = true;
							pickedTarget = attackable.get(z);
							break;
						}
					}
				}
				if (found){
					this.signal(pickedTarget.id, 9);
					//log("COMMED ATTACK");
					return attack(pickedTarget.x - currentLocation.x, pickedTarget.y - currentLocation.y);
				}
			}
			
			if (!enemyArchers.isEmpty()){ //attack archers first, everything else later
				int indexRobot = 0;
				Robot first = enemyArchers.get(0);
				Pair firstLoc = new Pair(first.x,first.y);
				Pair lowestLoc = firstLoc;
				int lowestDistance = Math.abs(firstLoc.x-currentLocation.x) + Math.abs(firstLoc.y-currentLocation.y);//Math.pow(calculateDistance(firstLoc,currentLocation),2);
				for (int a = 1; a < enemyArchers.size(); a++){
					Robot picked = enemyArchers.get(a); //work on specific attack target selection, this is closest target
					Pair pickedLoc = new Pair(picked.x,picked.y);
					int tempDist = Math.abs(pickedLoc.x-currentLocation.x) + Math.abs(pickedLoc.y-currentLocation.y);
					if (tempDist < lowestDistance){
						lowestDistance = tempDist;
						lowestLoc = pickedLoc;
						indexRobot = a;
					}
				}
				this.signal(enemyArchers.get(indexRobot).id, 9);
				return attack(lowestLoc.x - currentLocation.x, lowestLoc.y - currentLocation.y);
			}
			
			if (!attackable.isEmpty()){
				int indexRobot = 0;
				Robot first = attackable.get(0);
				Pair firstLoc = new Pair(first.x,first.y);
				Pair lowestLoc = firstLoc;
				int lowestDistance = Math.abs(firstLoc.x-currentLocation.x) + Math.abs(firstLoc.y-currentLocation.y);//Math.pow(calculateDistance(firstLoc,currentLocation),2);
				for (int a = 1; a < attackable.size(); a++){
					Robot picked = attackable.get(a); //work on specific attack target selection, this is closest target
					Pair pickedLoc = new Pair(picked.x,picked.y);
					int tempDist = Math.abs(pickedLoc.x-currentLocation.x) + Math.abs(pickedLoc.y-currentLocation.y);
					if (tempDist < lowestDistance){
						lowestDistance = tempDist;
						lowestLoc = pickedLoc;
						indexRobot = a;
					}
				}
				//log("good attack");
				this.signal(attackable.get(indexRobot).id, 9);
				return attack(lowestLoc.x - currentLocation.x, lowestLoc.y - currentLocation.y);
			}
			
			
			if (finishInitial){
				//log("here");
				int[][] robot_map = getVisibleRobotMap();
				//log("next castle: " + enemyCastles.get(0).x + "," + enemyCastles.get(0).y);
				if (attackable.isEmpty() && (robot_map[enemyCastles.get(0).y][enemyCastles.get(0).x] == 0) ){ //go to next enemy castle, how i determine this is SUPER hacky like i dont even use workerPath.isEmpty(), change this)
					//enemyIndex++;
					Pair removed = enemyCastles.poll();
				}
	
				//workerPath = searchPathCastle(currentLocation, enemyCastles.get(0));
				//Pair toGo = new Pair(workerPath.get(0).x, workerPath.get(0).y);
				//return move(toGo.x - this.me.x, toGo.y - this.me.y);
				return moveMe(this.me.unit,currentLocation, enemyCastles.get(0));
			}
	
        }

        else if (me.unit == SPECS.PREACHER){
			
        }
		
		else if (me.unit == SPECS.CHURCH){
		}
	}
	public boolean sniperAttack(Robot near, double dist, Pair currentLoc){ //hardcoded for mages might need to consider case where more than 1 enemy in self splash range
		boolean shouldAttack = false;
		if((near.team != this.me.team) && (this.SPECS.UNITS[4].ATTACK_RADIUS[0] <= dist) && (this.SPECS.UNITS[4].ATTACK_RADIUS[1] >= dist)){
			shouldAttack = true;
		}
		return shouldAttack;
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
	/*
	public void iniCastles(Pair loc){
		if (turn == 1){
			for (int i = 1; i < castleArr.length; i++){
				if(castleArr[i].turn != 0){
					this.castleTalk(loc.x);
					isFirst = false;
				}
			}
		}
		if (turn == 2){
			if(isFirst){
				for (int i = 1; i < castleArr.length; i++){
					int x = castleArr[i].castle_talk;
					if(castleArr[i].castle_talk != 0){
						teamCastles.add(new Pair(x,0));
					}
				}
			}
			else if (!isFirst){
				castleTalk(loc.y);
				finishedSetup = true;
			}
		}	
		if (turn == 3){
			if (isFirst){
				for (int i = 1; i < castleArr.length; i++){
					int y = castleArr[i].castle_talk;
					if(castleArr[i].castle_talk != 0){
						teamCastles.get(i).y = y;
					}
					finishedSetup = true;
				}
			}	
		}
	}
	*/
	
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
	
	public void initializationPilgrim(){
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
		if(horiSymm){
			movementDirections = horiSymmDirections;
		}
		else{
			movementDirections = vertSymmDirections;
		}
	}
	
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
	

	public Pair findShortest(LinkedList<Pair> targetArray){
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
				if(newDistance < distance){
					distance = newDistance;
					indexShortest = i;
					returnPair = new Pair(possible.x,possible.y);
				}
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
						if(this.fuelMap[i][j] || this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							workerOrders.add(pairToAdd);
						}
					}
				}
			}
			else{
				for(int i = center; i < this.map[0].length; i++){
					for(int j = 0; j < this.map.length; j++){
						if(this.fuelMap[i][j] || this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							workerOrders.add(pairToAdd);
						}
					}
				}
			}
		}
		else{
			if (locCastle.x < center){
				for(int i = 0; i < this.map[0].length; i++){
					for(int j = 0; j < this.map.length/2; j++){
						if(this.fuelMap[i][j] || this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							workerOrders.add(pairToAdd);
						}
					}
				}
			}
			else{
				for(int i = 0; i < this.map[0].length; i++){
					for(int j = center; j < this.map.length; j++){
						if(this.fuelMap[i][j] || this.karboniteMap[i][j]){
							Pair pairToAdd = new Pair(j,i);
							workerOrders.add(pairToAdd);
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
				if (isPassable(this.map,check,movementDirections.get(i),getVisibleRobotMap())){
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
					/*
					if (turn >= 8){
						for (int d = 0; d < directions.size(); d++){
							int[][] currentMap = getVisibleRobotMap();
							Pair potential = new Pair(neighbor.x + directions.get(d).x, neighbor.y + directions.get(d).y);
							int potentialID = currentMap[potential.y][potential.x];
							Robot potentialRobot = getRobot(potentialID);
							if (potentialRobot != null && potentialRobot.team == this.me.team){
								log("not putting this in path: " + potential.x + "," + potential.y);
								close = false;
								break;
							}
						}
					}
					*/
					
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