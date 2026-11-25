# Contributing

Thanks for considering a contribution.

## Development

```bash
cd backend && npm ci && npm test
cd ../android && ./gradlew :app:ktlintCheck :app:testDebugUnitTest
```

## Pull requests

- Keep changes focused and explain the user-visible behavior.
- Add or update tests for logic changes.
- Do not commit secrets, `.wrangler/`, `.dev.vars`, real D1/account IDs, private
  timetable screenshots, or personal data.
- Update `CHANGELOG.md` for user-visible changes.

## Code of conduct

By participating, you agree to follow `CODE_OF_CONDUCT.md`.
