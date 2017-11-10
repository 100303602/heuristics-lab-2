#!/bin/bash

# generate report (compile twice to generate cross-references)
cd report
pdflatex main.tex
pdflatex main.tex
cd ..

# create directories
mkdir -p deliverable/303602-346060/code/part-1
mkdir -p deliverable/303602-346060/code/part-2

# move files
cp report/main.pdf deliverable/303602-346060/report.pdf

# create zip file
cd deliverable
zip -r p2-303602-346060.zip 303602-346060/*
