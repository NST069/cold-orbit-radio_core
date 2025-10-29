# Cold Orbit Radio (core)

This repository contains the core Node.js components used to integrate Liquidsoap with a small radio/streaming manager. The code generates a Liquidsoap script, runs Liquidsoap, communicates with it over the built-in telnet API and keeps a simple queue of tracks.

Quick list of core files

- `server.js` — application entry point; starts track processing and stream manager
- `streamManager.js` — generates Liquidsoap script, starts Liquidsoap, pushes tracks and polls current track
- `util/LiquidSoapClient.js` — telnet client for sending Liquidsoap server commands
- `liquidsoap/radio_template.liq` — Liquidsoap script template used by the program
- `tracks.js` — track database and queue management

Prerequisites

- Node.js (14+ recommended)
- Liquidsoap installed and available on PATH
- FFmpeg installed (used by Liquidsoap output)
- Optional: Icecast server if you stream externally

Environment variables

- `LIQUIDSOAP_PWD` — password used by the Liquidsoap script for the Icecast mount point

Add a `.env` file in the project root with:

```
LIQUIDSOAP_PWD=secret_password
```

How it works (high level)

1. The app generates a Liquidsoap script from `liquidsoap/radio_template.liq` and writes it to the configured Liquidsoap script directory.
2. It starts Liquidsoap with that script.
3. It uses Liquidsoap's telnet server (port 1234 by default) to query queue length and the currently playing track and to push tracks into Liquidsoap's request queue.
4. `streamManager.js` polls Liquidsoap periodically to detect track changes and moves the queue accordingly.

Run (development)

1. Install dependencies:

```cmd
npm install
```

2. Create `.env` with `LIQUIDSOAP_PWD`.

3. Start the server:

```cmd
node server.js
```

This will call `run()` from `tracks.js` then `init()` from `streamManager.js` which generates the Liquidsoap script and starts Liquidsoap.

Per-file documentation

See `docs/` for file-level docs:

- `docs/LiquidSoapClient.md` — telnet client API and examples
- `docs/radio_template.liq.md` — Liquidsoap script template and registered telnet functions
- `docs/server.md` — what `server.js` does and how startup flows
- `docs/streamManager.md` — stream manager responsibilities, polling logic and configuration
 - `docs/tracks.md` — track fetching, queue management and exported API from `tracks.js`
 - `docs/LiquidSoapClient.md` — telnet client API and examples
 - `docs/radio_template.liq.md` — Liquidsoap script template and registered telnet functions
 - `docs/server.md` — what `server.js` does and how startup flows
 - `docs/streamManager.md` — stream manager responsibilities, polling logic and configuration

Troubleshooting notes

- If Liquidsoap fails to start, check that the `liquidsoap` binary is in PATH and that the script path is writable.
- Liquidsoap logs are written to `liquidsoap/liquidsoap.log` (the script generation replaces the `{LOG}` token with an absolute path).
- On non-Linux platforms `streamManager.js` uses a Windows-style script path; if running on Linux adjust `LIQUIDSOAP_SCRIPT_DIR` accordingly.

Contact / next steps

If you want per-file tests or a `tracks.js` doc, tell me and I will add them.

---
Last updated: 2025-10-29
