# API

Base URL: your deployed Worker origin, for example `https://api.example.com`.

All protected endpoints require:

```http
Authorization: Bearer <device-token>
```

## Auth

### `POST /v1/auth/device`

Creates a device identity.

```json
{}
```

Response:

```json
{"token":"<device-token>"}
```

## Space

### `GET /v1/state`

Returns the current space, timetables, plans, and version.

### `POST /v1/space/create`

```json
{"name":"Member A","personId":"member_a"}
```

### `POST /v1/space/join`

```json
{"name":"Member B","personId":"member_b","code":"ABC123"}
```

### `POST /v1/space/rejoin`

Replaces the device token for an existing role.

```json
{"name":"Member B","personId":"member_b","code":"ABC123"}
```

## Timetable

### `POST /v1/timetable`

```json
{
  "personId": "member_a",
  "courses": [
    {"day":0,"start":"08:00","end":"08:45","name":"高等数学","tone":"b"}
  ]
}
```

`day` is 0–6 (Monday–Sunday). Times are `HH:MM` and must be valid for the
deployment's period configuration.

## OCR

### `POST /v1/ocr`

```json
{
  "personId": "member_a",
  "image": "<base64 JPEG>"
}
```

The Worker forwards the image to the configured vision provider and returns:

```json
{
  "courses": [
    {"day":0,"start":"08:00","end":"08:45","name":"高等数学"}
  ]
}
```

Server-side validation rejects unknown days, times, empty names, and duplicates.
This endpoint is an example: configure provider, consent, and retention policy
before production use.

## Plans

### `POST /v1/plan`

```json
{"day":2,"startMin":720,"endMin":780,"title":"Lunch"}
```

### `DELETE /v1/plan/:id`

Deletes a plan.

## Real-time

### `GET /v1/ws` (WebSocket upgrade)

Requires the same bearer token. Notifies peers with `state_changed`.

## Errors

```json
{"error":"human-readable message"}
```

Common status codes: `400` invalid input, `401` invalid token, `403` wrong role,
`404` not found, `409` space full/conflict, `413` image too large, `502`
upstream OCR failure.
