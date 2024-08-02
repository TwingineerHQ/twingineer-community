#!/usr/bin/env bash

set -e

if [ "$2" = "" ]
then
  echo "Usage: $0 </path/to/SysML.mof> </path/to/SysML-v2-API-Services>"
  exit
fi

SYSML_UML_FILE=$1
API_ROOT_DIR=$2
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd "$SCRIPT_DIR/../../"
./gradlew :sysml-ref-generator:generateInterfaces "-Pinput=$SYSML_UML_FILE"
./gradlew :sysml-ref-generator:generateImpl "-Pinput=$SYSML_UML_FILE"
./gradlew :sysml-ref-generator:generateJsonSchema "-Pinput=$SYSML_UML_FILE"
./gradlew :sysml-ref-generator:generateJsonLd "-Pinput=$SYSML_UML_FILE"

cd "$SCRIPT_DIR"
rsync -av build/gen/interfaces/ "$API_ROOT_DIR/app/org/omg/sysml/metamodel/"
rsync -av build/gen/impl/ "$API_ROOT_DIR/app/org/omg/sysml/metamodel/impl/"
rsync -av build/gen/jsonld/ "$API_ROOT_DIR/public/jsonld/metamodel/"
rsync -av build/gen/jsonschema/ "$API_ROOT_DIR/conf/json/schema/metamodel/"