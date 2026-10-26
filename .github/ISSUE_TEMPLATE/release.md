---
name: Release
about: Checklist of the steps to complete before publishing a new version of DDOLib
title: "Release vX.Y.Z"
labels: release
assignees: ''
---

## Version

<!-- Follow the Semantic Versioning convention (see docs/Versioning-Conventions.md):
     MAJOR for incompatible API changes, MINOR for backward-compatible features,
     PATCH for backward-compatible bug fixes. -->

- Previous version: `vA.B.C`
- New version: `vX.Y.Z`
- Type: <!-- major / minor / patch -->

### Code

- [ ] All the issues and pull requests planned for this release are merged into `main`
- [ ] The unit tests pass (`mvn test`)
- [ ] The non-regression tests pass (`mvn test -P non-regression-tests`)
- [ ] The Javadoc builds without errors (`mvn javadoc:javadoc`)

### Version numbers

- [ ] `pom.xml`: update `<version>` to `X.Y.Z` (without the `-SNAPSHOT` suffix, if any)
- [ ] `README.md`: update the version in the Maven dependency snippet
- [ ] `userguide/source/intro.rst`: update the version in the Maven dependency snippet
- [ ] `userguide/source/conf.py`: update `version` (`X.Y`) and `release` (`X.Y.Z`)
- [ ] No other reference to the previous version remains (`grep -rn "A.B.C" --exclude-dir=target .`)

### Changelog

- [ ] `CHANGELOG.md`: rename the `[Unreleased]` section to `[X.Y.Z] - DD/MM/YYYY`
- [ ] `CHANGELOG.md`: check that every notable change of the release is listed, in the right
  section (`Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, ...)
- [ ] `CHANGELOG.md`: add a new empty `[Unreleased]` section at the top

### Documentation

- [ ] `README.md`: up to date with the new features (solvers, examples, ...)
- [ ] User guide (`userguide/`): up to date with the new features and API changes
- [ ] Wiki pages (`docs/`): up to date