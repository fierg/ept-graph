#!/bin/bash

mvn install:install-file \
   -Dfile='/opt/gurobi/gurobi951/linux64/lib/gurobi.jar' \
   -DgroupId='gurobi'\
   -DartifactId='solver' \
   -Dversion='9.5.1' \
   -Dpackaging='jar' \
   -DgeneratePom=true