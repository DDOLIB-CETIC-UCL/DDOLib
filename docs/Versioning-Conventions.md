# Versioning Conventions

We use the **Semantic Versioning** (SemVer) convention.

Given a version number `MAJOR.MINOR.PATCH`, increment the:

- **MAJOR** version when you make incompatible API changes.
- **MINOR** version when you add functionality in a backward-compatible manner.
- **PATCH** version when you make backward-compatible bug fixes.

More details can be found at [semver.org](https://semver.org/).

---

# Release Process

A new minor release is published every month.

Before releasing a new version, you must complete the following steps:

1. Update the changelog (e.g., `CHANGELOG.md`).
2. Update the version number in the `pom.xml` file.
3. Update the `README.md` with the latest version number.
4. Merge the `main` branch into the `release` branch.

## Releasing a Hotfix

When a critical bug occurs, we release **only** the bug fix (a PATCH version) without including unfinished features from
`main`.

- Isolate the specific bug fix commit from `main` (for example, by using `git cherry-pick`).
- Apply it directly to the `release` branch.
- The other non-released changes currently on `main` will simply wait for the next monthly minor release.