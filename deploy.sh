#!/bin/bash

JAR_NAME="spring0.jar"
SRC_DIR="src"
BUILD_DIR="build"
LIB_DIR="lib"

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

find $SRC_DIR -name "*.java" > sources.txt

javac -cp "$LIB_DIR/servlet-api.jar" \
      -d $BUILD_DIR \
      @sources.txt

rm sources.txt

jar -cvf $JAR_NAME -C $BUILD_DIR .

echo ""
echo "JAR généré : $JAR_NAME"
echo ""