export function parseLooseJson(content) {
  let value = String(content || "").trim()
    .replace(/^```(?:json)?\s*/i, "")
    .replace(/```$/i, "")
    .trim();
  const start = value.indexOf("{");
  const end = value.lastIndexOf("}");
  if (start >= 0 && end > start) value = value.slice(start, end + 1);
  value = value.replace(/,\s*([}\]])/g, "$1");
  return JSON.parse(value);
}

export function cleanCourseName(value) {
  let name = String(value || "").trim();
  for (const marker of ["@", "＠", "研", "实"]) {
    const index = name.indexOf(marker);
    if (index >= 0) name = name.slice(0, index);
  }
  return name
    .replace(/\[[^\]]{1,4}\]/g, "")
    .replace(/\.\.\.|…/g, "")
    .replace(/\s+/g, "")
    .slice(0, 40);
}

export function validateCourses(rawCourses, slots) {
  if (!Array.isArray(rawCourses)) return [];
  const allowedStarts = new Set(slots.map((slot) => slot[0]));
  const allowedEnds = new Set(slots.map((slot) => slot[1]));
  const cleaned = [];
  for (const course of rawCourses) {
    const day = Number(course?.day);
    const start = String(course?.start || "").slice(0, 5);
    const end = String(course?.end || "").slice(0, 5);
    if (!Number.isInteger(day) || day < 0 || day > 6) continue;
    if (!allowedStarts.has(start) || !allowedEnds.has(end)) continue;
    if (start >= end) continue;
    const name = cleanCourseName(course?.name);
    if (name.length < 2) continue;
    cleaned.push({ day, start, end, name });
  }
  return cleaned.filter((course, index, all) =>
    all.findIndex((other) =>
      other.day === course.day &&
      other.start === course.start &&
      other.end === course.end &&
      other.name === course.name
    ) === index
  );
}
