const axios = require("axios")

const SERVICES = {
    tgfetch: process.env.TGFETCH_URL || "http://tgfetch:4001",
    rotation: process.env.ROTATION_URL || "http://rotation:4002",
    liquidsoap: process.env.LIQUIDSOAP_URL || "http://liquidsoap:4000",
    icecast: process.env.ICECAST_URL || "http://icecast:8000"
}

const MOUNT_POINT = process.env.MOUNT_POINT || '/radio.mp3'

const REQUEST_TIMEOUT = 5000            //5s

const callService = async (serviceName, endpoint, options = {}) => {
    const baseUrl = SERVICES[serviceName]
    if (!baseUrl) throw new Error("Service unavailiable")
    
    try {
        const response = await axios({
            url: `${baseUrl}/${endpoint}`,
            method: options.method || "GET",
            data: options.data,
            params: options.params,
            timeout: REQUEST_TIMEOUT
        })

        return response.data
    }
    catch (error) {
        console.error(`Error while calling service ${serviceName} with endpoint ${endpoint}`)
        throw new Error(`Service ${serviceName} unavailable`)
    }
}

let cache = {
    data: null,
    timestamp: 0,
    ttl: REQUEST_TIMEOUT
}

getIcecastData = async () => {
    const now = Date.now()
    if (cache.data && (now - cache.timestamp) < cache.ttl) {
        return cache.data;
    }

    try {

        icecast_data = await callService("icecast", "status-json.xsl")

        cache.data = icecast_data
        cache.timestamp = now

        return icecast_data
    } catch (error) {
        console.error('Icecast request error', error.message);
        return cache.data || null;
    }
}

getStationInfo = async () => {
    const icecastData = await getIcecastData()

    if (!icecastData || !icecastData.icestats) {
        return null;
    }

    const source = icecastData.icestats.source
    if (Array.isArray(source)) {
        source = source.find(s =>
            s.listenurl && s.listenurl.includes(MOUNT_POINT)
        ) || source[0]
    }

    if (!source) {
        return null;
    }

    return {
        name: source.server_name || 'Cold Orbit Radio',
        description: source.server_description || 'Online Radio Station',
        genre: source.genre || 'Various',
        url: source.listenurl || `${SERVICES.icecast}${MOUNT_POINT}`,
        bitrate: source.bitrate || 192,
        format: source.server_type || 'audio/mpeg',
        currentSong: source.title || 'No song playing',
        currentArtist: source.title?.split(" - ")[0].trim() || 'Unknown Artist',
        listeners: source.listeners || 0,
        peakListeners: source.listener_peak || 0,
        streamStart: source.stream_start || null,
        nowPlaying: {
            title: source.title?.split(" - ")[0].trim(),
            artist: source.title?.split(" - ")[1].trim(),
            duration: null,
            started: null
        }
    }
}

listenersNow = async () => {
    const stationInfo = await getStationInfo()
    return { listeners: stationInfo.listeners || 0 }
}

module.exports = {
    getNowPlaying: () => callService("rotation", "nowPlaying"),
    getTrackCover: (trackId) => callService("tgfetch", "cover", { params: { trackId } }),
    getStationInfo: () => getStationInfo(),
    getListenersNow: () => listenersNow(),
    checkAllServices: async () => {
        const results = {};

        for (const [name, url] of Object.entries(SERVICES)) {
            try {
                await axios.get(url + `/${name === "icecast" ? "status-json.xsl" : "health"}`, { timeout: 3000 });
                results[name] = { status: 'OK', url };
            } catch (error) {
                results[name] = { status: 'ERROR', url, error: error.message };
            }
        }

        return results;
    }
}
