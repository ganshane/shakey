VERSION=`cat pom.xml|grep -e '<version>\(.*\)</version>'|head -n 1|sed  's/^.*<version>\(.*\)<\/version>/\1/g'`
echo $VERSION
echo makensis -DSHAKEY_VERSION=$VERSION support/nsis/app.nsi
makensis -DSHAKEY_VERSION=$VERSION support/nsis/app.nsi
echo makensis -DSHAKEY_VERSION=$VERSION support/nsis/install.nsi
makensis -DSHAKEY_VERSION=$VERSION support/nsis/install.nsi
