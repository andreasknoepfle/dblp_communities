community_converted=$5
community_unique=$4
community_out=$3
community_bin="Community_BGLL_CPP/"
community=$2
importer="importer/target/importer-with-deps.jar"
neo4j=$1
if [ ! -s $community/edges ];
then
  echo "No input data"
  exit 1
fi

if [ ! -d $community_out ];
then
	mkdir -p $community_out
fi
if [ ! -d $community_unique ];
then
	mkdir -p $community_unique
fi
if [ ! -d $community_converted ];
then
	mkdir -p $community_converted
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
#Import Communities
java -mx2048M -jar $importer $community_out $community_unique $neo4j
