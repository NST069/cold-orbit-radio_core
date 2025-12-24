# Settings
set("init.allow_root", true)
set("log.stdout", true)
set("log.level", 3)
set("log.file", true)
set("log.file.path", "{{LOG}}")

# Telnet
set("server.telnet", true)
set("server.telnet.port", 1234)
set("server.telnet.bind_addr", "0.0.0.0")

# Queue
radio_queue = request.queue(id="coldorbit")

# Tuning variables
crossfade_seconds = 3.0
blank_seconds = 10.0

# Webhook
webhook_url = "{{WEBHOOK_URL}}"

current_track_metadata = ref([])

def notify_webhook(event_type, metadata) =
  if
    webhook_url != ""
  then
    artist = metadata["artist"] ?? "Unknown"
    title = metadata["title"] ?? "Unknown"
    album = metadata["album"] ?? ""
    duration = metadata["duration"] ?? "0"

    data ="event=#{event_type}" ^ "&artist=" ^ artist ^ "&title=" ^ title ^
        "&album=" ^
        album ^
        "&duration=" ^
        duration ^
        "&timestamp=" ^
        "#{int_of_float(time())}"

    thread.run(
      fun () ->
        try
          response = http.post(webhook_url, data=data)
          log("Webhook #{event_type} response: #{response}")
        catch error do
          log("Error sending webhook #{event_type}: #{error}")
        end
    )
  else
    log("Webhook URL not configured")
  end
end

def notify_track_start(m) =
  if !current_track_metadata != [] then
    log("Sending track_end for previous track")
    notify_webhook("track_end", !current_track_metadata)
  end
  
  log("Track started: #{m['artist'] ?? 'Unknown'} - #{m['title'] ?? 'Unknown'}")
  
  current_track_metadata := m
  
  notify_webhook("track_start", m)
end

radio_queue.on_track(notify_track_start)

crossfade_radio = crossfade(radio_queue, duration=crossfade_seconds)

# Libre.fm scrobbling
crossfade_radio =
  librefm.submit.full(
    user="{{LIBREFM_USER}}",
    password="{{LIBREFM_PASSWORD}}",
    source="broadcast",
    length=true,
    delay=10.,
    force=true,
    crossfade_radio
  )

radio = fallback([crossfade_radio, blank(duration=blank_seconds)])

# Stream out
output.icecast(
  {{CODEC}},
  host="{{ICECAST_HOST}}",
  port={{ICECAST_PORT}},
  password="{{ICECAST_PASSWORD}}",
  mount="{{ICECAST_MOUNT}}",
  name=
    "Cold Orbit Radio",
  description=
    "Interplanetary broadcast station",
  genre="Various",
  radio
)

# API

def queue_length(_) =
  len = radio_queue.length()
  "#{len}"
end
server.register(
  namespace="coldorbit",
  description=
    "Get queue length",
  usage="length",
  "length",
  queue_length
)

def queue_current(_) =
  cur = null.get(radio_queue.current())
  print(cur)
  cur_uri = null.get(request.uri(cur), default="")
  "#{cur_uri}"
end
server.register(
  namespace="coldorbit",
  description=
    "Get currently playing track",
  usage="current",
  "current",
  queue_current
)

def queue_push(track_uri) =
  r = request.create(track_uri)
  radio_queue.push(r)
  "#{request.id(r)}"
end
server.register(
  namespace="coldorbit",
  description=
    "Push track to queue",
  usage=
    "push <track_uri>",
  "push",
  queue_push
)
