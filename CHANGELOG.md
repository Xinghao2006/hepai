# Changelog

All notable changes to this framework are documented here.

## [Unreleased]

### Added

- Generic two-member timetable framework with invite/rejoin flow.
- Versioned REST + WebSocket sync.
- Android Compose week / free-time / day-detail UI.
- Configurable OCR-upload contract with a Worker-side validation layer.
- Unit tests for Android course normalization and Worker validation.
- GitHub Actions for Android build/tests and Worker tests.

### Notes

- The OCR endpoint is an example integration. Configure your own model,
  provider, consent, and retention policy before production use.
