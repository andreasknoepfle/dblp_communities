community_converted="dblp-out/community-converted"
community_unique="dblp-out/community-unique"
community_out="dblp-out/community-out"
community_bin="Community_BGLL_CPP/"
community="dblp-out/community-input"
importer="importer/target/importer-with-deps.jar"
neo4j="dblp-out/neo4j"
if [ ! -s $community/edges ];
then
  echo "No input data"
  exit 1
fi

if [ ! -d $community_out ];
then
	mkdir $community_out
fi
if [ ! -d $community_unique ];
then
	mkdir $community_unique
fi
if [ ! -d $community_converted ];
then
	mkdir $community_converted
fi

for file in $community/* ; do
  b=$(basename $file)
  if [ ! $b = "edges" ];
  then 
	cut -d' ' -f1,2 $file |tr -s ' ' '\n' | sort -g -u > $community_unique/$b.unique
  $community_bin/convert -i $file -o $community_converted/$b.bin -w
  $community_bin/community $community_converted/$b.bin -l -1 -w > $community_out/$b.tree
  fi
done
java -mx2048 -jar $importer $community_out $community_unique $neo4j
