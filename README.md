# ept-graph
Collection of resources regarding the space efficient data structures edge periodic temporal graphs.


### Build
```shell
mvn clean install
```

### Run with f2f graph

```shell
Usage: EPT Graph Reader options_list
Arguments: 
    input -> input (Network id in range (0..61) { Int }
Options: 
    --state, -s [false] -> State to substitute in decomposition 
    --coroutines, -co [false] -> Use Coroutines for period computation. (Use with check) 
    --clean, -cl [false] -> Clean up periods of multiples 
    --mode, -m -> Mode of composing the periods [ALL,SIMPLE,GREEDY] { Value should be one of [greedy, simple, all] }
    --debug, -d [false] -> Turn on debug mode 
    --quiet, -q [false] -> Turn on quiet mode 
    --help, -h -> Usage info 

```
##### Example
```shell
java -jar target/ept-graph-1.0-SNAPSHOT-jar-with-dependencies.jar -s 0
```
runs on network with ID `0` and with substituting state set to `false`.