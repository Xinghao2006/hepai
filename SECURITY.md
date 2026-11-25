# Security policy

## Reporting

Please do not open a public issue for a security problem. Report it privately to
the maintainers through the repository's security advisory feature.

## Scope

This framework handles device tokens, invite codes, and optional timetable
images. Never commit real tokens, invite codes, D1 IDs, API keys, or timetable
screenshots.

The example OCR endpoint sends the submitted image to the configured vision
provider. Deployers are responsible for consent, retention, and provider terms.
