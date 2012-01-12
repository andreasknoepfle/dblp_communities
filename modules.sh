#Neo4j
neo4j="dblp-out/neo4j"
#Modules
counter="modules/counter/target/counter-with-deps.jar"
shortestPath="modules/shortestPath/target/shortestPath-with-deps.jar"

#Start Modules
echo "Counter" 
#java -mx2048M -jar $counter $neo4j 0
echo "ShortestPath"
java -mx2048M -jar $shortestPath $neo4j 1040444
