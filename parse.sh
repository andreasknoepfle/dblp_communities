if [ $# -eq 3 ] 
  then
dblp=$3
else
dblp="dblp/dblp.xml"
fi
if [ $# -ge 2 ]
  then 
  splitter=$1
else
   echo "Usage sh parser.sh [splitter] [output-folder] [[dblp-file]]"
   echo "splitter:"
   echo "0 = No Splitter"
   echo "X = Split in equal parts of X years (last one may be less than splitter)"
   exit 1
fi
if [ -f $dblp ]
  then
     java -mx1024M -DentityExpansionLimit=2500000 -jar parser/target/parser-1.0.jar $dblp $2 $splitter
else
     echo "No dblp.xml found ... Run Installer"
     exit 1
fi
