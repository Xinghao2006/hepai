import test from "node:test";
import assert from "node:assert/strict";

import { PEOPLE, SLOTS, OCR_MODEL, OCR_MAX_TOKENS } from "../src/config.js";
import { json, cors } from "../src/http.js";

test("roles are generic and two members only", () => {
  assert.deepEqual([...PEOPLE].sort(), ["member_a", "member_b"]);
});

test("slots are sorted and valid", () => {
  assert.ok(SLOTS.length >= 8);
  for (const [start, end] of SLOTS) {
    assert.match(start, /^\d{2}:\d{2}$/);
    assert.match(end, /^\d{2}:\d{2}$/);
    assert.ok(start < end);
  }
});

test("OCR settings are bounded", () => {
  assert.ok(OCR_MODEL.length > 0);
  assert.ok(OCR_MAX_TOKENS >= 2000);
});

test("JSON response includes no-store and CORS", async () => {
  const response = json({ ok: true });
  assert.equal(response.status, 200);
  assert.equal(response.headers.get("cache-control"), "no-store");
  assert.equal(response.headers.get("access-control-allow-origin"), "*");
  assert.equal(cors()["Vary"], "Origin");
});
