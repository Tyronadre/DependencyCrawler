# Dependency Crawler

This tool allows for easy creation of SPDX, CycloneDX-SBOM and VEX files. It can also analyze license collisions.
As an input, this uses a custom json input format, to allow maximum flexibility. 
The goal of this tool is to be able to analyze dependencies of an application, without being in the possession of the original build files.
Therefor, this takes a list of specified dependencies, analyzes them, and outputs in the given formats.

Note that this tool has some constraints when resolving versions, and should be used as a best effort service. 

## input format

By default we use a custom input format for maximum flexibility. The input format of the tool can be found in ProtoParser/src/in/dependency-crawler-input.proto.
It contains the information that the tool will analyze.
Here is an example input:

```json lines
{
  "application": {
    "name": "app_0",        //name of the application (can be filled at will but not null)
    "version": "1.0.0",     //version of the application (can be filled at will but not null)
    "groupId": "de.henrik", //group id of the application (optional)
    "dependencies": [
      {
        "groupId": "com.guicedee.activitymaster", //group of the dependency (optional)
        "name": "activity-master",                //the name of the dependency (not null)
        "version": "1.1.1.8-jre16",               //the version of the dependency (not null)
        "type": "MAVEN"                           //what type of component this is, where it can be found (MAVEN -> maven central repository)
      },
      {
        "groupId": "com.googlecode.htmlcompressor",
        "name": "htmlcompressor",                 
        "version": "1.5.3",                       //this is a version not found in the maven repo. however, we can find it only. we can
        "type": "MAVEN"                           //specify the found pom file in "parameters".
      },
      {
        "groupId": "platform.external",           //for an android native dependency groupId is the path in the android repository,
        "name": "bart",                           //the name is the name of the repository
        "version": "main",                        //the version is the tag, e.g. "main"
        "type": "ANDROID_NATIVE"                  
      },
      {
        "name": "s2n",
        "version": "1.4.1",
        "type": "CONAN"                           //c++/c build system
      },
      {
        "groupId": "jitpack",                     //a jitpack component is basicly a git repository release added to the code as library
        "name": "gradle-simple",                  //groupId, name are the same to ANDROID_NATIVE
        "version": "2.0",                         //the version is the release tag
        "type": "JITPACK"                         
      }
    ]
  },
  "parameters": [
    {
      "key": "POM_FILE:com.googlecode.htmlcompressor:htmlcompressor:1.5.3", //a custom pom file. Syntax: "POM_FILE:{groupId}:{name}:{version}"
      "value": "C:/Users/Henrik/Downloads/htmlcompressor-1.5.3/htmlcompressor-1.5.3/pom.xml"
    }
  ]
}
```

The tool can also read from CycloneDX-SBOM, SPDX and VEX files. Note, that a vex file does not contain enough information to generate another format than itself from it.

We can transform the input formats as such:
- default -> sbom, spdx, vex, tree, tree-all, license-collisions
- sbom -> sbom, spdx, vex, tree, tree-all, license-collisions
- spdx -> sbom, spdx, vex, tree, tree-all, license-collisions
- vex -> vex

## Output Formats

CycloneDX-SBOM, VEX and SPDX are standards and should be familiar.
Tree and tree-all are a custom tree format, that will show a tree of dependencies. tree-all will include the first level of non resolved dependencies additionally.
License-collisions will contain all collisions in libraries, and in the application in a json format.

## Commands

The shortest command to execute a programm is such:
```powershell
java -jar DependencyCrawler.jar --input input.json
```
This will execute the crawler with the default input format, and output sbom, spdx, vex and license-collisions. 

To update and transform and sbom file we use:
```powershell
java -jar DependencyCrawler.jar --input input.sbom.json --input-format sbom --output updated_sbom --output-format sbom spdx
```

All available commands are as followed:
```text
Usage:
--input <file> :                                    input file in JSON format
--output <file name> :                  [output]    output file name
--input-type <type> :                   [default]   type of the input file. Supported types: default, sbom, spdx, vex.
--output-type <type1> [<type2> ...] :   [sbom, spdx, vex, license-collisions]
                                        one or multiple output types. Supported types: sbom, spdx, vex, tree, tree-all, license-collisions
--no-log :                              [false]     disable logging.
--log-level :                           [INFO]      what level of logs should be shown. Supported types: ERROR, SUCCESS, INFO.
--crawl-optional :                      [false]     crawl dependencies flagged as optional (maven).
--crawl-all :                           [false]     crawl all dependencies, regardless of scope and optional (maven).
--crawl-threads :                       [20]        number of threads for crawling. 
--data-folder :                         [crntDir]   changed the location of the data folder.

--help :                                print this help message
```