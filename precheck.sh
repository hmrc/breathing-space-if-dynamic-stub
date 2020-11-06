#!/usr/bin/env bash

[[ $(sm -s) == *MONGO* ]] || docker start mongo36
sbt clean validate scalastyle coverage it:test coverageReport
