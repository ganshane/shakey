echo makensis -DSHAKEY_VERSION=$VERSION support/nsis/app.nsi
makensis -DSHAKEY_VERSION=$VERSION support/nsis/app.nsi
echo makensis -DSHAKEY_VERSION=$VERSION support/nsis/install.nsi
makensis -DSHAKEY_VERSION=$VERSION support/nsis/install.nsi
