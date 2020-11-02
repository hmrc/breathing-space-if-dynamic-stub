#!/usr/bin/env bash

sbt clean validate scalastyle coverage it:test coverageReport
