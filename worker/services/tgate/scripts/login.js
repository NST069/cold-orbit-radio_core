require('dotenv').config()
const client = require("../src/telegram/client")

async function main() {
    await client.login({
        async getPhoneNumber(retry) {
            if (retry) {
                throw new Error('Invalid phone number')
            }

            return process.env.TELEGRAM_PHONE
        }
    })

    console.log('Session created')

    process.exit(0)
}

main()
