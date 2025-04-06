# Iperf3 Mobile Application

## Overview

The Iperf3 mobile application is a network performance measuring tool for Android. 
It allows users to test the bandwidth, delay, jitter, and packet loss of their network connections.
The application is written in Kotlin and uses Jetpack Compose with Material3.

## Features

- **Command line use**: The application run based on the provided command from the user. The user only needs to provide the Iperf3 options for the measurements. Basically the 'Iperf3' command is a non-displayed prefix.
- **Client or Host**: The application can be used as client to another server or as server, but it was never created to be a server app in mind.
- **Last command**: The application remembers the last command the user run. The help command is excluded.
- **History**: Keeps a history of successful measurements along with the command and a timestamp.
- **Help command**: Use the help option to view the Iperf3 help without erasing the command.


## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/georgehinop/Iperf3_Mobile_App.git
   ```
2. Open/Import the Iperf3 folder with Android Studio

3. Install the application directly to your phone or build an apk


## Future Work

- Add favorite server list along with a description.
- Provide a graph with the measurements.
- Add ability to export measurements as txt.

## Contributing

Any ideas or contribution is welcome.

