#include <fstream>

struct Car {
  int posX;
  int posY;
  char cat;
  int order;
};

struct Node {
  Car[][] parking;
  Node parent;
  int H;
  int G;
};

int manhattan(Car c1, Car c2) {
  return abs(c1.posX-c2.posX) + abs(c1.posY-c2.posY);
}

int calculateH(Node map1, Node map2) {
  int totalH = 0;
  for (int x = 0; x < M; x++) {
    for (int y = 0; y < N; y++) {
      for (int i = 0; i < M; i++) {
        for (int j = 0; j < N; j++) {
          if (map1.parking[x][y].cat == map2.parking[i][j].cat && map1.parking[x][y].order == map2.parking[i][j].order && map1.parking[x][y].cat != '_') {
            if (x,y) != (i,j) {
              totalH += manhattan(map1.parking[x][y], map2.parking[x][y]);
            }
          }
        }
      }
    }
  }
  return totalH;
}

std::vector<Node> expandNode(Node node, Node grid) {

  return 0;
}

std::vector<Node> aStar(Node initial, Node goal) {
  std::vector<Node> closedSet;
  std::vector<Node> openSet;

  Node current = initial

  openSet.pushback(current);

  while (openSet.size > 0) {
    current = min()

    if (current == goal) {
      std::vector<Node> path;
      while current.parent {
        path.pushback(current)
        current = current.parent
      }
    }
  }
  return 0;
}

int main(int argc, char const *argv[]) {
  int M;
  int N;

  ifstream initialFile(argv[1]);

  string initialConf;

  if (initialFile.is_open())
  {
    while ( getline (initialFile,line) )
    {
      initialFile >> initialData;
      initialConf.pushback(initialData);
    }
    initialFile.close();
  }

  ifstream finalFile(argv[2]);

  string finalFile;

  if (finalFile.is_open())
  {
    while ( getline (finalFile,line) )
    {
      finalFile >> finalData;
      finalConf.pushback(finalData);
    }
    finalFile.close();
  }

  M = stoi(initialConf[0])
  N = stoi(initialConf[1])

  initialConf.erase(initialConf.begin())
  initialConf.erase(initialConf.begin())

  finalConf.erase(finalConf.begin())
  finalConf.erase(finalConf.begin())

  initialConf.pop_back();
  finalConf.pop_back();

  return 0;
}
