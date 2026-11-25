# Privacy

This framework is a template, not a hosted service.

## Data the app handles

- device token (stored locally)
- space invite code
- member display name
- timetable courses (day, time, name)
- optional timetable screenshot for OCR

## OCR

The example `/v1/ocr` endpoint sends the submitted image to a vision provider.
If you deploy this endpoint, you must:

- obtain consent from the people whose timetables are uploaded;
- document the provider's data-retention behavior;
- avoid retaining images longer than necessary;
- provide a way to delete timetables and plans.

The repository itself contains no real user screenshots or schedules.

## Secrets

Keep these out of git:

- `DEEPSEEK_API_KEY`
- `.dev.vars`
- `.wrangler/`
- real D1/account/zone IDs
- production invite codes and device tokens
