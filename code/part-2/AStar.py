import sys
import copy

class Car:
	def __init__(self, point, cat, order):
		self.point = point
		self.cat = cat
		self.order = order

class Node:
	def __init__(self, parking):
		self.parking = parking
		self.parent = None
		self.H = 0
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
							totalH += manhattan(map1.parking[x][y], map2.parking[x][y])
	return totalH

def manhattan(pos1, pos2):
	return abs(pos1.point[0] - pos2.point[0]) + abs(pos1.point[1] - pos2.point[1])

def children(node, grid):
	links = []
	for j in range(M):
		for k in range(N):
			if node.parking[j][k].cat == '_': continue
			x,y = node.parking[j][k].point
			#print("Children:", x ,y)
			currentNodeCopy = copy.deepcopy(node.parking[j][k])

			# car to be moved not located in an edge
			if y > 0 and y < N:
				blocksToMoveRight = N-y
				for i in range(blocksToMoveRight):
					if i == 0: continue
					if grid.parking[x][y+i].cat != '_': break
					auxGrid1 = copy.deepcopy(grid)
					auxGrid1.parking[x][y].cat = '_'
					auxGrid1.parking[x][y].order = '_'
					auxGrid1.parking[x][y+i] = currentNodeCopy
					links.append(auxGrid1)

				blocksToMoveLeft = y
				for i in range(blocksToMoveLeft+1):
					if i == 0: continue
					if grid.parking[x][y-i].cat != '_': break
					auxGrid2 = copy.deepcopy(grid)
					auxGrid2.parking[x][y].cat = '_'
					auxGrid2.parking[x][y].order = '_'
					auxGrid2.parking[x][y-i] = currentNodeCopy
					links.append(auxGrid2)

			# car to be moved located in the left edge
			elif y == 0:
				blocksToMoveRight = N
				for i in range(blocksToMoveRight):
					if i == 0: continue
					if grid.parking[x][y+i].cat != '_': break
					auxGrid3 = copy.deepcopy(grid)
					auxGrid3.parking[x][y].cat = '_'
					auxGrid3.parking[x][y].order = '_'
					auxGrid3.parking[x][y+i] = currentNodeCopy
					links.append(auxGrid3)

				b = M-x
				for i in range(b):
					if i == 0: continue
					if grid.parking[x+i][y].cat != '_': continue
					auxGrid4 = copy.deepcopy(grid)
					auxGrid4.parking[x][y].cat = '_'
					auxGrid4.parking[x][y].order = '_'
					auxGrid4.parking[x+i][y] = currentNodeCopy
					#links.append(grid.parking[x+i][y])
					links.append(auxGrid4)

				c = x
				for i in range(c):
					if i == 0: continue
					if grid.parking[x-i][y].cat != '_': continue
					auxGrid5 = copy.deepcopy(grid)
					auxGrid5.parking[x][y].cat = '_'
					auxGrid5.parking[x][y].order = '_'
					auxGrid5.parking[x-i][y] = currentNodeCopy
					#links.append(grid.parking[x-i][y])
					links.append(auxGrid5)

				d = M
				for i in range(d):
					if i == 0: continue
					if grid.parking[d-1][N-1].cat != '_': continue
					auxGrid6 = copy.deepcopy(grid)
					auxGrid6.parking[x][y].cat = '_'
					auxGrid6.parking[x][y].order = '_'
					auxGrid6.parking[d-1][N-1] = currentNodeCopy
					#links.append(grid.parking[d-1][N-1])
					links.append(auxGrid6)

			# car located in the right edge
			else:
				a = N
				for i in range(a):
					if i == 0: continue
					if grid.parking[x][y-i].cat != '_': break
					auxGrid7 = copy.deepcopy(grid)
					auxGrid7.parking[x][y].cat = '_'
					auxGrid7.parking[x][y].order = '_'
					auxGrid7.parking[x][y-i] = currentNodeCopy
					#links.append(grid.parking[x][y-i])
					links.append(auxGrid7)

				b = M-x
				for i in range(b):
					if i == 0: continue
					if grid.parking[x+i][y].cat != '_': continue
					auxGrid8 = copy.deepcopy(grid)
					auxGrid8.parking[x][y].cat = '_'
					auxGrid8.parking[x][y].order = '_'
					auxGrid8.parking[x+i][y] = currentNodeCopy
					#links.append(grid.parking[x+i][y])
					links.append(auxGrid8)

				c = x
				for i in range(c):
					if i == 0: continue
					if grid.parking[x-i][y].cat != '_': continue
					auxGrid9 = copy.deepcopy(grid)
					auxGrid9.parking[x][y].cat = '_'
					auxGrid9.parking[x][y].order = '_'
					auxGrid9.parking[x-i][y] = currentNodeCopy
					#links.append(grid.parking[x-i][y])
					links.append(auxGrid9)

				d = M
				for i in range(d):
					if i == 0: continue
					if grid.parking[d][0].cat != '_': continue
					auxGrid10 = copy.deepcopy(grid)
					auxGrid10.parking[x][y].cat = '_'
					auxGrid10.parking[x][y].order = '_'
					auxGrid10.parking[d][0] = currentNodeCopy
					#links.append(grid.parking[d][0])
					links.append(auxGrid10)

	#return [link for link in links if link.cat == '_']
	#print([col.cat for col in link.parking for link in links]
	print('Children states generated from current state:')
	for a in links:
		for x in range(M):
			for y in range(N):
				sys.stdout.write(a.parking[x][y].cat)
				sys.stdout.write(a.parking[x][y].order)
			print()
		print()
	return [link for link in links]


def aStar(start, goal):
	closedSet = set()
	openSet = set()

	current = start
	grid = current

	openSet.add(current)

	while openSet:
		print ("OPENSET: ", len(openSet))
		print ("CLOSEDSET: ", len(closedSet))
		current = min(openSet, key=lambda o:o.G + o.H)

		grid = current

		print("CURRENT STATE:")
		for x in range(M):
			for y in range(N):
				sys.stdout.write(current.parking[x][y].cat)
				sys.stdout.write(current.parking[x][y].order)
			print()


		if nodeIsEqual(current, goal):
			path = []
			while current.parent:
				path.append(current)
				current = current.parent
			path.append(current)
			return path[::-1]

		openSet.remove(current)

		closedSet.add(current)

		for node in children(current, grid):
			nodeIncluded = False
			for i in closedSet:
				if nodeIsEqual(node, i):
					nodeIncluded = True
			#if node in closedSet:
			#	continue
			if nodeIncluded == True:
				print("node already in closed")
				continue

			#if node in openSet:
			nodeIncluded = False
			for i in openSet:
				if nodeIsEqual(node, i):
					nodeIncluded = True

			if nodeIncluded == True:
				new_g = current.G + current.move_cost(node)
				if node.G > new_g:
					node.G = new_g
					node.parent = current

			if nodeIncluded == False:
				node.G = current.G + current.move_cost(node)
				node.H = calculateH(node, goal)
				node.parent = current
				openSet.add(node)

	raise ValueError('No Path Found')

def nodeIsEqual(node1, node2):
	for x in range(M):
		for y in range(N):
			if node1.parking[x][y].cat == node2.parking[x][y].cat and node1.parking[x][y].order == node2.parking[x][y].order:
				continue
			else:
				return False
	return True

def next_move(initial, final):
	#grid = initial
	print("Starting A*")
	path = aStar(initial, final)

	print("Path length:", len(path) - 1)
	print("Path:")

	for node in path:
		for x in range(M):
			for y in range(N):
				sys.stdout.write(node.parking[x][y].cat)
				sys.stdout.write(node.parking[x][y].order)
			print()
		print()

initialConfiguration = open(sys.argv[1], 'r')

dimensions = initialConfiguration.readline().split()

M = int(dimensions[0])
N = int(dimensions[1])

data = []

for line in initialConfiguration:
	lineValues = [str(i) for i in line.split()]
	data.append(lineValues)

initialConfiguration.close()
print(M, N)
print(data)
parkingI = []

for x in range(M):
	aux = []
	for y in range(N):
		aux.append(Car((x,y), data[x][y][0], data[x][y][1]))
	parkingI.append(aux)

finalConfiguration = open(sys.argv[2], 'r')

dimensions = finalConfiguration.readline().split()

if int(dimensions[0]) != M or int(dimensions[1]) != N:
	raise ValueError('Different dimensions between files')

data = []

for line in finalConfiguration:
	lineValues = [str(i) for i in line.split()]
	data.append(lineValues)

finalConfiguration.close()
print(data)
parkingF = []


grid = copy.deepcopy(parkingI)

for x in range(M):
	aux = []
	for y in range(N):
		aux.append(Car((x,y), data[x][y][0], data[x][y][1]))
	parkingF.append(aux)

next_move(Node(parkingI), Node(parkingF))
