#!/usr/bin/env bash

# TODO: Add environment checks (i.e java path)
KRAFT_CLI_DIR=$(cd `dirname $0`/../..; pwd -P)

kraft() {
    java -client \
        -Xmx1g \
        -Xms128m \
        -classpath $(echo ${KRAFT_CLI_DIR}/target/{classes,lib/\*} | tr \  :) \
        "net.tvidal.kraft.ApplicationKt" \
        $*
}
