#!/usr/bin/env bash

set -e

if [ "$1" = "" ]
then
  echo "Usage: $0 </path/to/KerML.mof>"
  exit
fi

KERML_UML_FILE=$1
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd "$SCRIPT_DIR/../../"
./gradlew :sysml-ref-generator:generateJsonSchemaKerML "-Pinput=$KERML_UML_FILE"

echo "Generated to $SCRIPT_DIR/build/gen/jsonschema-kerml"