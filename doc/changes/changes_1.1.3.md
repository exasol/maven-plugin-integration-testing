# Maven Plugin Integration Testing 1.1.3, released 2024-06-06

Code name:

## Summary

This release fixes a broken link in file `changes_1.1.2.md`, updates dependencies on top of 1.1.2, and removes transitive dependency `junit:junit`.

## Features

* #11: Removed transitive dependency `junit:junit`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.4.1` to `1.0.1`
* Updated `commons-io:commons-io:2.11.0` to `2.16.1`
* Updated `org.apache.maven:maven-model:3.8.6` to `3.9.7`

### Runtime Dependency Updates

* Updated `org.jacoco:org.jacoco.agent:0.8.8` to `0.8.12`

### Test Dependency Updates

* Updated `org.junit.jupiter:junit-jupiter-engine:5.8.2` to `5.10.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.8.2` to `5.10.2`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.1.1` to `2.0.3`
* Updated `com.exasol:project-keeper-maven-plugin:2.4.6` to `4.3.3`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.15` to `0.16`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.10.1` to `3.13.0`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0-M1` to `3.1.2`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.0.0` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M5` to `3.2.5`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.0.1` to `3.2.4`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.0` to `3.7.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M5` to `3.2.5`
* Added `org.apache.maven.plugins:maven-toolchains-plugin:3.2.0`
* Added `org.basepom.maven:duplicate-finder-maven-plugin:2.0.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.2.7` to `1.6.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.10.0` to `2.16.2`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.8` to `0.8.12`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184` to `4.0.0.4121`
* Updated `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13` to `1.7.0`
