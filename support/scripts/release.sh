export GIT_SSL_NO_VERIFY=NO
git pull origin
MVN_OPTS="-Xmx1g" mvn -Dmaven.test.skip=true -DskipTests=true -Darguments="-DskipTests"  -P production release:prepare
