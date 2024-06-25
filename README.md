# FileFormat

## SPDX

SPDX als eigenes proto file schreiben?

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

- [ ] version resolver ist nicht recursive auf pom files die als dependency management geladen werden
- [ ] read from sbom broken
- [ ] read from vex broken

## TODOS

- [X] https://jitpack.io als source. (zip runterladen)
- [x] vex files output
- [x] read spdx file
- [x] update loaded spdx file
- [x] read sbom file
- [x] conan repo link dependency (native). iwi api link rausfinden und parsen.
- [x] android native dependency. version is tag
- [x] license collision. von allen (auch dependencies)
- [x] read & update vex file
- [x] better gradlew script
- [x] SPDX Lizenzen laden
- [x] Wenn Component in mehreren Versionen, dann höchste Version nehmen
- [x] Im input kann eine Component mehrfach vorkommen, hier höchste benutzte Version nehmen.
- [x] use data from maven parent when empty
- [x] dependency tree sbom nur tiefe 1
- [x] user möglichkeit geben pom files zu spezifizieren, für repos die nicht geladen werden konnten im zweiten durchgang

## References

- (License find) https://github.com/pivotal/LicenseFinder
- (License find) https://github.com/jaredsburrows/gradle-license-plugin
- (license analysis) https://fossa.com
- (license analysis) https://www.mend.io
- (license analysis) https://www.blackducksoftware.com
- (license analysis) https://github.com/jk1/Gradle-License-Report

## FRAGEN

## ARBEIT

~ 30 Seiten zusätzlich bilder u.ä
~ englisch!

### INTRODUCTION

einführung für person die sich nicht auskennt

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
