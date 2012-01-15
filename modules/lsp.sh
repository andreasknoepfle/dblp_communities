neo4j=$1
lsp="modules/lsp/target/lsp-with-deps.jar"
echo "LSP on $neo4j"
java -XX:+UseConcMarkSweepGC -mx2048M -jar $lsp $neo4j 0
