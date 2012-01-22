out="dblp-out"
label="test"
community="$out/$label/community-input"
community_out="$out/$label/community-out"
community_unique="$out/$label/community-unique"
community_converted="$out/$label/community-converted"
neo4j="$out/$label/neo4j"
dblp="dev/partial_dblp.xml"
splitter=0

if [ $# -eq 1 ];
then
  splitter=$1
fi
echo "Splittig with $splitter."
if [ ! -d $community ];
then
	mkdir -p $community
fi

if [ ! -d $neo4j ];
then
	mkdir -p $neo4j
fi
sh parse.sh $dblp $community $splitter $neo4j

if [ ! -s $community/edges ];
then
  echo "Parse failed"
fi

sh convert.sh $neo4j $community $community_out $community_unique $community_converted


