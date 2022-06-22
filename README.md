# launchableinc/testng

test selector for TestNG

## How to use

```
# create subset list file
$ launchable subset --target 30% maven src/test/java > subset.txt

# set subset result file path to ENV
$ export LAUNCHABLE_SUBSET_FILE=subset.txt

# run tests
$ mvn test
```

## Author

Launchable, Inc.

