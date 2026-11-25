export const PEOPLE = new Set(["member_a", "member_b"]);

export const INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

// Generic example timetable. Replace these slots for your deployment.
export const SLOTS = [
  ["08:00", "08:45"],
  ["08:55", "09:40"],
  ["10:00", "10:45"],
  ["10:55", "11:40"],
  ["14:00", "14:45"],
  ["14:55", "15:40"],
  ["16:00", "16:45"],
  ["16:55", "17:40"],
  ["19:00", "19:45"],
  ["19:55", "20:40"],
];

export const OCR_MODEL = "deepseek-flash";
export const OCR_MAX_TOKENS = 8000;
export const MAX_IMAGE_BASE64 = 12 * 1024 * 1024;

// Set this to your deployed origin. "*" is only for local experiments.
export const ALLOWED_ORIGIN = "*";
