#!/bin/sh

cd "$(dirname "$0")/.."
jarFile="target/scala-2.12/breathing-space-if-dynamic-stub.jar"
[ ! -e $jarFile ] && sbt assembly
java -cp $jarFile uk.gov.hmrc.breathingspaceifstub.schema.GenIndividualDetailsSchema
