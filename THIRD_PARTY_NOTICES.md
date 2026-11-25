# Third-party notices

This repository's own code and documentation are MIT-licensed. The following
third-party components are used as dependencies or build tooling and remain
under their respective licenses.

## Android

| Component | License | Text |
|---|---|---|
| AndroidX / Jetpack Compose | Apache License 2.0 | `LICENSES/Apache-2.0.txt` |
| Kotlin / kotlinx.coroutines / kotlinx.serialization | Apache License 2.0 | `LICENSES/Apache-2.0.txt` |
| OkHttp | Apache License 2.0 | `LICENSES/Apache-2.0.txt` |
| Material Icons (via Compose Material) | Apache License 2.0 | `LICENSES/Apache-2.0.txt` |
| JUnit 4 | Eclipse Public License 1.0 | `LICENSES/EPL-1.0.txt` |
| Gradle Wrapper | Apache License 2.0 | `LICENSES/Apache-2.0.txt` |

## Backend

| Component | License |
|---|---|
| Wrangler | MIT OR Apache-2.0 |
| Cloudflare Workers runtime / Durable Objects | Cloudflare terms |
| Node.js test runner | MIT (Node.js) |

## Fonts and preview images

The Android app uses the platform UI font; no font file is bundled in this
repository. `docs/screenshots/preview.png` is a screenshot of the demo build
with rasterized text; no font software is distributed.

The `docs/screenshots/*.png` images show the project's own demo UI and generic
course names. They do not contain third-party photos or real user timetables.

## Contributor Covenant

`CODE_OF_CONDUCT.md` is adapted from Contributor Covenant 2.1, licensed under
Creative Commons Attribution 4.0 International (CC BY 4.0):
https://www.contributor-covenant.org/version/2/1/code_of_conduct/
The full license text is in `LICENSES/CC-BY-4.0.txt`.

## What is not included

- No third-party source code was copied into the application logic.
- No third-party images, icons, fonts, or timetable screenshots are bundled
  except the dependencies listed above.
- The project name and logo should be checked for trademark availability before
  commercial use; an open-source license does not grant trademark rights.
