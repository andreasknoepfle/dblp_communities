filename="Community_BGLL_CPP"
zip="$filename.zip"
url="https://sites.google.com/site/findcommunities/"
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
 
