
# Vars
filename="Community_BGLL_CPP"
zip="$filename.zip"
url="https://sites.google.com/site/findcommunities/"
dblp_server="http://dblp.uni-trier.de/xml/"
dblp_xml="dblp.xml"
dblp_dtd="dblp.dtd"
dblp_dir="./dblp-out"
#Skript

echo "Getting Remote Resources"
if [ ! -d $filename ]; then
  if [ ! -f $zip ]; then
   echo "Downloading Community Detection Algorithm from $url"
   wget --no-check-certificate --secure-protocol=auto $url$zip
  fi
  if [ -f $zip ]; then 
    unzip -o $zip
    rm $zip 
  else
    echo "Could not download $url$zip ... Download it manually and put it here."
    exit -1
  fi
fi
cd $filename
make
echo "Downloading DBLP"
cd ..
echo "Creating DBLP Directory"
mkdir -p $dblp_dir

cd $dblp_dir
if [ ! -f $dblp_xml ]; then
wget $dblp_server$dblp_xml
fi
if [ ! -f $dblp_dtd ]; then
wget $dblp_server$dblp_dtd
fi
cd ..

#Interface

cd db_interface
mvn clean
mvn install
cd ..

# Parser 

cd parser 
mvn clean
mvn package
cd ..

#Importer

cd importer 
mvn clean
mvn package
cd ..

# Modules

cd modules
  # Counter
  cd counter 
    mvn clean
    mvn package
  cd ..
  #Shortest Path
  cd shortestPath 
    mvn clean
    mvn package
  cd ..
cd ..
