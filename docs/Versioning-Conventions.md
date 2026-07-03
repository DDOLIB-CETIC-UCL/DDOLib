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
4. Deploy on the maven central.
5. Create a tag on the `main` branch.

> [!WARNING]
>
> The tag name MUST start with the letter `v` (e.g., `v0.0.6` or `v1.0.0`).