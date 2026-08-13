##############################################################################
# Cold Orbit Radio
# Liquidsoap 2.4.2
##############################################################################

##############################################################################
# Runtime
##############################################################################

set("init.allow_root", true)

set("log.stdout", true)
# set("log.file", true)
# set("log.file.path", "{{LOG}}")
set("log.level", 3)

set("server.telnet", true)
set("server.telnet.port", 1234)
set("server.telnet.bind_addr", "0.0.0.0")

##############################################################################
# Configuration
##############################################################################

crossfade_duration = 3.0
blank_duration = 10.0

icecast_host = "{{ICECAST_HOST}}"
icecast_port = {{ICECAST_PORT}}
icecast_password = "{{ICECAST_PASSWORD}}"
icecast_mount = "{{ICECAST_MOUNT}}"

##############################################################################
# Sources
##############################################################################

queue_source = request.queue(id="coldorbit")

blank_source = blank(id="queue_empty", duration=blank_duration)

##############################################################################
# Playback Pipeline
##############################################################################

playback =
    crossfade(
        queue_source,
        duration=crossfade_duration
    )

radio_output =
    fallback([
        playback,
        blank_source
    ])

##############################################################################
# Outputs
##############################################################################

output.icecast(
    {{CODEC}},

    host=icecast_host,
    port=icecast_port,
    password=icecast_password,
    mount=icecast_mount,

    name="Cold Orbit Radio",
    description="Interplanetary broadcast station",
    genre="Various",

    radio_output
)

##############################################################################
# Webhook
##############################################################################

webhook_url = "{{WEBHOOK_URL}}"

current_track_metadata = ref([])
current_track_uri = ref("")

def metadata_value(track_metadata, key) =
  track_metadata[key] ?? ""
end

def current_uri() =
  uri = null.case(
    queue_source.current(),
    fun() -> "",
    fun(r) -> request.uri(r)
  )

  uri
end

def build_track_payload(event_type, track_metadata, track_uri) = 
  track_artist   = metadata_value(track_metadata, "artist")
  track_title    = metadata_value(track_metadata, "title")
  track_album    = metadata_value(track_metadata, "album")
  track_duration = metadata_value(track_metadata, "duration")
  track_genre    = metadata_value(track_metadata, "genre")

  obj = json()

  obj.add("event", event_type)
  obj.add("timestamp", int_of_float(time()))
  obj.add("artist", track_artist)
  obj.add("title", track_title)
  obj.add("album", track_album)
  obj.add("duration", track_duration)
  obj.add("genre", track_genre)
  obj.add("uri", track_uri)

  obj.stringify(compact=true)
end

def build_system_payload(event_type) = 
  obj = json()

  obj.add("event", event_type)
  obj.add("timestamp", int_of_float(time()))

  obj.stringify(compact=true)
end

def webhook_send(event_type, payload) =
  if webhook_url != "" then
    try
      response =
        http.post(
          webhook_url,
          data=payload,
          headers=[
            ("Content-Type", "application/json"),
            ("User-Agent", "ColdOrbitRadio/Liquidsoap")
          ]
        )

      log("Webhook #{event_type} response: #{response}")
    catch err do
      log("Error sending webhook #{event_type}: #{err}")
    end
  end
end

def notify_track_start(track_metadata) =
  if current_track_metadata() != [] then
    payload = build_track_payload("track_end", current_track_metadata(), current_track_uri())
    webhook_send("track_end", payload)
  end

  uri = current_uri()

  current_track_metadata := track_metadata
  current_track_uri := uri

  payload = build_track_payload("track_start", track_metadata, uri)
  webhook_send("track_start", payload)
end

def notify_track_scrobble(_, _) =
  payload = build_track_payload("track_scrobble", current_track_metadata(), current_track_uri())
  webhook_send("track_scrobble", payload)
end

def notify_queue_empty() =
  payload = build_system_payload("queue_empty")
  webhook_send("queue_empty", payload)
end

queue_source.on_track(synchronous=false, notify_track_start)

queue_source.on_position(position=30., synchronous=false, notify_track_scrobble)

blank_source.on_wake_up(synchronous=false, notify_queue_empty)

##############################################################################
# Internal API
##############################################################################

# Queue length
def queue_length(_) =
  "#{queue_source.length()}"
end
server.register(
  namespace="coldorbit",
  description="Get queue length",
  usage="length",
  "length",
  queue_length
)

# Current track
def queue_current(_) =
  current_uri()
end
server.register(
  namespace="coldorbit",
  description="Get currently playing track",
  usage="current",
  "current",
  queue_current
)

# Push track
def queue_push(uri) =
  queue_source.push.uri(uri)
  "OK"
end
server.register(
  namespace="coldorbit",
  description="Push track to queue",
  usage="push <track_uri>",
  "push",
  queue_push
)

# Skip current track
def queue_skip(_) =
  queue_source.skip()
  "OK"
end
server.register(
  namespace="coldorbit",
  description="Skip current track",
  usage="skip",
  "skip",
  queue_skip
)

# Seconds till next track
def queue_remaining(_) =
  "#{queue_source.remaining()}"
end
server.register(
  namespace="coldorbit",
  description="Seconds till next track",
  usage="remaining",
  "remaining",
  queue_remaining
)

# Elapsed tracks
def queue_elapsed(_) =
  "#{queue_source.elapsed()}"
end
server.register(
  namespace="coldorbit",
  description="Elapsed tracks",
  usage="elapsed",
  "elapsed",
  queue_elapsed
)

# Current track duration
def queue_duration(_) =
  "#{queue_source.duration()}"
end
server.register(
  namespace="coldorbit",
  description="Current track duration",
  usage="duration",
  "duration",
  queue_duration
)
