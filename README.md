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
    --state, -s [false] -> State to substitute in decomposition (true,false) 
    --debug, -d [false] -> Turn on debug mode 
    --quiet, -q [false] -> Turn on quiet mode 
    --help, -h -> Usage info 
```
##### Example
```shell
java -jar target/ept-graph-1.0-SNAPSHOT-jar-with-dependencies.jar -s 0
```
runs on network with ID `0` and with substituting state set to `false`.