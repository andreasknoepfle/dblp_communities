community="dblp-out/community-input"
neo4j="dblp-out/neo4j"
dblp="dblp-out/dblp.xml"
community_bin="Community_BGLL_CPP/"


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

if [ ! -s $community/edges ];
then
  echo "Parse failed"
fi

