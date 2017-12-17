import sys
import string

class Car:

	def __init__(self, point, cat, order):
		self.point = point
        	self.cat = cat
        	self.order = order

class Node:
	def __init__(self, point, cat, order):
		self.cat = cat
		self.order = order

		self.point = point
		self.parent = None
		self.H = 0
		self.G = 0

	def move_cost(self, final):
		cost = 0
		if self.point[0] == final.point[0]:
			if self.point[1] < final.point[1]:
				cost = 1
			else:
				cost = 2
		elif final.point[1] == 0:
				cost = 3
		elif final.point[1] == (N-1):
				cost = 4
			
		return cost

def manhattan(pos1, pos2):
	return abs(pos1.point[0] - pos2.point[0]) + abs(pos1.point[1] - pos2.point[1])

def children(car, grid):
	x,y = car.point
	links = [grid[d[0]][d[1]] for d in [(x-1, y),(x,y - 1),(x,y + 1),(x+1,y)]]
	return [link for link in links if link.value != '__']

def aStar(start, goal, grid):
	closedSet = set();
	openSet = set();
	
	current = start

	openSet.add(current)

	while openset:
		current = min(openset, key=lambda o:o.G + o.H)

		if current == goal:
			path = []
			while current.parent:
				path.append(current)
				current = current.parent
			path.append(current)
			return path[::-1]
		openSet.remove(current)
		closedSet.remove(current)

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
	path = aStar(grid[initial.point[0]][initial.point[1]], grid[final.point[0]][final.point[1]])

	print len(path) - 1
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

parkingI = []

for x in xrange(M-1):
	for y in xrange(N-1):
		parkingI[M][N] = Car((x,y), data[x][y][0], data[x][y][1])

finalConfiguration = open(sys.argv[2], 'r')

dimensions = finalConfiguration.readline().split()

if dimensions[0] != M or dimensions[1] != N:
	raise ValueError('Different dimensions between files')

data = []

for line in finalConfiguration:
	lineValues = [string(i) for i in line.split()]
	data.append(lineValues)

finalConfiguration.close()

parkingdF = []


for x in xrange(M-1):
	for y in xrange(N-1):
		parkingF[M][N] = Car((x,y), data[x][y][0], data[x][y][1])




