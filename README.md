# nautilus

### Building

To use this plugin, run `./gradlew build` (or `./gradlew.bat build` on Windows).

### Development

To start a development server, run `./gradlew runServer` (or `./gradlew.bat runServer` on Windows).

If you'd rather set up your own server and automatically copy the built jar to the server, you can set up a new command
with a parameter:
1. Copy your server's plugin directory path
2. Execute `./gradlew build -PnautilusOut="<path here>"`
3. (Optional) Use Paper's `update` folder by setting the path to `plugins/update` so that the previous jar won't be
accidentally overridden while the server is running, but instead moved upon server (re)boot.