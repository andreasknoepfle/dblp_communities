community="dblp/community-input"
neo4j="dblp/neo4j"
dblp="dblp/dblp.xml"
if [ $# -eq 1 ];
then
  dblp=$1
fi
if [ ! -d $community ];
then
	mkdir $community
fi
if [ ! -d $neo4j ];
then
	mkdir $neo4j
fi
sh parse.sh $dblp $community 10 $neo4j
