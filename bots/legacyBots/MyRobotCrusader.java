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
				//log("whoops");
				targetEnemyCastle = determineEnemyCastle(map,startingCastle, true);
				log("refCastle: " + reflecEnemyCastle.x + "," + reflecEnemyCastle.y);

			}
			
			//log("refCastle: " + reflecEnemyCastle.x + "," + reflecEnemyCastle.y);
			//log("here");
			Robot[] nearbyRobots = this.getVisibleRobots();
			ArrayList<Robot> attackable = new ArrayList<Robot>();
			
			for (int s = 0; s < nearbyRobots.length; s++){
				if (!this.isVisible(nearbyRobots[s]))
					continue;
				double dist = Math.pow(calculateDistance(new Pair(nearbyRobots[s].x,nearbyRobots[s].y),currentLocation),2);
				//log("dist calculated: " + dist);
				//log("attack radius :" + this.SPECS.UNITS[3].ATTACK_RADIUS[0] + "," + this.SPECS.UNITS[3].ATTACK_RADIUS[1]);
				if ((nearbyRobots[s].team != this.me.team) && (this.SPECS.UNITS[3].ATTACK_RADIUS[0] <= dist) && (this.SPECS.UNITS[3].ATTACK_RADIUS[1] >= dist)){
					attackable.add(nearbyRobots[s]);	
				}
			}
	
			if (!attackable.isEmpty()){
				Robot picked = attackable.get(0);
				log("ABOUT TO ATTACK, Current Location of Robot: " + currentLocation.x + "," + currentLocation.y);
				return attack(picked.x - currentLocation.x, picked.y - currentLocation.y);
			}
			
			if (!atDestination){
				moveMe(this.me.unit, currentLocation);
			}
			*/