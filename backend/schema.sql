PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  token_hash TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  space_id TEXT,
  person_id TEXT,
  created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS spaces (
  id TEXT PRIMARY KEY,
  invite_code TEXT UNIQUE NOT NULL,
  version INTEGER NOT NULL DEFAULT 1,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS timetables (
  space_id TEXT NOT NULL,
  person_id TEXT NOT NULL,
  courses_json TEXT NOT NULL,
  image_key TEXT,
  updated_at INTEGER NOT NULL,
  PRIMARY KEY (space_id, person_id)
);

CREATE TABLE IF NOT EXISTS plans (
  id TEXT PRIMARY KEY,
  space_id TEXT NOT NULL,
  day INTEGER NOT NULL,
  start_min INTEGER NOT NULL,
  end_min INTEGER NOT NULL,
  title TEXT NOT NULL,
  note TEXT,
  created_by TEXT NOT NULL,
  updated_at INTEGER NOT NULL,
  deleted INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_plans_space ON plans(space_id, updated_at);
CREATE INDEX IF NOT EXISTS idx_users_space ON users(space_id);
