# Android-Voice-Sensor
Android Voice Sensor make to audio file and do STT
- 마이크를 실시간으로 감지하여 볼륨이 클 때 목소리를 감지하여 음성 녹음
- 녹음된 음성은 서버로 전송되어 Google Speech to Text api를 통해 Text로 변환
- 변환된 Text는 android 앱으로 다시 전송되어 처리

# Tech Stacks
- Google Speech to Text
- [AndroidAudioConverter](https://github.com/adrielcafe/AndroidAudioConverter)
- [ffmpeg-android](https://github.com/WritingMinds/ffmpeg-android)
- Vert.x
- aquery
