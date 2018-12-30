#!/usr/bin/env bash
[ -n "$DEBUG" ] && set -x

BASE_DIR=$(cd `dirname $0`; pwd -P)
JAR_FILE="$BASE_DIR/$(basename $0)"

if [ -n "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ]
then
    JAVA="$JAVA_HOME/bin/java"

elif type -p java &> /dev/null
then
    JAVA=$(type -p java)

elif [ -x /usr/bin/java ]
then
    JAVA=/usr/bin/java

else
    >&2 echo "Unable to find Java"
    exit 1
fi

ARGS=(-Dsun.misc.URLClassPath.disableJarChecking=true $JAVA_OPTS -jar "$JAR_FILE" "$@")

"$JAVA" "${ARGS[@]}"
exit $?
