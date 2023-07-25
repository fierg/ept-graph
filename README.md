# ept-graph - Edge Periodic Graphs from Temporal Graphs
Collection of resources regarding the space efficient data structures edge periodic temporal graphs.

### Build
```shell
mvn clean install
```

### Run with f2f graph
```
Usage: EPT Graph Reader options_list
Arguments: 
    input -> input (Network id in range (0..61) { Int }
    threshold -> Min threshold of cover to be valid { Double }
Options: 
    --dotenv, -env [false] -> Use config from .env file (Recommended usage due to amount of args) 
    --state, -s [false] -> Invert state to substitute in decomposition (if set, decomposition will replace 0s instead of 1s) 
    --skipSingleStepEdges, -skipSingle [false] -> Skip single time step label edges 
    --debug, -d [false] -> Turn on debug mode 
    --quiet, -q [false] -> Turn on quiet mode 
    --deltaWindowPreprocessing [0] -> Delta window in preprocessing of the label { Int }
    --deltaWindowAlgo [0] -> Delta window during decomposing { Int }
    --help, -h -> Usage info 

```

### .env config file

All Configuration of options can be handled via the `.env` file in the projects root. In this case only the network id has to be provided as argument.


### Example
```shell
java -jar target/ept-graph-1.0-SNAPSHOT-jar-with-dependencies.jar -env 0
```
runs with configuration from `.env` file on network with id `0`


```shell
java -jar target/ept-graph-1.0-SNAPSHOT-jar-with-dependencies.jar 0 0.8 -s
```
runs on network with ID `0` and with substituting state set to `false`, with a threshold of 80% values covered for found periods.

