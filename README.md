# launchableinc/testng

test selector for TestNG

## How to use

### Subset

```sh
# create subset list file
$ launchable subset --target 30% maven src/test/java > subset.txt

# set subset result file path to ENV
$ export LAUNCHABLE_SUBSET_FILE_PATH=subset.txt

# run tests
$ mvn test
```

### Record tests

After running `mvn test`, some test report files will be produced. But please use only one report that mentioned below for the record tests command.

```sh
$ tree target/surefire-reports/
target/surefire-reports/
├── Surefire\ suite
│   ├── Surefire\ test.xml
│   └── ...
├── TEST-TestSuite.xml ← Please use this file!
├── junitreports
│   ├── TEST-xxx.xml
│   └── ...
├── testng-results.xml
└── ...
```

e.g)
```sh

$ launchable record tests maven target/surefire-reports/TEST-TestSuite.xml
```

## Author

Launchable, Inc.

