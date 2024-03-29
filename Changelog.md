# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.10.1] - 2023/01/31
### Changed
- [DCAEGEN2-3323] Fix vulnerabilities:
  - Update io.undertow:2.2.17.Final to version 2.3.0.Final

## [1.10.0] - 2023/01/11
### Changed
- [DCAEGEN2-3190] Bug fix:
  - PM-mapper is not able to map the input data from xml file from measType to measTypes.
  - As it is taking the p value from the input xml, it is no longer sequential.

## [1.9.0] - 2022/05/13
### Changed
- [DCAEGEN2-3218] Fix vulnerabilities:
  - Update io.undertow:undertow-core to version 2.2.17.Final
  - Update dcae-sdk to version 1.8.10
  - Update io.projectreactor:reactor-core to version 3.4.21
- [DCAEGEN2-3037] Disable TLS in DataRouter (CSIT)
- [DCAEGEN2-3182] Extend development tools


## [1.8.0] - 2022/01/24
### Changed
- [DCAEGEN2-2964] Switch configuration provider to CBS Client - DCAE SDK
- [DCAEGEN2-3049] Remove vulnerability
- [DCAEGEN2-3032] [DCAEGEN2-3038] Allow supports unauthenticated topic and connection without TLS

## [1.7.2] - 2021/08/26
### Changed
- Fix issues reported by Sonar

## [1.7.1] - 2021/08/18
### Changed
- Add pm-mapper local development tools
- Fix granularityPeriod mapping

## [1.7.0] - 2021/07/29
### Changed
- Update io.undertow:undertow-core to version 2.2.9.Final
- Update org.freemarker:freemarker to version 2.3.31
- Update oparent to version 3.2.0

## [1.6.0] - 2021/04/16
### Changed
- Utilize DMaaP-Client in PM-Mapper
- Switched Dockerfile to integration image (alpine-based)

## [1.5.2] - 2021/03/18

- Implement singleton cache for events being processed
- Add JUnit tests

## [1.5.1] - 2021/02/03
### Added
- Add configuration number of threads and limit rate in files processing
### Security
- Fix vulnerability - update undertow from 2.0.3.Final to 2.2.3.Final and commons.io from 2.6.0 to 2.8.0
