neo4j=$1
conductance="modules/conductance/target/conductance-with-deps.jar"
echo "Conductance on $neo4j" 
java -XX:+UseConcMarkSweepGC -mx2048M -jar $conductance $neo4j

