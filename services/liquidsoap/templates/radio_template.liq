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

radio = fallback([crossfade(radio_queue, duration=crossfade_seconds), blank(duration=blank_seconds)])

# Stream out
output.icecast({{CODEC}},
 host = "{{ICECAST_HOST}}", port = {{ICECAST_PORT}},
 password = "{{ICECAST_PASSWORD}}", mount = "{{ICECAST_MOUNT}}",
 name = "Cold Orbit Radio",
 description = "Interplanetary broadcast station",
 genre = "Various",
 radio
)

# API

def queue_length(_) 
  len = radio_queue.length()
  "#{len}"
end
server.register(
 namespace = "coldorbit",
 description = "Get queue length",
 usage = "length",
 "length",
 queue_length
)

def queue_current(_) 
 cur = null.get(radio_queue.current())
 print(cur)
 cur_uri = null.get(request.uri(cur), default = "")
 "#{cur_uri}"
end
server.register(
 namespace = "coldorbit",
 description = "Get currently playing track",
 usage = "current",
 "current",
 queue_current
)

def queue_push(track_uri) 
 r = request.create(track_uri)
 radio_queue.push(r)
 "#{request.id(r)}"
end
server.register(
 namespace = "coldorbit",
 description = "Push track to queue",
 usage = "push <track_uri>",
 "push",
 queue_push
)