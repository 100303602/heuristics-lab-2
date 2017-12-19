import sys
import copy
import time

# start timer
start_time = time.time()

class Car:
	def __init__(self, point, cat, order):
		# x,y position
		self.point = point
		# category
		self.cat = cat
		# arrival order
		self.order = order

class State:
	def __init__(self, parking):
		# parking information
		self.parking = parking
		# parent state (for reconstructing the path)
		self.parent = None
		# h function value
		self.H = 0
		# g function value
		self.G = 0

	def move_cost(self, child):
		#print(child)
		cost = 0
		localCost = 0
		for x in range(M):
			for y in range(N):
				for i in range(M):
					for j in range(N):
						if self.parking[x][y].cat == child.parking[i][j].cat and self.parking[x][y].order == child.parking[i][j].order and self.parking[x][y].cat != '_' and child.parking[i][j].cat != '_':
							if (x,y) != (i,j):
								if self.parking[x][y].point[0] == child.parking[i][j].point[0] and self.parking[x][y].point[1] == self.parking[i][j].point[1]:
									localCost = 0
								elif self.parking[x][y].point[1] == 0 and self.parking[i][j].point[1] != child.parking[i][j].point[0] and child.parking[i][j].point[1] == 0:
									localCost = 3
								elif self.parking[x][y].point[1] == N and self.parking[i][j].point[1] != child.parking[i][j].point[0] and child.parking[i][j].point[1] == N:
									localCost = 4
								elif self.parking[x][y].point[1] > 0 and self.parking[x][y].point[1] < N and child.parking[i][j].point[1] == 0 and child.parking[i][j].point[0] != self.parking[i][j].point[0]:
									localCost = 2
								elif self.parking[x][y].point[1] > 0 and self.parking[x][y].point[1] < N and child.parking[i][j].point[1] == N and child.parking[i][j].point[0] != self.parking[i][j].point[0]:
									localCost = 1
								cost += localCost
		return cost

def calculateH(map1, map2):
	totalH = 0
	for x in range(M):
		for y in range(N):
			for i in range(M):
				for j in range(N):
					if map1.parking[x][y].cat == map2.parking[i][j].cat and map1.parking[x][y].order == map2.parking[i][j].order and map1.parking[x][y].cat != '_':
						if (x,y) != (i,j):
							totalH += getManhattanDistance(map1.parking[x][y], map2.parking[x][y])
	return totalH

def getManhattanDistance(pos1, pos2):
	return abs(pos1.point[0] - pos2.point[0]) + abs(pos1.point[1] - pos2.point[1])

def getChildrenStates(state, grid):
	childrenStates = []
	for j in range(M):
		for k in range(N):
			if state.parking[j][k].cat == '_': continue
			x,y = state.parking[j][k].point
			#print("Children:", x ,y)
			currentStateCopy = copy.deepcopy(state.parking[j][k])

			# car to be moved not located in an edge
			if y > 0 and y < N:
				# take a car and make it move to the right
				availableRightMovements = N-y
				for i in range(availableRightMovements):
					if grid.parking[x][y+i].cat != '_': break
					if i != 0:
						auxGrid1 = copy.deepcopy(grid)
						auxGrid1.parking[x][y].cat = '_'
						auxGrid1.parking[x][y].order = '_'
						auxGrid1.parking[x][y+i] = currentStateCopy
						childrenStates.append(auxGrid1)

				availableLeftMovements = y
				for i in range(availableLeftMovements+1):
					if grid.parking[x][y-i].cat != '_': break
					if i != 0:
						auxGrid2 = copy.deepcopy(grid)
						auxGrid2.parking[x][y].cat = '_'
						auxGrid2.parking[x][y].order = '_'
						auxGrid2.parking[x][y-i] = currentStateCopy
						childrenStates.append(auxGrid2)

			# car to be moved located in the left edge
			elif y == 0:
				availableRightMovements = N
				for i in range(availableRightMovements):
					if grid.parking[x][y+i].cat != '_': break
					if i != 0:
						auxGrid3 = copy.deepcopy(grid)
						auxGrid3.parking[x][y].cat = '_'
						auxGrid3.parking[x][y].order = '_'
						auxGrid3.parking[x][y+i] = currentStateCopy
						childrenStates.append(auxGrid3)

				b = M-x
				for i in range(b):
					if grid.parking[x+i][y].cat != '_': continue
					if i != 0:
						auxGrid4 = copy.deepcopy(grid)
						auxGrid4.parking[x][y].cat = '_'
						auxGrid4.parking[x][y].order = '_'
						auxGrid4.parking[x+i][y] = currentStateCopy
						#childrenStates.append(grid.parking[x+i][y])
						childrenStates.append(auxGrid4)

				c = x
				for i in range(c):
					if grid.parking[x-i][y].cat != '_': continue
					if i != 0:
						auxGrid5 = copy.deepcopy(grid)
						auxGrid5.parking[x][y].cat = '_'
						auxGrid5.parking[x][y].order = '_'
						auxGrid5.parking[x-i][y] = currentStateCopy
						#childrenStates.append(grid.parking[x-i][y])
						childrenStates.append(auxGrid5)

				d = M
				for i in range(d):
					if grid.parking[d-1][N-1].cat != '_': continue
					if i != 0:
						auxGrid6 = copy.deepcopy(grid)
						auxGrid6.parking[x][y].cat = '_'
						auxGrid6.parking[x][y].order = '_'
						auxGrid6.parking[d-1][N-1] = currentStateCopy
						#childrenStates.append(grid.parking[d-1][N-1])
						childrenStates.append(auxGrid6)

			# car located in the right edge
			else:
				a = N
				for i in range(a):
					if grid.parking[x][y-i].cat != '_': break
					if i != 0:
						auxGrid7 = copy.deepcopy(grid)
						auxGrid7.parking[x][y].cat = '_'
						auxGrid7.parking[x][y].order = '_'
						auxGrid7.parking[x][y-i] = currentStateCopy
						#childrenStates.append(grid.parking[x][y-i])
						childrenStates.append(auxGrid7)

				b = M-x
				for i in range(b):
					if grid.parking[x+i][y].cat != '_': continue
					if i != 0:
						auxGrid8 = copy.deepcopy(grid)
						auxGrid8.parking[x][y].cat = '_'
						auxGrid8.parking[x][y].order = '_'
						auxGrid8.parking[x+i][y] = currentStateCopy
						#childrenStates.append(grid.parking[x+i][y])
						childrenStates.append(auxGrid8)

				c = x
				for i in range(c):
					if grid.parking[x-i][y].cat != '_': continue
					if i != 0:
						auxGrid9 = copy.deepcopy(grid)
						auxGrid9.parking[x][y].cat = '_'
						auxGrid9.parking[x][y].order = '_'
						auxGrid9.parking[x-i][y] = currentStateCopy
						#childrenStates.append(grid.parking[x-i][y])
						childrenStates.append(auxGrid9)

				d = M
				for i in range(d):
					if grid.parking[d][0].cat != '_': continue
					if i != 0:
						auxGrid10 = copy.deepcopy(grid)
						auxGrid10.parking[x][y].cat = '_'
						auxGrid10.parking[x][y].order = '_'
						auxGrid10.parking[d][0] = currentStateCopy
						#childrenStates.append(grid.parking[d][0])
						childrenStates.append(auxGrid10)

	print('Children states generated from current state:')
	for a in childrenStates:
		for x in range(M):
			for y in range(N):
				sys.stdout.write(a.parking[x][y].cat)
				sys.stdout.write(a.parking[x][y].order)
			print()
		print()

	return [link for link in childrenStates]


def aStar(startState, goalState):
	closedSet = set()
	openSet = set()

	totalExpansions = 0
	totalCost = 0

	currentState = startState
	grid = currentState

	openSet.add(currentState)

	while openSet:
		print ("OPENSET: ", len(openSet))
		print ("CLOSEDSET: ", len(closedSet))
		currentState = min(openSet, key=lambda o:o.G + o.H)

		grid = currentState

		print("CURRENT STATE:")
		for x in range(M):
			for y in range(N):
				sys.stdout.write(currentState.parking[x][y].cat)
				sys.stdout.write(currentState.parking[x][y].order)
			print()
		print()

		if stateIsEqual(currentState, goalState):
			path = []
			while currentState.parent:
				path.append(currentState)
				currentState = currentState.parent
			path.append(currentState)
			generateInfoFile(len(path) - 2, totalCost, totalExpansions)
			return path[::-1]

		openSet.remove(currentState)
		closedSet.add(currentState)

		for state in getChildrenStates(currentState, grid):
			stateIncluded = False
			for i in closedSet:
				if stateIsEqual(state, i):
					stateIncluded = True

			#if state in closedSet:
			if stateIncluded == True:
				print("state already in closed")
				continue

			totalExpansions += 1

			#if state in openSet:
			stateIncluded = False
			for i in openSet:
				if stateIsEqual(state, i):
					stateIncluded = True

			if stateIncluded == True:
				new_g = currentState.G + currentState.move_cost(state)
				if state.G > new_g:
					state.G = new_g
					state.parent = currentState

			if stateIncluded == False:
				state.G = currentState.G + currentState.move_cost(state)
				state.H = calculateH(state, goalState)
				state.parent = currentState
				openSet.add(state)

			totalCost += currentState.move_cost(state)

	print ('NO PATH FOUND')

def stateIsEqual(state1, state2):
	for x in range(M):
		for y in range(N):
			if not (state1.parking[x][y].cat == state2.parking[x][y].cat and state1.parking[x][y].order == state2.parking[x][y].order):
				return False
	return True

def generateInfoFile(length, cost, expansions):
	executionTime = round(time.time() - start_time, 2)

	resultInfoFile = open("result.info", "w")
	resultInfoFile.write("Step length:", length)
	resultInfoFile.write("Running time (seconds):", executionTime)
	resultInfoFile.write("Total cost:", cost)
	resultInfoFile.write("Expansions:", expansions)
	resultInfoFile.close()


initialConfigurationFile = open(sys.argv[1], 'r')

dimensions = initialConfigurationFile.readline().split()

M = int(dimensions[0])
N = int(dimensions[1])

data = []

for line in initialConfigurationFile:
	lineValues = [str(i) for i in line.split()]
	data.append(lineValues)

initialConfigurationFile.close()
print(M, N)
print(data)
initialParking = []

for x in range(M):
	aux = []
	for y in range(N):
		aux.append(Car((x,y), data[x][y][0], data[x][y][1]))
	initialParking.append(aux)

finalConfigurationFile = open(sys.argv[2], 'r')

dimensions = finalConfigurationFile.readline().split()

if int(dimensions[0]) != M or int(dimensions[1]) != N:
	raise ValueError('Different dimensions between files')

data = []

for line in finalConfigurationFile:
	lineValues = [str(i) for i in line.split()]
	data.append(lineValues)

finalConfigurationFile.close()

finalParking = []

for x in range(M):
	aux = []
	for y in range(N):
		aux.append(Car((x,y), data[x][y][0], data[x][y][1]))
	finalParking.append(aux)

totalCost = 0

aStar(State(initialParking), State(finalParking))
