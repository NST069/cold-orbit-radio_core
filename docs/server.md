# server.js

Purpose

`server.js` is the application entry point. It loads environment variables, starts the track processing, initializes the stream manager and starts an Express listener (used here only as a tiny runtime host — no route handlers are defined by default).

Key behavior

- Loads `.env` via `require('dotenv').config()`.
- Imports and calls `run()` from `tracks.js` and then `init()` from `streamManager.js`.
- Starts an Express server on port 3000 which logs a message when ready.

Note: `streamManager.init()` will generate the Liquidsoap script and start Liquidsoap; it now waits for the telnet port to become available before the rest of the app begins calling telnet commands.

Run

```cmd
node server.js
```

Notes

- The visible HTTP server is minimal and used mainly to keep the Node process alive and expose a potential extension point for HTTP APIs later.
- If you want the app to run without opening a network port, you may remove or modify the Express server, but ensure `run()` and `init()` are still invoked.
