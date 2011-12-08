if [ ! $# -eq 4 ]
then
   echo "Usage sh parser.sh [input] [output-folder] [splitter] [dbpath]"
   echo "splitter:"
   echo "0 = No Splitter"
   echo "X = Split in equal parts of X years (last one may be less than splitter)"
   exit 1
fi
if [ -f $1 ]
  then
     java -mx2048M -DentityExpansionLimit=2500000 -jar parser/target/parser-with-deps.jar $1 $2 $3 $4
else
     echo "No dblp.xml found ... Run Installer"
     exit 1
fi
