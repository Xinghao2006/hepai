import { fail, json, readJson } from "./http.js";
import { MAX_IMAGE_BASE64, OCR_MAX_TOKENS, OCR_MODEL, SLOTS } from "./config.js";
import { parseLooseJson, validateCourses } from "./validation.js";

async function repairModelJson(env, badContent) {
  const prompt = `下面这段课程表识别结果本应是合法 JSON，但格式不合法。
请把它修复成严格 JSON，只保留这个结构：
{"courses":[{"day":0,"start":"08:00","end":"08:45","name":"课程名"}]}
不要删除真实课程，不要编造课程，不要输出 markdown 或解释。原始内容：
${String(badContent).slice(0, 12000)}`;
  const response = await fetch("https://api.deepseek.com/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${env.DEEPSEEK_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: OCR_MODEL,
      messages: [
        { role: "system", content: "你是 JSON 修复器。只返回合法 JSON。" },
        { role: "user", content: prompt },
      ],
      response_format: { type: "json_object" },
      temperature: 0,
      max_tokens: 5000,
    }),
  });
  if (!response.ok) return null;
  const data = await response.json();
  return data?.choices?.[0]?.message?.content || null;
}

export async function recognizeTimetable(request, env, user) {
  if (!user.space_id) fail("请先创建或加入空间", 409);
  if (!env.DEEPSEEK_API_KEY) fail("服务端还没有配置 DEEPSEEK_API_KEY", 503);
  const body = await readJson(request);
  const rawImage = String(body.image || "").replace(/^data:image\/[a-zA-Z0-9.+-]+;base64,/, "");
  if (rawImage.length < 200) fail("图片为空");
  if (rawImage.length > MAX_IMAGE_BASE64) fail("图片过大", 413);
  const personId = String(body.personId || user.person_id);
  if (personId !== user.person_id) fail("只能识别自己的课表", 403);

  const isMemberB = personId === "member_b";
  const table = SLOTS
    .map((slot, index) => `第${index + 1}行 ${slot[0]}-${slot[1]}`)
    .join("\n");
  const prompt = isMemberB
    ? `这是成员 B 课表按天拆开的 7 列总图。列顶部从左到右依次是：周一、周二、周三、周四、周五、周六、周日。
每列左侧是第1～10行时间尺。请严格按列顶标签确定 day：0=周一,1=周二,2=周三,3=周四,4=周五,5=周六,6=周日。
返回严格 JSON：
{"courses":[{"day":0,"start":"08:00","end":"08:45","name":"课程名"}]}
课程块跨第a-b行时：start取第a行开始时间，end取第b行结束时间。
start 必须是某行开始时间，end 必须是某行结束时间；不能自己编：
${table}
每个彩色课程块输出一条；课程名忽略教室、教师、校区、[理]、[实]、省略号。
不要输出 markdown，不要输出解释。`
    : `这是成员 A 的原始课表截图。
请按图中的星期列和左侧时间行，读取每个彩色课程块，返回严格 JSON：
{"courses":[{"day":0,"start":"08:00","end":"08:45","name":"课程名"}]}
day: 0=周一,1=周二,2=周三,3=周四,4=周五,5=周六,6=周日。
重要：最左侧窄列“月份”不是星期一，不要把它当作课程列。
课程列从左到右依次是：周一、周二、周三、周四、周五、周六、周日。
请按每列标题下面的日期确认 day，不要按视觉列序号猜。
成员 A 图左侧是第1～10行。课程块跨第a-b行时：start取第a行开始时间，end取第b行结束时间。
start 必须是某行开始时间，end 必须是某行结束时间；不能自己编：
${table}
每个彩色课程块输出一条；课程名忽略教室、教师、校区、[理]、[实]、省略号。
不要输出 markdown，不要输出解释。`;

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 55_000);
  let response;
  try {
    response = await fetch("https://api.deepseek.com/chat/completions", {
      method: "POST",
      signal: controller.signal,
      headers: {
        "Authorization": `Bearer ${env.DEEPSEEK_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: OCR_MODEL,
        messages: [
          { role: "system", content: "你是课程表 OCR 结构化助手。只返回合法 JSON。" },
          {
            role: "user",
            content: [
              { type: "text", text: prompt },
              { type: "image_url", image_url: { url: `data:image/jpeg;base64,${rawImage}` } },
            ],
          },
        ],
        response_format: { type: "json_object" },
        temperature: 0,
        max_tokens: OCR_MAX_TOKENS,
      }),
    });
  } finally {
    clearTimeout(timer);
  }

  if (!response.ok) {
    const detail = await response.text();
    console.error("deepseek error", response.status, detail.slice(0, 500));
    fail("DeepSeek 识别失败", 502);
  }
  const data = await response.json();
  const content = data?.choices?.[0]?.message?.content;
  if (!content) fail("DeepSeek 没有返回内容", 502);
  let parsed;
  try {
    parsed = parseLooseJson(content);
    if (!Array.isArray(parsed?.courses)) throw new Error("missing courses");
  } catch (error) {
    const repaired = await repairModelJson(env, content);
    if (!repaired) fail("DeepSeek 返回的 JSON 无法修复", 502);
    try {
      parsed = parseLooseJson(repaired);
      if (!Array.isArray(parsed?.courses)) throw new Error("missing courses");
    } catch (repairError) {
      fail("DeepSeek 返回的 JSON 无法修复", 502);
    }
  }
  return json({ courses: validateCourses(parsed.courses, SLOTS) });
}
