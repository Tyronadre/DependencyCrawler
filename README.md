# FileFormat

## SPDX

- Json format
    - "SPDXID" : id?
    - "spdxVersion" : "SPDX-2.3"
    - "creationInfo" : {}
        - "comment" : ?
        - created: ?
        - creators: []
            - "Tool: ?", "Organization: ?", "Person: ?"
        - licenseListVersion: ?
    - name: ?
    - dataLicense: ?
    - comment: ?
    - externalDocumentRefs: []

## NATIVE LIBRARY

Ich bekomme eine repo link (conan)
ODER Native librarys von android
(NAME DER LIB, VERSION (ANDROID
VERSION)) [link](https://android.googlesource.com/platform/system/core.git/+/refs/tags/android-14.0.0_r45)

## Lizenzen

Wenn Lizenz nicht in Component, in parent gucken!

## Licence collision

Ob Lizenzen sich gegenseitig verbieten, z.b. dependency hat common lizenz, aber parent hat private lizenz.

Ich bin mir nicht sicher was ich hier machen soll. Ich kann zwar die alle durchgehen, aber eigentlich sollten
dependencies an sich ja immer diese Bedingungen erfüllen.
Was Sinn machen würde, wäre zu gucken anhand aller Lizenzen die die Dependencies haben, wie die Lizenz von der App
aussehen muss.
Außerdem kann ich hier nur die ein paar Lizenzen prüfen, da ich jede per Hand analysieren muss. (es werden eh fast
ausschließlich Apache-2.0, GPL-3.0 und MIT benutzt)

## Conversion

Ressourcen neu laden aus web laden

## vulnerability

cron job ob neue vulerabilitys auf vex / cyclondx

## Bugs

- [x] wie werden hier die versionen
  resolved https://repo1.maven.org/maven2/com/google/firebase/firebase-admin/8.1.0/firebase-admin-8.1.0.pom -> wird
  ignoriert da ich nicht weiß wie
- [ ] SBOM ref muss scope mit drin haben.
- [x] Es werden sachen doppel geladen im multithreading
- [ ] main app ist als component drin

## Performance

- [x] multithreading

## TODOS

- [ ] https://jitpack.io als source?
- [x] vex files output
- [ ] read spdx file
- [x] update loaded spdx file
- [x] read sbom file
- [ ] conan repo link dependency (native)
- [ ] android native dependency
- [ ] license collision
- [x] read & update vex file
- [ ] read and update vulnerabilitys in spdx file
- [x] better gradlew script
- [x] SPDX Lizenzen laden
- [x] Wenn Component in mehreren Versionen, dann höchste Version nehmen
- [x] Im input kann eine Component mehrfach vorkommen, hier höchste benutzte Version nehmen.
- [ ] use data from maven parent when empty

## References

- (License find) https://github.com/pivotal/LicenseFinder
- (License find) https://github.com/jaredsburrows/gradle-license-plugin
- (license analysis) https://fossa.com
- (license analysis) https://www.mend.io
- (license analysis) https://www.blackducksoftware.com
- (license analysis) https://github.com/jk1/Gradle-License-Report

## FRAGEN

- https://cyclonedx.org/use-cases/#dependency-graph und spec stimmen bei dependecies nicht überein
- (see above) steht dass man dependencies am besten nur eine ebene tief hinschreiben soll wegen cycle. wir können aber
  in unserem fall keine cycle haben. soll ich das ändern?

## ARBEIT

~ 30 Seiten zusätzlich bilder u.ä

### INTRODUCTION

### BACKGROUND

### RELATED WORK (oder nach evaluation)

textuell vergleich

### ANSATZ

### EVALUATION

empirischer vergleich

### LIMITATIONS

### CONCLUSION

## until 2 weeks

- conversion
- vex file
- nativ libraries?
- mehr lizenzen laden?
- lizenz collision?
- text anfangen
