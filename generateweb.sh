out="dblp-out"
neo4j="$out/$1/neo4j"
templatedir="analyze/web/templates"
cssdir="analyze/web/css"
htmlout="$out/$1/web/"
generateweb="analyze/generateweb/target/generateweb-with-deps.jar"
staticdir="analyze/web/static"
if [ ! -d $htmlout ];
then 
mkdir $htmlout 
fi
if [ -d $neo4j ];
then
cp -r $cssdir $htmlout
cp -r $staticdir $htmlout
java -XX:+UseConcMarkSweepGC -mx2048M -jar $generateweb $neo4j $templatedir $htmlout
fi
