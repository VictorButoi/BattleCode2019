from battlecode import BCAbstractRobot, SPECS
import battlecode as bc
import random

__pragma__('iconv')
__pragma__('tconv')
__pragma__('opov')

# don't try to use global variables!!
class MyRobot(BCAbstractRobot):
    step = -1

    def turn(self):
        self.step += 1
        
        if self.me['unit'] == SPECS['CRUSADER']:
            
            visible = self.get_visible_robots()

            # get attackable robots
            attackable = []
            for r in visible:
                # x = 5
                # if not self.is_visible(r):
                if 'x' not in r: #not visible. hacky. do not use at home
                    continue
                # now all in vision range, can see x, y etc
                dist = (r['x'] - self.me['x'])**2 + (r['y'] - self.me['y'])**2
                if r['team'] != self.me['team'] and SPECS['UNITS'][SPECS["CRUSADER"]]['ATTACK_RADIUS'][0] <= dist <= SPECS['UNITS'][SPECS["CRUSADER"]]['ATTACK_RADIUS'][1]:
                    attackable.append(r)

            if attackable:
                # attack first robot
                r = attackable[0]
                
                return self.attack(r['x'] - self.me['x'], r['y'] - self.me['y'])

            # The directions: North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest
            choices = [(0,-1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0), (-1, -1)]
            choice = random.choice(choices)
            
            return self.move(*choice)

        elif self.me['unit'] == SPECS['CASTLE']:
            if self.step < 10:
                
                return self.build_unit(SPECS['CRUSADER'], 1, 1)
                

robot = MyRobot()
