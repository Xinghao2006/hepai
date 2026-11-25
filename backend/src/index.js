import { SpaceRoom } from "./room.js";
import { PEOPLE, INVITE_ALPHABET } from "./config.js";
import { ApiError, json, fail, readJson } from "./http.js";
import { recognizeTimetable } from "./ocr.js";

export { SpaceRoom };

const encoder = new TextEncoder();

async function tokenHash(token) {
  const digest = await crypto.subtle.digest("SHA-256", encoder.encode(token));
  return [...new Uint8Array(digest)]
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

function newToken() {
  const bytes = crypto.getRandomValues(new Uint8Array(32));
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

function newInviteCode() {
  const bytes = crypto.getRandomValues(new Uint8Array(6));
  return [...bytes].map((byte) => INVITE_ALPHABET[byte % INVITE_ALPHABET.length]).join("");
}

function normalizeName(value) {
  const name = String(value || "").trim().slice(0, 24);
  return name || "未命名";
}

function normalizeCourses(value) {
  if (!Array.isArray(value) || value.length > 120) {
    fail("courses 必须是数组，且不超过 120 条");
  }
  return value.map((course) => {
    const day = Number(course.day);
    if (!Number.isInteger(day) || day < 0 || day > 6) fail("课程 day 不合法");
    const start = String(course.start || "").slice(0, 5);
    const end = String(course.end || "").slice(0, 5);
    if (!/^\d{2}:\d{2}$/.test(start) || !/^\d{2}:\d{2}$/.test(end)) {
      fail("课程时间格式必须为 HH:MM");
    }
    return {
      day,
      start,
      end,
      name: String(course.name || "未命名课程").slice(0, 60),
      location: String(course.location || "").slice(0, 80),
      teacher: String(course.teacher || "").slice(0, 40),
      kind: String(course.kind || "").slice(0, 20),
      tone: String(course.tone || "a").slice(0, 10),
      periodLabel: String(course.periodLabel || "").slice(0, 20),
    };
  });
}

async function requireUser(request, env) {
  const header = request.headers.get("Authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
  if (!token) fail("缺少 Authorization: Bearer <token>", 401);
  const hash = await tokenHash(token);
  const user = await env.DB.prepare("SELECT * FROM users WHERE token_hash = ?").bind(hash).first();
  if (!user) fail("设备 token 无效", 401);
  return user;
}

async function bump(env, spaceId) {
  const now = Date.now();
  await env.DB.prepare("UPDATE spaces SET version = version + 1, updated_at = ? WHERE id = ?")
    .bind(now, spaceId)
    .run();
  const row = await env.DB.prepare("SELECT version FROM spaces WHERE id = ?").bind(spaceId).first();
  return row ? row.version : 1;
}

async function notifyPartner(env, spaceId, version) {
  try {
    const id = env.SPACE_ROOM.idFromName(spaceId);
    const room = env.SPACE_ROOM.get(id);
    await room.fetch("https://room/broadcast", {
      method: "POST",
      body: JSON.stringify({ type: "state_changed", version }),
    });
  } catch (error) {
    console.error("broadcast failed", error);
  }
}

async function spaceState(env, user) {
  if (!user.space_id) {
    return {
      space: null,
      me: { personId: null, name: user.name },
      timetables: {},
      plans: [],
    };
  }

  const space = await env.DB.prepare("SELECT * FROM spaces WHERE id = ?").bind(user.space_id).first();
  if (!space) fail("所在空间不存在", 404);

  const members = await env.DB.prepare(
    "SELECT name, person_id FROM users WHERE space_id = ? ORDER BY created_at ASC"
  )
    .bind(space.id)
    .all();

  const timetableRows = await env.DB.prepare(
    "SELECT person_id, courses_json, image_key, updated_at FROM timetables WHERE space_id = ?"
  )
    .bind(space.id)
    .all();

  const planRows = await env.DB.prepare(
    "SELECT id, day, start_min, end_min, title, note, created_by, updated_at FROM plans WHERE space_id = ? AND deleted = 0 ORDER BY day, start_min"
  )
    .bind(space.id)
    .all();

  const timetables = {};
  for (const row of timetableRows.results || []) {
    let courses = [];
    try {
      courses = JSON.parse(row.courses_json);
    } catch (error) {
      courses = [];
    }
    timetables[row.person_id] = {
      courses,
      imageKey: row.image_key || null,
      updatedAt: row.updated_at,
    };
  }

  return {
    space: {
      id: space.id,
      inviteCode: space.invite_code,
      version: space.version,
      createdAt: space.created_at,
      members: (members.results || []).map((member) => ({
        personId: member.person_id,
        name: member.name,
      })),
    },
    me: { personId: user.person_id, name: user.name },
    timetables,
    plans: (planRows.results || []).map((plan) => ({
      id: plan.id,
      day: plan.day,
      startMin: plan.start_min,
      endMin: plan.end_min,
      title: plan.title,
      note: plan.note || "",
      createdBy: plan.created_by,
      updatedAt: plan.updated_at,
    })),
  };
}

async function bootstrap(request, env) {
  const token = newToken();
  const hash = await tokenHash(token);
  await env.DB.prepare("INSERT INTO users (token_hash, name, created_at) VALUES (?, ?, ?)")
    .bind(hash, "未命名", Date.now())
    .run();
  return json({ token });
}

async function createSpace(request, env, user) {
  if (user.space_id) fail("这个设备已经在一个空间里", 409);
  const body = await readJson(request);
  const personId = String(body.personId || "");
  if (!PEOPLE.has(personId)) fail("personId 必须是 member_a 或 member_b");
  const name = normalizeName(body.name);

  let spaceId = crypto.randomUUID();
  let inviteCode = newInviteCode();
  for (let attempt = 0; attempt < 6; attempt += 1) {
    const existing = await env.DB.prepare("SELECT id FROM spaces WHERE invite_code = ?")
      .bind(inviteCode)
      .first();
    if (!existing) break;
    inviteCode = newInviteCode();
  }

  const now = Date.now();
  await env.DB.prepare(
    "INSERT INTO spaces (id, invite_code, version, created_at, updated_at) VALUES (?, ?, 1, ?, ?)"
  )
    .bind(spaceId, inviteCode, now, now)
    .run();
  await env.DB.prepare(
    "UPDATE users SET name = ?, space_id = ?, person_id = ? WHERE token_hash = ?"
  )
    .bind(name, spaceId, personId, user.token_hash)
    .run();

  user.name = name;
  user.space_id = spaceId;
  user.person_id = personId;
  return json(await spaceState(env, user));
}

async function joinSpace(request, env, user) {
  if (user.space_id) fail("这个设备已经在一个空间里", 409);
  const body = await readJson(request);
  const code = String(body.code || "").trim().toUpperCase();
  const name = normalizeName(body.name);
  if (!code) fail("请输入邀请码");

  const space = await env.DB.prepare("SELECT * FROM spaces WHERE invite_code = ?").bind(code).first();
  if (!space) fail("邀请码不存在", 404);

  const members = await env.DB.prepare(
    "SELECT person_id FROM users WHERE space_id = ?"
  )
    .bind(space.id)
    .all();
  const rows = members.results || [];
  if (rows.length >= 2) {
    fail("这个空间已经有两个人了；如果这是重装 App，请用邀请码恢复原身份", 409);
  }

  const requested = String(body.personId || "");
  const occupied = new Set(rows.map((row) => row.person_id));
  const personId = PEOPLE.has(requested) && !occupied.has(requested)
    ? requested
    : ["member_a", "member_b"].find((candidate) => !occupied.has(candidate));
  if (!personId) fail("空间已满", 409);

  await env.DB.prepare(
    "UPDATE users SET name = ?, space_id = ?, person_id = ? WHERE token_hash = ?"
  )
    .bind(name, space.id, personId, user.token_hash)
    .run();
  const version = await bump(env, space.id);
  await notifyPartner(env, space.id, version);

  user.name = name;
  user.space_id = space.id;
  user.person_id = personId;
  return json(await spaceState(env, user));
}

/**
 * Recover an existing member after an App reinstall.
 * This is intentionally gated by the shared invite code + own personId.
 * It replaces only that person's device token, never the partner's.
 */
async function rejoinSpace(request, env, user) {
  if (user.space_id) fail("这个设备已经在一个空间里", 409);
  const body = await readJson(request);
  const code = String(body.code || "").trim().toUpperCase();
  const personId = String(body.personId || "");
  const name = normalizeName(body.name);
  if (!code) fail("请输入邀请码");
  if (!PEOPLE.has(personId)) fail("personId 必须是 member_a 或 member_b");

  const space = await env.DB.prepare("SELECT * FROM spaces WHERE invite_code = ?").bind(code).first();
  if (!space) fail("邀请码不存在", 404);

  const existing = await env.DB.prepare(
    "SELECT token_hash FROM users WHERE space_id = ? AND person_id = ?"
  )
    .bind(space.id, personId)
    .first();
  if (!existing) fail("这个空间里没有这个身份，请改用加入空间", 404);

  await env.DB.prepare("DELETE FROM users WHERE space_id = ? AND person_id = ?")
    .bind(space.id, personId)
    .run();
  await env.DB.prepare(
    "UPDATE users SET name = ?, space_id = ?, person_id = ? WHERE token_hash = ?"
  )
    .bind(name, space.id, personId, user.token_hash)
    .run();

  const version = await bump(env, space.id);
  await notifyPartner(env, space.id, version);

  user.name = name;
  user.space_id = space.id;
  user.person_id = personId;
  return json(await spaceState(env, user));
}

async function saveTimetable(request, env, user) {
  if (!user.space_id) fail("请先创建或加入空间", 409);
  const body = await readJson(request);
  if (String(body.personId || "") !== user.person_id) {
    fail("只能更新自己的课表", 403);
  }
  const courses = normalizeCourses(body.courses);
  await env.DB.prepare(
    `INSERT INTO timetables (space_id, person_id, courses_json, image_key, updated_at)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT(space_id, person_id) DO UPDATE SET
       courses_json = excluded.courses_json,
       image_key = excluded.image_key,
       updated_at = excluded.updated_at`
  )
    .bind(
      user.space_id,
      user.person_id,
      JSON.stringify(courses),
      null,
      Date.now()
    )
    .run();

  const version = await bump(env, user.space_id);
  await notifyPartner(env, user.space_id, version);
  return json({ ok: true, version, count: courses.length });
}

async function savePlan(request, env, user) {
  if (!user.space_id) fail("请先创建或加入空间", 409);
  const body = await readJson(request);
  const day = Number(body.day);
  const startMin = Number(body.startMin);
  const endMin = Number(body.endMin);
  const title = String(body.title || "共同安排").trim().slice(0, 60) || "共同安排";
  const note = String(body.note || "").slice(0, 200);

  if (!Number.isInteger(day) || day < 0 || day > 6) fail("day 必须是 0–6");
  if (!Number.isFinite(startMin) || !Number.isFinite(endMin) || startMin < 0 || endMin > 1440) {
    fail("时间范围不合法");
  }
  if (endMin - startMin < 10) fail("计划至少 10 分钟");
  if (endMin <= startMin) fail("结束时间必须晚于开始时间");

  const id = body.id ? String(body.id).slice(0, 64) : crypto.randomUUID();
  const updatedAt = Date.now();

  if (body.id) {
    const result = await env.DB.prepare(
      "UPDATE plans SET day = ?, start_min = ?, end_min = ?, title = ?, note = ?, updated_at = ?, deleted = 0 WHERE id = ? AND space_id = ?"
    )
      .bind(day, startMin, endMin, title, note, updatedAt, id, user.space_id)
      .run();
    if (!result.meta || result.meta.changes === 0) fail("计划不存在", 404);
  } else {
    await env.DB.prepare(
      "INSERT INTO plans (id, space_id, day, start_min, end_min, title, note, created_by, updated_at, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)"
    )
      .bind(id, user.space_id, day, startMin, endMin, title, note, user.person_id, updatedAt)
      .run();
  }

  const version = await bump(env, user.space_id);
  await notifyPartner(env, user.space_id, version);
  return json({ ok: true, id, version });
}

async function deletePlan(env, user, planId) {
  if (!user.space_id) fail("请先创建或加入空间", 409);
  const result = await env.DB.prepare(
    "UPDATE plans SET deleted = 1, updated_at = ? WHERE id = ? AND space_id = ?"
  )
    .bind(Date.now(), planId, user.space_id)
    .run();
  if (!result.meta || result.meta.changes === 0) fail("计划不存在", 404);
  const version = await bump(env, user.space_id);
  await notifyPartner(env, user.space_id, version);
  return json({ ok: true, version });
}

async function websocket(request, env, user) {
  if (!user.space_id) fail("请先创建或加入空间", 401);
  if (request.headers.get("Upgrade") !== "websocket") fail("需要 WebSocket Upgrade", 426);
  const id = env.SPACE_ROOM.idFromName(user.space_id);
  return env.SPACE_ROOM.get(id).fetch(request);
}

async function handle(request, env) {
  const url = new URL(request.url);
  const method = request.method.toUpperCase();

  if (method === "OPTIONS") {
    return new Response(null, { status: 204, headers: cors() });
  }

  if (method === "GET" && url.pathname === "/v1/health") {
    return json({ ok: true, service: "hepai-api", time: Date.now() });
  }

  if (method === "POST" && url.pathname === "/v1/auth/device") {
    return bootstrap(request, env);
  }

  if (method === "GET" && url.pathname === "/v1/ws") {
    const user = await requireUser(request, env);
    return websocket(request, env, user);
  }

  const user = await requireUser(request, env);

  if (method === "GET" && url.pathname === "/v1/state") {
    return json(await spaceState(env, user));
  }

  if (method === "POST" && url.pathname === "/v1/space/create") {
    return createSpace(request, env, user);
  }

  if (method === "POST" && url.pathname === "/v1/space/join") {
    return joinSpace(request, env, user);
  }

  if (method === "POST" && url.pathname === "/v1/space/rejoin") {
    return rejoinSpace(request, env, user);
  }

  if (method === "POST" && url.pathname === "/v1/ocr") {
    return recognizeTimetable(request, env, user);
  }

  if (method === "POST" && url.pathname === "/v1/timetable") {
    return saveTimetable(request, env, user);
  }

  if (method === "POST" && url.pathname === "/v1/plan") {
    return savePlan(request, env, user);
  }

  if (method === "DELETE" && url.pathname.startsWith("/v1/plan/")) {
    return deletePlan(env, user, decodeURIComponent(url.pathname.slice("/v1/plan/".length)));
  }

  return json({ error: "not found" }, 404);
}

export default {
  async fetch(request, env, ctx) {
    try {
      return await handle(request, env);
    } catch (error) {
      if (error instanceof ApiError) {
        return json({ error: error.message }, error.status);
      }
      console.error(error);
      return json({ error: "服务器内部错误" }, 500);
    }
  },
};
