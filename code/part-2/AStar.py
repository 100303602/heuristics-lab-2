import sys

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
		cost = 0
		for x in xrange(M):
			for y in xrange(N):
				for i in xrange(M):
					for j in xrange(N):
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

def manhattan(pos1, pos2):
	return abs(pos1.point[0] - pos2.point[0]) + abs(pos1.point[1] - pos2.point[1])

def children(car, grid):
	for i in xrange(M+1):
		
	x,y = car.point
	print "Children:", x ,y

	if y > 0 and y < N:
		a = N-y
		for i in xrange(a+1):
			links.append(grid[x][y+i])

		b = y
		for i in xrange(b+1):
			links.append(grid[x][y-i])

	if y == 0:
		a = N
		for i in xrange(a+1):
			links.append(grid[x][y+i])
	
		b = M-x
		for i in xrange(b+1):
			links.append(grid[x+i][y])

		c = x
		for i in xrange(c+1):
			links.append(grid[x-i][y])

		d = M
		for i in xrange(d):
			links.append(grid[d][N-1])
		
	if y == N:
		a = N
		for i in xrange(a+1):
			links.append(grid[x][y-i])
	
		b = M-x
		for i in xrange(b+1):
			links.append(grid[x+i][y])

		c = x
		for i in xrange(c+1):
			links.append(grid[x-i][y])

		d = M
		for i in xrange(d):
			links.append(grid[d][0])

	print "Links:", links
	return [link for link in links if link.cat == '_']
	#return [link for link in links]

def aStar(start, goal, grid):
	closedSet = set()
	openSet = set()
	
	current = start

	openSet.add(current)

	while openSet:
		current = min(openSet, key=lambda o:o.G + o.H)

		if current == goal:
			path = []
			while current.parent:
				path.append(current)
				current = current.parent
			path.append(current)
			return path[::-1]

		openSet.remove(current)

		closedSet.add(current)

		for node in children(current, grid):
			if node in closedSet:
				continue

			if node in openSet:
				new_g = current.G + current.move_cost(node)
				if node.G > new_g:
					node.G = new_g
					node.parent = current

			else:
				node.G = current.G + current.move_cost(node)
				node.H = manhattan(node, goal)

				node.parent = current

				openSet.add(node)

	raise ValueError('No Path Found')

def next_move(initial, final, grid):
	for x in xrange(len(grid)):
		for y in xrange(len(grid[x])):
			grid[x][y] = Node(parkingI[x][y].point, parkingI[x][y].cat, parkingI[x][y].order)

	print "Starting A* in ", initial.point, " ", final.point
	path = aStar(grid[initial.point[0]][initial.point[1]], grid[final.point[0]][final.point[1]], grid)

	print "Path length:", len(path) - 1
	print "Path:"
	for node in path:
		x, y = node.point
		print x, y

initialConfiguration = open(sys.argv[1], 'r')

dimensions = initialConfiguration.readline().split()

M = int(dimensions[0])
N = int(dimensions[1])

data = []

for line in initialConfiguration:
	lineValues = [str(i) for i in line.split()]
	data.append(lineValues)

initialConfiguration.close()
print M, N
print data
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
print data
parkingF = []


grid = parkingI

for x in range(M):
	aux = []
	for y in range(N):
		aux.append(Car((x,y), data[x][y][0], data[x][y][1]))
	parkingF.append(aux)

for x in range(M):
	for y in range(N):
		for i in range(M):
			for j in range(N):
				if parkingI[x][y].cat == parkingF[i][j].cat and parkingI[x][y].order == parkingF[i][j].order and parkingI[x][y].cat != '_' and parkingF[i][j].cat != '_':
					if (x,y) != (i,j):
						next_move(parkingI[x][y], parkingF[i][j], grid)

