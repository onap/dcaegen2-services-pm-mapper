# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.8.0] - 24/01/2022
### Changed
- Switch configuration provider to CBS Client - DCAE SDK

## [1.7.2] - 26/08/2021
### Changed
- Fix issues reported by Sonar

## [1.7.1] - 18/08/2021
### Changed
- Add pm-mapper local development tools
- Fix granularityPeriod mapping

## [1.7.0] - 29/07/2021
### Changed
- Update io.undertow:undertow-core to version 2.2.9.Final
- Update org.freemarker:freemarker to version 2.3.31
- Update oparent to version 3.2.0

## [1.6.0] - 16/04/2021
### Changed
- Utilize DMaaP-Client in PM-Mapper
- Switched Dockerfile to integration image (alpine-based)

## [1.5.2] - 18/03/2021

- Implement singleton cache for events being processed
- Add JUnit tests

## [1.5.1] - 03/02/2021
### Added
- Add configuration number of threads and limit rate in files processing
### Security
- Fix vulnerability - update undertow from 2.0.3.Final to 2.2.3.Final and commons.io from 2.6.0 to 2.8.0
