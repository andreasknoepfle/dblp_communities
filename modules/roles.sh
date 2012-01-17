neo4j=$1
roles="modules/roles/target/roles-with-deps.jar"
echo "Roles on $neo4j"
java -XX:+UseConcMarkSweepGC -mx2048M -jar $roles $neo4j 
