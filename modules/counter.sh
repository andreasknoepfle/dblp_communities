neo4j=$1
counter="modules/counter/target/counter-with-deps.jar"
echo "Count on $neo4j" 
java -mx2048M -jar $counter $neo4j 0

