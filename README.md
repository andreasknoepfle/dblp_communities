

Dependencies:
------------
Before Running the installer please install the following dependencies.

*  java
*  maven2
*  unzip
*  g++

Installation:
------------
sh install.sh

Parsing and Importing Data into Neo4J
-------------------------------------
Option 1) Split data with a year splitter (default 10 years)

sh import-split.sh

Option 2) Don't Split Data 
sh import-all.sh 

You can have both options because they will import data to 
dblp/split 
or 
dblp/all

Modules:
--------
Invoking calculations like count, participationcoefficient, withinmoduledegree ...

sh modules.sh split
or
sh modules.sh all

After this steps the neo4j Database can be found in dblp-out/split/neo4j or dblp-out/all/neo4j


