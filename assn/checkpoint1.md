# Preliminiary UI Design

## App Name and Features
- app name: `Inventory Manager`
- group members: `[Patrick Miller]`
- advanced features:
  - Barcode Scanner
  - Network Api
  - GPS/Google maps API (tentative)
  - Bluetooth (tentative)
  - Offline first/data caching (tentative)

## App proposal

This app is part of a project I am workin on at work to implement barcodes for our raw material tracking. The idea is to have a mobile app that will facilitate the inventory features mentioned below. There will be a backend server(with a database) that will require a network connection to fetch and push data to. At the moment, the idea is to use Android's ML kit to scan and detect PDF417 formatted barcodes. Tentative features (geo-location, bluetooth for external barcode scanners, and caching of the database and scans in case of a network drop) will be implimented if time permits.

The app will contian at least 3 activities. The settings fragment will contain configuration for connecting to the backend server as well as other settings (default activity, dark/light theme, auto-scan function switch). The main activity will include buttons for each of the inventory features mentioned below. Each of these buttons may take the user to a different activity or just the scan activity with the functionality to facilitate known in the background to modify the scan activity's behavior. Finally, the scan activity will be a simple activity with a camera preview of the scanner and a manual scan button.

### Inventory features to facilitate
- Querying inventory (fetching data for a given material)
- Moving inventory (re-assigning a storage location)
- Issuing barcode tags to a given inventory
- Taking physical inventory
