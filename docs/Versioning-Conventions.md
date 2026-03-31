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

To apply a hotfix cleanly, we use the **Hotfix Branch** workflow:

1. **Branch from release:** Create a new temporary hotfix branch starting from the `release` branch (e.g.,
   `git switch -c hotfix-bug-name release`).
2. **Fix the bug:** Commit your specific correction to this new hotfix branch.
3. **Update release:** Merge your hotfix branch back into the `release` branch to publish the patch.
4. **Update main (Crucial):** Merge the `release` branch (or the hotfix branch) back into the `main` branch. This
   ensures the bug fix is integrated into ongoing development and prevents the bug from reappearing in the next monthly
   minor release.

*(Alternatively, if the fix is already isolated in a single commit on `main`, you can
use `git cherry-pick <commit-hash>` while on the `release` branch to apply it directly.)*