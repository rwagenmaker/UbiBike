# UbiBike

UbiBike is a mobile application that allows cyclists to earn and share points as they cycle. Cyclists earn points by cycling (more traveled distance translates into more earned points). Additionally, cyclists can:

- send/receive points to/from other cyclists;
- send/receive text messages to/from other cyclists

Communication between cyclists (sharing points and exchanging text messages) is done through WiFi Direct. The target platform for UbiBike is Android version >= 4.0.

The UbiBike application is responsible for tracking the trajectory (using GPS) while cycling. The tracking starts when the user approaches the bike. Using GPS, UbiBike tracks the distance (and trajectory) traveled using the bike and stops when the user moves away from the bike. Each bike has attached a BLE (Bluetooth Low Energy) beacon sticker, which allows UbiBike to detect when the user is using a (specific) bike or not. Bikes are picked and dropped at parking stations. All users must register (using UbiBike) in a central server. The server knows about all users currently registered in the system, including their current score (total number of points) and trajectories. This data (score and trajectories) is opportunistically uploaded by the UbiBike app (running on users' device) to the central server. Additionally, by contacting the central server, the mobile application can be used to book an available bike from a particular parking station.

The server knows which bikes are available at each parking station because the UbiBike app informs the server whenever a bike arrives or leaves the station. To detect such events automatically, the app can use heuristics based on GPS readings and BLE beacons sensing. The app running on a cyclist’s device can detect a bike pickup event by comparing the current GPS location of the device with the GPS coordinates of the parking station where the bike is stationed, and perform subsequent GPS readings in order to detect that the device is moving away from the parking station. To ensure that the cyclist is riding the bike (and not simply walking away), the application is constantly sense the wireless signal of the bike’s BLE beacon. To detect a bike drop-off event, the reasoning is similar except that instead of detecting that both device and beacon are departing from the parking station, the application must detect that they are approaching a parking station and eventually get immobilized, and the user moves away from the bike.

The application supports the following interactions:

- Between mobile devices (using WiFi Direct)

  - Send and receive points
  - Send and receive text messages

- Between mobile devices and the central server

  - Register user
  - Send new trajectory
  - Show most recent and past trajectories on a map
  - Get user information (including current score and trajectories) 
  - Get list of stations with available bikes to book
  - Book bike at specific station (while showing their location on a map)

- Between mobile devices, BLE beacons, and the central server 
  - Notify bike pick up
  - Notify bike drop off
  
- Between the user and the bike being used (using BLE)
  - Detect what bike is being used by which user
