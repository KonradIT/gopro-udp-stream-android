## What's this?

This is an ongoing effort to programmatically get GoPro HERO5's UDP video and audio stream and play it using a video player library on Android.
Tested so far:
- FFmpeg udp://:8554 ==> 127.0.0.1 && LibVLC: Works but as the data is received the lag increases
- FFmpeg udp://:8554 ==> 127.0.0.1 && ExoPlayer: Does not recognize udp protocol
- FFmpeg udp://:8554 ==> 127.0.0.1 && Vitamio: Error with library