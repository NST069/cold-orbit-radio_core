const axios = require("axios")

const TG_FETCH_URL = process.env.TG_FETCH_URL || "http://localhost:4001"

async function requestWithRetry(url, options = {}, retries = 5, delay = 1000) {
    for (let i = 0; i < retries; i++) {
        try {
            //const health = await axios({url: `${TG_FETCH_URL}/health`})
            //if(health.data.status !== "ok") throw "Helath not ok"
            const res = await axios({ url, ...options });
            console.log(res.data)
            return res.data;
        } catch (err) {
            if (i === retries - 1) throw err;
            console.warn(`Request failed, retrying in ${delay}ms...`);
            console.log(err.code, err.config.url)
            await new Promise(r => setTimeout(r, delay));
            delay *= 2; // экспоненциальная задержка
        }
    }
}

exports.tgFetchClient = {
    async getNextTrack() {
        const res = await requestWithRetry(`${TG_FETCH_URL}/api/next-track`, { method: "GET" });
        return res.data;
    },

    async getQueueLength() {
        const res = await requestWithRetry(`${TG_FETCH_URL}/api/queue-length`, { method: "GET" });
        return res.data;
    },

    async getTrackTitle(fileName) {
        const res = await requestWithRetry(`${TG_FETCH_URL}/api/track-title`, { method: "GET", data: { fileName } });
        return res.data;
    },

    async markTrackAsPlayed(fileName) {
        await requestWithRetry(`${TG_FETCH_URL}/api/mark-played`, { method: "POST", data: { fileName } });
    },

    async scheduleTrack(fileName) {
        await requestWithRetry(`${TG_FETCH_URL}/api/mark-scheduled`, { method: "POST", data: { fileName } });
    }
};