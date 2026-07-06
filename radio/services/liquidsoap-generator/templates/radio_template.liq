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

# webhook_url = "{{WEBHOOK_URL}}"

icecast_host = "{{ICECAST_HOST}}"
icecast_port = {{ICECAST_PORT}}
icecast_password = "{{ICECAST_PASSWORD}}"
icecast_mount = "{{ICECAST_MOUNT}}"

##############################################################################
# Sources
##############################################################################

queue_source =
    request.queue(id="coldorbit")

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
        blank(duration=blank_duration)
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
  q = queue_source.queue()
  if list.length(q) == 0 then
    ""
  else
    request.uri(list.hd(q))
  end
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
