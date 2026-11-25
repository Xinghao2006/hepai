/**
 * One Durable Object per shared space.
 * It only keeps WebSocket connections and fans out version notifications.
 * The authoritative state lives in D1.
 */
export class SpaceRoom {
  constructor(ctx, env) {
    this.ctx = ctx;
    this.env = env;
  }

  async fetch(request) {
    const url = new URL(request.url);

    if (url.pathname.endsWith("/broadcast")) {
      const message = await request.text();
      for (const socket of this.ctx.getWebSockets()) {
        try {
          socket.send(message);
        } catch (error) {
          // A dead socket is cleaned up by the runtime.
        }
      }
      return new Response("ok");
    }

    if (!url.pathname.endsWith("/ws")) {
      return new Response("not found", { status: 404 });
    }

    if (request.headers.get("Upgrade") !== "websocket") {
      return new Response("Expected WebSocket", { status: 426 });
    }

    const pair = new WebSocketPair();
    const [client, server] = Object.values(pair);
    this.ctx.acceptWebSocket(server);

    return new Response(null, {
      status: 101,
      webSocket: client,
    });
  }

  async webSocketMessage(socket, message) {
    if (message === "ping") {
      socket.send("pong");
    }
  }

  async webSocketClose(socket, code, reason) {
    try {
      socket.close(code, reason);
    } catch (error) {
      // Already closed.
    }
  }

  async webSocketError(socket) {
    try {
      socket.close(1011, "room error");
    } catch (error) {
      // Already closed.
    }
  }
}
