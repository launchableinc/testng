# launchableinc/testng

test selector for TestNG

## How to use

### Using subset result

```
# create subset list file
$ launchable subset --target 30% maven src/test/java > subset.txt

# set subset result file path to ENV
$ export LAUNCHABLE_SUBSET_FILE_PATH=subset.txt

# run tests
$ mvn test
```

### Using rest result

```
# create rest list file
$ launchable subset --target 30% --rest rest.txt maven src/test/java > subset.txt

# set rest result file path to ENV
$ export LAUNCHABLE_REST_FILE_PATH=rest.txt

# run tests
$ mvn test
```

## Author

Launchable, Inc.

