#!/bin/sh

set -e

mvn assembly:assembly

java -jar target/photocollage-1.3.5-runnable.jar "$@"