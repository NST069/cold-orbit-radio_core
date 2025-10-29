# tracks.js

Purpose

`tracks.js` handles fetching audio messages from a configured Telegram channel, downloading audio files (and covers) to the local music directory, maintaining an in-memory queue of tracks and performing periodic garbage collection of unused files.

Location

`tracks.js`

Environment variables used

- `API_ID` — Telegram API ID
- `API_HASH` — Telegram API hash
- `PHN` — phone number used to log in the Telegram client
- `CHANNEL` — public channel username (e.g. `@channelname`) to fetch tracks from

Dependencies

- `tdl` and `prebuilt-tdlib` — used to interact with Telegram
- Node's `fs` module for file operations

Important constants

- `MUSIC_DIRECTORY` — local directory where downloaded audio files are stored (`_td_files/music/`)
- Timeouts (ms):
  - `GET_TRACKS_FROM_CHANNEL_TIMEOUT` — refresh tracks from channel every 1 hour
  - `DELETE_UNUSED_TRACKS_TIMEOUT` — run garbage collector every 10 minutes
  - `FULL_QUEUE_TIMEOUT` — 5 minutes pause when queue is full
  - `GET_RANDOM_TRACK_TIMEOUT` — 2 minutes between background downloads when not initializing
  - `UNIVERSAL_RETRY_TIMEOUT` — 30 seconds retry on errors

How it works (high-level)

1. The module creates and logs in a Telegram client using `tdl` and `prebuilt-tdlib`.
2. `getTracksFromChannel(channelUsername)` pages through the channel history and collects messages that contain audio (`messageAudio`). It stores metadata in an internal `tracks` array and refreshes this list on a 1-hour timer.
3. `getRandomTrack()` picks a random item from `tracks`, downloads the audio and optional cover via `downloadTrack()` / `downloadTrackCover()`, then pushes an object to `trackQueue`:
   - `{ postId, fileName, coverFileName, isScheduled }`
4. `deleteUnusedTracks()` periodically scans `MUSIC_DIRECTORY` and deletes files that are not present in `trackQueue` (garbage collector).
5. The module exposes several helper functions used by `streamManager.js` (see Exported API below).

Background behavior and side effects

- The module runs continuous background tasks via `setTimeout` loops: channel polling, queue filling (random downloads) and garbage collection.
- Downloads are written to disk under `MUSIC_DIRECTORY` and may be renamed during the process.
- The module now persists the in-memory `trackQueue` to disk at `liquidsoap/trackQueue.json` (next to the generated `radio.liq`). Saves are debounced to avoid frequent writes.

Exported API (what other modules call)

 - `run()` — starts background tasks: loads the persisted queue (if present), connects to Telegram, fetches channel tracks, begins downloading random tracks and starts the garbage collector.

 - `removeTrackFromQueue()` — removes the first item from `trackQueue` (used when `streamManager` detects a track ended) and persists the queue.

 - `getQueueLength()` — returns the number of non-scheduled tracks in the queue (used to decide whether to push more tracks to Liquidsoap).

- `getNextTrack()` — returns the next non-scheduled track object from the queue.

- `getTrackMetadata(fileName)` — returns metadata for a given file by mapping the queue entry to the `tracks` list (uses `postId`).

- `getTrackTitle(fileName)` — returns a human-readable title for a given file (uses `getTrackMetadata`).

 - `scheduleTrack(fileName)` — marks the queue entry as scheduled (sets `isScheduled = true`) and persists the queue.

Notes & caveats

- The module persists the `trackQueue` to `liquidsoap/trackQueue.json` and loads it on startup. `loadQueue()` filters out queue entries whose files are missing and resets `isScheduled` to `false` for all loaded entries.
- Many operations (downloads, deletes) are asynchronous but the code uses simple callbacks / promises; consider stronger error handling if used in production.
- `MUSIC_DIRECTORY` and other paths now use `path.join()` for cross-platform robustness.
- Several timeouts are hard-coded; moving them to configurable environment variables would make tuning easier.

Track title derivation

- The helper `getTrackFullName(track)` (used by `getTrackTitle`) derives a human-friendly title using the following rules, in order:
  - If the message `caption` exists, return its first line (trimmed). This solves multi-line captions where the first line contains the intended display title.
  - If at least one of `performer` or `title` fields is present, build the string `performer - title`. Missing parts are filled with Russian placeholders: `<Неизвестен>` for missing performer and `<Без названия>` for missing title.
  - If neither caption nor title/performer are present, the function falls back to the audio `fileName` (stripping the extension).
  - If none of the above data is available, it returns the string `Unknown`.

Suggested improvements

- The queue path is currently next to the generated Liquidsoap script; consider making it configurable via `QUEUE_PATH` env var so runtime files live outside the repo (e.g., `/var/lib/cold-orbit/`).
- Add unit tests for helper functions (e.g., `getTrackFullName`) — mock `tdl` client for deterministic tests.
- Consider flushing the debounced save on process exit to guarantee the final state is persisted.

Example integration (how `streamManager.js` uses this module)

- `streamManager.js` calls:
  - `getQueueLength()` to decide whether to push tracks to Liquidsoap
  - `getNextTrack()` to obtain `fileName` for `coldorbit.push`
  - `scheduleTrack(fileName)` after pushing to mark it scheduled
  - `removeTrackFromQueue()` when Liquidsoap moves to the next track

This completes the overview of `tracks.js`. If you'd like, I can also:

- Add `docs/usage.md` with step-by-step deployment instructions.
- Convert path building to `path.join()` and add a small unit test suite that mocks `sendCommand` and the Telegram client.
