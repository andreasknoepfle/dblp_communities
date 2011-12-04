
# Vars
filename="Community_BGLL_CPP"
zip="$filename.zip"
url="https://sites.google.com/site/findcommunities/"
dblp_server="http://dblp.uni-trier.de/xml/"
dblp_xml="dblp.xml"
dblp_dtd="dblp.dtd"
dblp_dir="./dblp"
#Skript

echo "Getting Remote Resources"
if [ ! -f $zip ]; then
echo "Downloading Community Detection Algorithm from $url"
wget $url$zip
fi
if [ -f $i ]; then 
    unzip -o $zip
    rm $zip 
else
    echo "Could not download $url$zip ... Download it manually and put it here."
    exit -1
fi
cd $filename
make
echo "Downloading DBLP"
cd ..
echo "Creating DBLP Directory"
mkdir -p $dblp_dir

cd dblp
if [ ! -f $dblp_xml ]; then
wget $dblp_server$dblp_xml
fi
if [ ! -f $dblp_dtd ]; then
wget $dblp_server$dblp_dtd
fi
cd ..

