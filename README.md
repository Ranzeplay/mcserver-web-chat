# MCServer Web Chat

A Minecraft Mod that enables players to chat both in web browsers and in-game.

## Features

- Chat in-game and through web browsers simultaneously
- User authentication with OTP for new users
- Persistent chat history
- Works as a standalone mod or as a BlueMap addon

## Installation

### Standalone Mod

1. Download the mod JAR for your platform (Fabric or NeoForge) from the releases page
2. Place the JAR file in your `mods` folder
3. Start your Minecraft server
4. Access the web chat at `http://your-server-ip:8080`

### BlueMap Addon

If you have [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap) installed:

1. Install the standalone mod as described above
2. Download the BlueMap addon JAR from the releases page
3. Place the addon JAR in your BlueMap addons folder (typically `config/bluemap/addons/`)
4. Restart your server
5. The web chat will now integrate with BlueMap's web interface

**Note:** The mod works perfectly fine without BlueMap. The addon is optional and only provides enhanced integration if BlueMap is present.

## Configuration

See [CONFIGURATION.md](CONFIGURATION.md) for detailed configuration options.

## Usage

### As a Standalone Mod
The web chat server runs on port 8080 by default. Players can access it via their web browser while playing on the server.

### With BlueMap Integration
When used as a BlueMap addon, the web chat seamlessly integrates with BlueMap's web interface, allowing players to view the map and chat simultaneously.

