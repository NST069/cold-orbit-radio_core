# streamManager.js

Purpose

Controls the lifecycle of Liquidsoap: generates the script from the template, starts the Liquidsoap process, and polls Liquidsoap via telnet to manage the queue and advance tracks.

Location

`streamManager.js`

Main responsibilities

- `generateLiquidSoapScript()`
  - Reads `liquidsoap/radio_template.liq`, replaces `{PWD}` and `{LOG}` placeholders and writes the resulting script into the configured `LIQUIDSOAP_SCRIPT_DIR`.

  Additional details:
  - Ensures the script directory exists and writes `radio.liq` to `LIQUIDSOAP_SCRIPT_DIR` (on Linux `/etc/liquidsoap`, otherwise `./liquidsoap`).
  - Performs a light template validation (checks telnet setting and required `coldorbit` handlers) and logs warnings if the template seems incompatible with the expected API.

- `startLiquidSoap()`
  - Spawns the `liquidsoap` process passing the generated script path.
  - Pipes stdout/stderr to the parent process logs.
  - Logs spawn errors and adds an `error` listener so failures to start the binary are visible.

- `checkTrack()` (poller)
  - Uses `util/LiquidSoapClient.sendCommand` to call `coldorbit.length` and `coldorbit.current` on the Liquidsoap telnet API. `coldorbit.length` output is parsed defensively (non-digit characters stripped) to avoid parse/NaN issues.
  - Keeps `currentTrack` state to detect when a track changed.
  - When a track change is detected it calls `removeTrackFromQueue()` and pushes the next track into Liquidsoap using `coldorbit.push <filename>`.
  - Adjusts polling interval depending on whether there are queued tracks (`60s` when queue has items, `5s` otherwise).

Additional behaviors

- Path handling: when pushing a local file the code builds the absolute path using `path.join()` and then converts backslashes to forward slashes before sending to Liquidsoap (Liquidsoap expects `/` separators).
- Telnet wait: before starting the poller the manager uses `waitForTelnet()` to avoid immediate `ECONNREFUSED` errors when Liquidsoap is still starting.
- Startup push: if a persisted local queue exists and Liquidsoap reports an empty queue, the manager pushes the next persisted track immediately so playback can resume without waiting for the next poll interval.

Configuration notes

- The script location `LIQUIDSOAP_SCRIPT_DIR` is chosen based on `os.platform()`; on Windows it uses `./liquidsoap` and on Linux `\etc\liquidsoap` (verify and adapt for your system).
- `MUSIC_DIRECTORY` points to `_td_files/music/` where `streamManager` expects track files.

Integration with `tracks.js`

`streamManager.js` depends on functions exported by `tracks.js`:

- `removeTrackFromQueue()` — called when Liquidsoap finishes a track
- `getQueueLength()` — to decide whether to push new tracks
- `getNextTrack()` — supplies the next track filename to push
- `scheduleTrack()` — called after pushing to schedule internal bookkeeping
- `getTrackTitle()` — helper for logging

Errors & Logging

- The spawned Liquidsoap process logs its stdout and stderr to the parent process console.
- `LiquidSoapClient.sendCommand` promise rejections should be logged or handled; `streamManager.js` currently catches command errors in-place with `.catch(e => console.log(e))`.

Notes & possible improvements

- Replace string path manipulations with `path.join()` for cross-platform correctness.
- Consider stronger error handling and restart logic for the Liquidsoap child process.
- Add unit tests for `generateLiquidSoapScript()` (verify tokens replaced) and for `checkTrack()` logic (mock `sendCommand`).
