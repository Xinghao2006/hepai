import { ALLOWED_ORIGIN } from "./config.js";

export class ApiError extends Error {
  constructor(message, status = 400) {
    super(message);
    this.status = status;
  }
}

export function cors() {
  return {
    "Access-Control-Allow-Origin": ALLOWED_ORIGIN,
    "Access-Control-Allow-Methods": "GET,POST,DELETE,OPTIONS",
    "Access-Control-Allow-Headers": "Authorization,Content-Type",
    "Access-Control-Max-Age": "86400",
    "Vary": "Origin",
  };
}

export function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store",
      ...cors(),
    },
  });
}

export function fail(message, status = 400) {
  throw new ApiError(message, status);
}

export async function readJson(request) {
  try {
    return await request.json();
  } catch (error) {
    fail("请求体不是合法 JSON");
  }
}
