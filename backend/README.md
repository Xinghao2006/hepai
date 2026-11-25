# Backend framework

Cloudflare Worker + D1 + Durable Object for a two-member shared timetable.

## Setup

```bash
npm ci
npx wrangler d1 create shared-timetable
# put the returned database_id into wrangler.toml
npx wrangler d1 execute shared-timetable --remote --file=./schema.sql
npx wrangler secret put DEEPSEEK_API_KEY
npx wrangler deploy
```

Replace the example Android API base URL and set your Worker domain.

`/v1/ocr` forwards a composite course-card image to a vision-capable model and
expects a JSON result. The example role IDs are `member_a` / `member_b`; change
them in `src/index.js` and the Android app together.

Never commit `.dev.vars`, `.wrangler/`, real D1 IDs, account IDs, API keys, or
production invite codes.
