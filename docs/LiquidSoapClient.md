# LiquidSoapClient (util/LiquidSoapClient.js)

Purpose

Small helper that sends commands to Liquidsoap's telnet server and returns the textual response.

Location

`util/LiquidSoapClient.js`

Public API

- `sendCommand(cmd, options)` -> Promise<string>
 - `waitForTelnet(host, port, timeoutMs, interval)` -> Promise<boolean>

The module also exposes a small helper `waitForTelnet` which waits until a TCP port accepts connections (used to avoid ECONNREFUSED while Liquidsoap starts).

Parameters

- `cmd` (string) — the Liquidsoap command to send, for example `coldorbit.current` or `coldorbit.push <uri>`.
- `options` (object, optional):
  - `host` (string) — telnet host (default `127.0.0.1`)
  - `port` (number) — telnet port (default `1234`)
  - `timeout` (number) — milliseconds to wait for a response (default `5000`)

Behavior and return value

The function opens a TCP connection, sends the command (with a terminating newline) and waits for the response. Liquidsoap telnet responses in this project are expected to end with the string `END` — the client strips a trailing `END` and returns the trimmed text. If the socket ends without `END` the buffer is returned as a fallback.

Example usage

```js
const { sendCommand } = require('./util/LiquidSoapClient')

// get queue length
const length = await sendCommand('coldorbit.length')

// push a track file URI
await sendCommand(`coldorbit.push ${trackUri}`)
```

Notes

- Uses Node's `net` module; no external dependencies.
- Caller should handle errors (promise rejection) and may choose to set a larger timeout for long operations.
 - `waitForTelnet(host, port, timeoutMs)` returns true if the port became available before timeout, false otherwise.

Example: wait for Liquidsoap telnet before proceeding

```js
const { waitForTelnet } = require('./util/LiquidSoapClient')
const ok = await waitForTelnet('127.0.0.1', 1234, 15000)
if (!ok) console.error('Liquidsoap telnet not ready')
```
