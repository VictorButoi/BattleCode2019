

Pair currentLocation = new Pair(this.me.x,this.me.y);
			log("Current Location of Robot: " + currentLocation.x + "," + currentLocation.y);
			calculateLocations();
			target = findShortest(workerOrders);
			log("Nearest Fuel Location: " + target.x + "," + target.y);
			
            

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
				
				workerPath = searchPathResourcecurrentLocation,target);
				Pair toGo = workerPath.poll();
				//log("toGo: " + toGo.x + "," + toGo.y);
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
			Pair currentLocation = new Pair(this.me.x,this.me.y);
			if (turn == 1) // checking to see if just spawned, if so, save castle location and closest deposit
			{
				
				Robot[] nearby = getVisibleRobots();
				for (int i = 0; i < nearby.length; i++)
				{
					if (nearby[i].unit == 0){
						startingCastle = new Pair(nearby[i].x, nearby[i].y);
						startingCastleId = nearby[i].id;
					}
				}
				//log("whoops");
				targetEnemyCastle = determineEnemyCastle(map,startingCastle, horiSymm);
				//log("refCastle: " + reflecEnemyCastle.x + "," + reflecEnemyCastle.y);

			}
			if(!scouted){
				if(Math.pow(calculateDistance(currentLocation,targetEnemyCastle),2) <= this.SPECS.UNITS[2].VISION_RADIUS){
					for(Robot possible: getVisibleRobots()){
						if(possible.unit == 0){
							if(possible.x == targetEnemyCastle.x && possible.y == targetEnemyCastle.y){
								scouted = true;
								break;
							}
						}
					}
					if(scouted == false){
						scouted = true;
						horiSymm = false;
					}
				}
				else{
					return moveMe(this.me.unit,currentLocation, targetEnemyCastle);
				}
			}
			if(!broadcasted){
				log("BROADCAST TO CASTLE");
				int broadcast = horiSymm ? 2 : 1;
				this.castleTalk(broadcast);
				broadcasted = true;
				return moveMe(this.me.unit,currentLocation, targetEnemyCastle);
			}		