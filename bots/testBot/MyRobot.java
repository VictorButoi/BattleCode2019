package bc19;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class MyRobot extends BCAbstractRobot {
	
	public int counter = 0;
	public int enemyIndex;
	public int turn;
	public int startingCastleId;
	public int numberCastles;

	public Pair startingCastle; //when pilgrim first made, it will do a getVisibleRobots and figure out the castle's coordinates
	public Pair startingLocation;
	public Pair target;
	public Pair targetEnemyCastle;

	public ArrayList<Pair> workerOrders = new ArrayList<Pair>();
	public LinkedList<Pair> workerPath = new LinkedList<Pair>();
	public LinkedList<Pair> teamCastles = new LinkedList<Pair>();
	public LinkedList<Pair> enemyCastles = new LinkedList<Pair>();
	public ArrayList<Pair> directions = new ArrayList<Pair>(Arrays.asList(new Pair(0,-1),new Pair(0,1),new Pair(1,0),new Pair(-1,0),new Pair(1,1),new Pair(-1,-1),new Pair(-1,1), new Pair(1,-1)));

	public boolean calculated = false;
	public boolean broadcasted = false;
	public boolean broadcastedSymmetry = false;
	public boolean scouted = false;
	public boolean horiSymm = true; //true = 2, false = 1
	public boolean start;
	public boolean adjustedSymm = false;
	public boolean isFirst = true;
	public boolean finishedSetup;
	public boolean finishInitial;
	public boolean atDestination = false;

	public Robot[] castleArr;
	
	//Pairs are X,Y
    public Action turn() {
    	turn++;

    	if (me.unit == SPECS.CASTLE) {
			Robot currentRobot = this.me;
    		Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1){ // check if first castle in turn
				castleArr = getVisibleRobots();
				numberCastles = castleArr.length;
				teamCastles.add(currentLocation);
				if (numberCastles == 1){
					finishedSetup = true;
				}
			}
			if (!finishedSetup){
				iniCastles(currentLocation);
			}
			if (this.karbonite != 0 && isFirst && turn >= 3 ){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap())){
						log("Building a preacher at: " + (currentLocation.x+selectedDir.x) + ", " + (currentLocation.y+selectedDir.y));
						return buildUnit(SPECS.PREACHER,selectedDir.x,selectedDir.y);
					}
				}
			}
			else if (this.karbonite == 0 && isFirst){
				if (counter < teamCastles.size()){
					Pair allySignal = teamCastles.get(counter);
					int signal = shiftLoc(allySignal);
					counter++;
					if (counter == teamCastles.size()){
						signal = signal + 1;
					}
					this.signal(signal,4);
				}
			}

			else if(turn == 1 && isFirst || turn > 4 && turn < 10 && this.karbonite >= 10){
				for(int d = 0; d < directions.size(); d++){
					Pair selectedDir = directions.get(d);
					if(isPassable(this.map,currentLocation,selectedDir,getVisibleRobotMap())){
						log("Building a pilgrim at: " + (currentLocation.x+selectedDir.x) + ", " + (currentLocation.y+selectedDir.y));
						return buildUnit(SPECS.PILGRIM,selectedDir.x,selectedDir.y);
					}
				}
			}
			
		}
  

    	else if (me.unit == SPECS.PILGRIM) {
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) // checking to see if just spawned, if so, save castle location and closest deposit
			{
				
				Robot[] nearby = getVisibleRobots();
				for (int i = 0; i < nearby.length; i++)
				{
					if (nearby[i].unit == 0){
						startingCastle = new Pair(nearby[i].x, nearby[i].y);
					}
				}
				startingLocation = currentLocation;

				calculateLocations();
				target = findShortest(workerOrders);
				calculated = true;
			}
			//Checking to see if at Destination
			if (currentLocation.x == target.x && currentLocation.y == target.y){
				atDestination = true;
			}

			if (!atDestination) {
				
				workerPath = searchPathResource(currentLocation,target);
				Pair toGo = workerPath.poll();
				return move(toGo.x - this.me.x, toGo.y - this.me.y);
			}
			else{
				Robot robotPilgrim = this.me; //might need to add behavior if nearby enemies
				if (robotPilgrim.fuel < 100){
					return mine();
				}
				else{	
					currentLocation = new Pair(robotPilgrim.x,robotPilgrim.y);
					int diffX = Math.abs(currentLocation.x - startingCastle.x);
					int diffY = Math.abs(currentLocation.y - startingCastle.y);
					if (diffX <= 1 && diffY <= 1){
						atDestination = false;
						return give(startingCastle.x-currentLocation.x, startingCastle.y-currentLocation.y, 0, 100);
					}
					//go back to castle, behavior can be changed to go back to a church or a different castle
					workerPath = searchPathResource(currentLocation,startingLocation);
					Pair toGo = workerPath.poll(); //workerPath.indexOf(currentLocation)
					return move(toGo.x - this.me.x, toGo.y - this.me.y);
				}					
			}	
    	}

        else if (me.unit == SPECS.CRUSADER){
			
		}
		
    	
        else if (me.unit == SPECS.PROPHET){    
        }

        else if (me.unit == SPECS.PREACHER){
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) // checking to see if just spawned, if so, save castle location and closest deposit
			{
				initialization();
				return null;
			}
			Robot[] nearbyRobots = getVisibleRobots();
			if (!finishInitial){
				for (int i = 0; i < nearbyRobots.length; i++){
					int nearbySignal = nearbyRobots[i].signal;
					if (nearbySignal != -1 && nearbyRobots[i].turn >= 5 && nearbyRobots[i].unit == 0){
						Pair allyCastle = unshiftSignal(nearbySignal);
						Pair enemyCastle = determineEnemyCastle(map, allyCastle, horiSymm);
						teamCastles.add(allyCastle);
						enemyCastles.add(enemyCastle);
						if (determineLast(nearbySignal)){
							finishInitial = true;
						}
					}
				}
			}

			LinkedList<Robot> attackable = new LinkedList<Robot>();
			for (Robot pos: getVisibleRobots()){
				if (!this.isVisible(pos))
					continue;
				if(pos != null && pos.team != this.me.team) {
					attackable.add(pos);	
				}
			}

			if(attackable.size() != 0){
				Pair toAttackInit = new Pair(this.me.x,this.me.y,-1);
				Pair toAttack = determineAttackTile(toAttackInit, this.getVisibleRobotMap());
			
				if (toAttack != null){
					log("Attacking at: " + toAttack.x + "," + toAttack.y + " with a weight of : " + toAttack.weight);
					return attack(toAttack.x - currentLocation.x, toAttack.y - currentLocation.y);
				}
			}

			if (finishInitial){
				workerPath = searchPathCastle(currentLocation, enemyCastles.get(0));
				int[][] robot_map = getVisibleRobotMap();
				if (attackable.isEmpty() && (robot_map[enemyCastles.get(0).y][enemyCastles.get(0).x] == 0) ){ //go to next enemy castle, how i determine this is SUPER hacky like i dont even use workerPath.isEmpty(), change this)
					//enemyIndex++;
					Pair removed = enemyCastles.poll();
				}
				return moveMe(this.me.unit,currentLocation, enemyCastles.get(0));
			}
			
        }
	}

    public Pair determineAttackTile(Pair input, int[][] robotMap){
		Pair returnPair = input;

		int xLowerBound = this.me.x - this.SPECS.UNITS[5].ATTACK_RADIUS[1] < 0 ? 0 : this.me.x - this.SPECS.UNITS[5].ATTACK_RADIUS[1];
		int yLowerBound = this.me.y - this.SPECS.UNITS[5].ATTACK_RADIUS[1] < 0 ? 0 : this.me.y - this.SPECS.UNITS[5].ATTACK_RADIUS[1];
		int xUpperBound = this.me.x + this.SPECS.UNITS[5].ATTACK_RADIUS[1] > (robotMap.length - 1) ? (robotMap.length - 1) : this.me.x + this.SPECS.UNITS[5].ATTACK_RADIUS[1];
		int yUpperBound = this.me.y + this.SPECS.UNITS[5].ATTACK_RADIUS[1] > (robotMap.length - 1) ? (robotMap.length - 1) : this.me.y + this.SPECS.UNITS[5].ATTACK_RADIUS[1];

        for(int i = xLowerBound; i < xUpperBound; i++){
            for(int j = xLowerBound; i < xUpperBound; j++){
                if(this.map[i][j]){
                    int temp = calculateRawScore(robotMap, i ,j);
                    if(temp > returnPair.weight){
                        returnPair.setX(i);
                        returnPair.setY(j);
                        returnPair.setWeight(temp);
                    }
                }
            }
        }
        return returnPair.weight < 0 ? null : returnPair;
    }

    public int calculateRawScore(int[][] robotMap, int x, int y){
		int score = 0;

		int lowerX = (x - 1) < 0 ? 0 : (x - 1);
		int higherX = (x + 1) > (this.map.length - 1) ? (this.map.length - 1) : (x + 1);
		int lowerY = (y - 1) < 0 ? 0 : (y - 1);
		int higherY = (y + 1) > (this.map.length - 1) ? (this.map.length - 1) : (y + 1);

        for(int i = lowerX; i < higherX; i++){
            for(int j = lowerY; j < higherY; j++){
                if(this.map[i][j]){
					if(robotMap[i][j] != -1 || robotMap[i][j] != 0){
						if(getRobot(robotMap[i][j]).team != this.me.team){
							score += 2;
						}
						else{
							score -= 5;
						}
					}
				}
            }
		}
		
		return score;
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

	public void iniCastles(Pair loc){
		if (turn == 1){
			castleArr = getVisibleRobots();
			for (int i = 1; i < castleArr.length; i++){
				if(castleArr[i].turn != 0){
					this.castleTalk(loc.x);
					isFirst = false;
				}
			}
		}
		if (turn == 2){
			castleArr = getVisibleRobots();
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
			castleArr = getVisibleRobots();
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
	
	public void initialization(){
		calculateLocations();
		determineSymmetry();
	}

	public Action moveMe(int id, Pair current, Pair destination){
		workerPath = searchPathCastle(current, destination);
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

	public Pair findShortest(ArrayList<Pair> targetArray){
		Pair returnPair = new Pair(this.me.x,this.me.y);
		double distance = -1;
		for(Pair possible: targetArray){
			double newDistance = calculateDistance(new Pair(this.me.x, this.me.y), possible);
			if(distance == -1){
				distance = newDistance;
				returnPair = new Pair(possible.x,possible.y);
			}
			else{
				if(newDistance < distance){
					distance = newDistance;
					returnPair = new Pair(possible.x,possible.y);
				}
			}
		}
		return returnPair;
	}

	public void calculateLocations(){
		for(int i = 0; i < this.map[0].length; i++){
			for(int j = 0; j < this.map.length; j++){
				if(this.fuelMap[i][j]){
					Pair pairToAdd = new Pair(j,i);
					workerOrders.add(pairToAdd);
				}
			}
		}
	}

	public static class Pair implements Comparable<Pair>{
		private int x;
		private int y;
        private double pairDistance;
        private int weight;
		public Pair pathParent;

		public Pair(int x, int y){
			this.x = x;
            this.y = y;
            this.weight = 0;
			this.pairDistance = 0;
			this.pathParent = null;
        }
        
        public Pair(int x, int y, int weight){
			this.x = x;
            this.y = y;
            this.weight = weight;
			this.pairDistance = 0;
			this.pathParent = null;
		}
		
		public void setDistance(Pair origin){
			pairDistance = calculateDistance(origin, this);
        }
        
        public void setX(int pos){
            this.x = pos;
        }

        public void setY(int pos){
            this.y = pos;
        }

        public void setWeight(int weight){
            this.weight = weight;
        }

		@Override public int compareTo(Pair other){
			return Double.compare(pairDistance, other.pairDistance);
		}
	}
	
	//int temp = calculateRawScore(robotMap, i ,j);
	int temp = -5;
	log("Raw score weight: " + temp);
	if(temp > returnPair.weight){
		returnPair.setX(i);
		returnPair.setY(j);
		returnPair.setWeight(temp);
	}

	if(this.map[i][j]){
		if(robotMap[i][j] != -1 && robotMap[i][j] != 0){
			if(getRobot(robotMap[i][j]).team != this.me.team){
				score += 2;
			}
			else{
				score -= 5;
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
				for(int i = 0; i < directions.size(); i++){
					if (isPassable(this.map,check,directions.get(i),getVisibleRobotMap())){
						Pair neighbor = new Pair(check.x+directions.get(i).x,check.y+directions.get(i).y);
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
				if (isPassable(this.map,check,directions.get(i),getVisibleRobotMap())){
					Pair neighbor = new Pair(check.x+directions.get(i).x,check.y+directions.get(i).y);
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
		Pair orig = workerOrders.get(0);
		Pair hOrigReflec = new Pair(orig.x, map.length-orig.y-1);
		if (!this.fuelMap[hOrigReflec.y][hOrigReflec.x]){
			horiSymm = false;
		}
		else{
			horiSymm = true;
		}
	}

	public boolean isPassable(boolean[][] pass_map, Pair loc, Pair dir, int[][] robot_map){
		Pair new_point = new Pair(loc.x+dir.x,loc.y+dir.y);
		if (new_point.x < 0 || new_point.x >= pass_map.length)
			return false;
		if (new_point.y < 0 || new_point.y >= pass_map.length)
			return false;
		if (!pass_map[new_point.y][new_point.x])
			return false;
		if (robot_map[new_point.y][new_point.x] > 0)
			return false;
		return true;			
	}
	
	public static double calculateDistance(Pair origin, Pair target){
		return Math.sqrt(Math.pow(target.x - origin.x,2) + Math.pow(target.y - origin.y,2));
	}

	public Pair determineAttackTile(Pair input, int[][] robotMap){
		Pair returnPair = input;

		int xLowerBound = returnPair.x - 8 < 0 ? 0 : returnPair.x - 8;
		int yLowerBound = returnPair.y - 8 < 0 ? 0 : returnPair.y - 8;
		int xUpperBound = returnPair.x + 8 > (robotMap.length - 2) ? (robotMap.length - 2) : returnPair.x + 8;
		int yUpperBound = returnPair.y + 8 > (robotMap.length - 2) ? (robotMap.length - 2) : returnPair.y + 8;
		
        for(int i = xLowerBound; i < xUpperBound; i++){
			for(int j = yLowerBound; j < yUpperBound; j++){
				double dist = Math.pow(calculateDistance(returnPair, new Pair(j,i)),2);
				if(this.map[j][i] && robotMap[j][i] != -1 && dist <= 4 && dist >= 1){
					log("Yo Im hit doggg");
					//int rawScore = calculateRawScore(robotMap,i,j);
					//log(rawScore + "");
					//if(rawScore > returnPair.weight){
						//returnPair.setX(i);
						//returnPair.setY(j);
						//returnPair.setWeight(rawScore);
					//}
				}
			}
        }
		//return returnPair.weight < 1 ? null : returnPair;
		return null;
		
    }

    public int calculateRawScore(int[][] robotMap, int x, int y){
		int score = 0;

		int lowerX = (x - 1) < 0 ? 0 : (x - 1);
		int higherX = (x + 1) >= (this.map.length - 1) ? (this.map.length - 1) : (x + 1);
		int lowerY = (y - 1) < 0 ? 0 : (y - 1);
		int higherY = (y + 1) >= (this.map.length - 1) ? (this.map.length - 1) : (y + 1);

        for(int i = lowerX; i <= higherX; i++){
            for(int j = lowerY; j <= higherY; j++){
				int currentPoint = robotMap[j][i];
				if(currentPoint > 0){
					if(getRobot(currentPoint).team != this.me.team){
						score = score + 3;
					}
					if(getRobot(currentPoint).team == this.me.team){
						score = score - 5;
					}
				}
			}
		}

		return score;
	}

} 