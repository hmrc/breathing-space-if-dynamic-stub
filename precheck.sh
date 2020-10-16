#!/usr/bin/env bash

sbt clean scalastyle coverage it:test coverageReport
