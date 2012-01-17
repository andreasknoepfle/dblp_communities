neo4j=$1
counter="modules/max/target/max-with-deps.jar"
echo "Max on $neo4j" 
java -XX:+UseConcMarkSweepGC -mx2048M -jar $counter $neo4j 0

