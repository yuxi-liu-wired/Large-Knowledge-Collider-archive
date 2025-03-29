#!/bin/bash

# checks whether Maven is available
echo Checking for Maven...
MVN=`which mvn`
if [ ! -n "$MVN" ]; then
        echo Maven is not set up correctly. Terminating...
        exit
fi
echo Maven found at $MVN. Building and packaging the plug-in...
$MVN assembly:assembly -Dmaven.test.skip=true
mv target/*.jar .

for x in *.jar ; do 
    len=${#x}
    mv $x ${x:0:len-4}.larkc
done

rm -r target
echo Exiting...