const net = require("net")

/**
 * Отправляет команду Liquidsoap через telnet-интерфейс.
 * Открывает соединение, ждёт ответ, закрывает.
 *
 * @param {string} cmd - команда Liquidsoap, например 'coldorbit.current'
 * @param {object} [options]
 * @param {string} [options.host='127.0.0.1']
 * @param {number} [options.port=1234]
 * @param {number} [options.timeout=5000] - таймаут в мс
 * @returns {Promise<string>} - текст ответа без 'END'
 */
exports.sendCommand = (cmd, options = {}) => {
  const {
    host = "127.0.0.1",
    port = 1234,
    timeout = 5000
  } = options

  return new Promise((resolve, reject) => {
    const client = net.createConnection({ host, port })
    let buffer = ""

    const cleanup = (err) => {
      client.destroy()
      if (err) reject(err)
    }

    client.setEncoding("utf8")
    client.setTimeout(timeout, () => cleanup(new Error("Timeout")))

    client.on("connect", () => {
      client.write(cmd.trim() + "\n")
    })

    client.on("data", (chunk) => {
      buffer += chunk
      if (buffer.includes("END")) {
        const result = buffer.replace(/END\s*$/, "").trim()
        client.end()
        resolve(result)
      }
    })

    client.on("error", cleanup)
    client.on("end", () => {
      if (!buffer.includes("END"))
        resolve(buffer.trim()) // fallback, если сервер не вернул END
    })
  })
}
