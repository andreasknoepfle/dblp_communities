out="dblp-out"
neo4j="$out/$1/neo4j"
#All Operations
if [ $1 = "all" ];
then
  sh modules/counter.sh $neo4j
 # sh modules/lsp.sh $neo4j
  sh modules/roles.sh $neo4j
  sh modules/conductance.sh $neo4j
fi
if [ $1 = "test" ]
then
  sh modules/counter.sh $neo4j
  sh modules/roles.sh $neo4j
  sh modules/conductance.sh $neo4j
fi
#Split Operations
if [ $1 = "split" ];
then
  sh modules/counter.sh $neo4j
fi
