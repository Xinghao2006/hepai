import test from "node:test";
import assert from "node:assert/strict";

import { parseLooseJson, cleanCourseName, validateCourses } from "../src/validation.js";
import { SLOTS } from "../src/config.js";

test("parses fenced and trailing-comma JSON", () => {
  const parsed = parseLooseJson('```json\n{"courses":[{"day":0,"name":"课程 A"},]}\n```');
  assert.equal(parsed.courses[0].name, "课程 A");
});

test("repairs prose around a JSON object", () => {
  const parsed = parseLooseJson('Result:\n{"ok":true}\nDone');
  assert.equal(parsed.ok, true);
});

test("removes classroom and teacher suffixes", () => {
  assert.equal(cleanCourseName("高等数学@教学楼A 张老师"), "高等数学");
  assert.equal(cleanCourseName("计算机网络研201[理]"), "计算机网络");
});

test("validates days, time slots and duplicate courses", () => {
  const courses = validateCourses([
    { day: 0, start: "08:00", end: "08:45", name: "课程 A" },
    { day: 0, start: "08:00", end: "08:45", name: "课程 A" },
    { day: 7, start: "08:00", end: "08:45", name: "课程 B" },
    { day: 1, start: "08:00", end: "09:41", name: "课程 C" },
  ], SLOTS);
  assert.deepEqual(courses, [
    { day: 0, start: "08:00", end: "08:45", name: "课程A" },
  ]);
});

test("uses only configured slots", () => {
  const courses = validateCourses([
    { day: 2, start: "08:10", end: "08:50", name: "未被支持的时间" },
    { day: 2, start: "08:00", end: "08:45", name: "支持的时间" },
  ], SLOTS);
  assert.equal(courses.length, 1);
  assert.equal(courses[0].name, "支持的时间");
});
