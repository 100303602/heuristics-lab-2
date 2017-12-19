#!/bin/bash
java -XX:-UseGCOverheadLimit -Xmx8000m -classpath .:jacop-4.3.0.jar SATParking $1
