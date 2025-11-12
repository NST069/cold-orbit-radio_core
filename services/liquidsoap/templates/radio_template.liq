# Settings
settings.init.allow_root := true
settings.log.stdout := false
settings.log.level := 3
settings.log.file := true
settings.log.file.path := "{LOG}"

# Telnet
settings.server.telnet := true
settings.server.telnet.port := 1234

# Queue
radio_queue = request.queue(id="coldorbit")

# Tuning variables
crossfade_seconds = 3.0
blank_seconds = 10.0

radio = fallback([crossfade(radio_queue, duration=crossfade_seconds), blank(duration=blank_seconds)])

# Stream out
output.icecast(%ffmpeg(format="mp3", %audio(codec="libmp3lame")),
 host = "localhost", port = 8000,
 password = "{PWD}", mount = "radio.mp3",
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