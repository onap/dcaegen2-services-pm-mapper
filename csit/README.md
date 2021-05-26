## Continuous System and Integration Testing (CSIT) for DCAEGEN2 PM-Mapper

The directory structure:

- **plans/** contains testing plans, each sub-folder represents a separate test plan with contents processed subsequently:
    _startup.sh_ (serves docker containers startup), _testplan.txt_ (lists test suites), _teardown.sh_ (serves docker containers stopping and images removal)
- **scripts/** contains shell scripts used on tests executions
- **tests/** contains test suites that are processed by folder name (relative to _tests_ folder) taken from _testplan.txt_

Test suites are executed using Robot framework.

### Running on local environment

Prerequisites:
- maven
- docker
- docker-compose
- settings.xml corresponding to the one under oparent repository 

```bash

Navigate to project directory
```bash
cd ~/<your_git_repo>/pm-mapper
```

Build a docker image from your pm-mapper directory:

```bash
mvn clean install docker:build
```

Execute tests from pm-mapper/csit folder:
```bash
cd csit
./run-project-csit.sh
```

To run any individual suite, use run-csit.sh with appropriate plans subdirectory, for example:

```bash
./run-csit.sh plans/pmmapper
```

