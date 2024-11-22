//map: https://nataliacs25.github.io/War-Driving/wifi_map.html

import java.io.*;
import java.util.*;

public class WiFiMap {
    public static void main(String[] args) {
       
        String filePath = "C:/Users/natal/OneDrive/Desktop/Wireless security/WigleWifi_FinalData.csv";
       
        List<Coordinate> coordinates = readCoordinates(filePath);
        
        // Check if coordinates were read
        if (coordinates.isEmpty()) {
            System.out.println("No coordinates found in the CSV file.");
        } else {
            System.out.println("Successfully read " + coordinates.size() + " coordinates.");
        }
        
        String html = generateHTML(coordinates);
        saveHTML("wifi_map.html", html);
    }
    
    // Method to read the CSV file
    public static List<Coordinate> readCoordinates(String file) {
    	List<Coordinate> coordinates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        	String line;
            String[] headers = null;

            // Read the first line containing the headers
            if ((line = br.readLine()) != null) {
                headers = line.split(",");
            }

            // Check if headers are present
            if (headers != null) {
                // Read the following lines
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");

                    // Map the data to columns using the headers
                    double latitude = 0.0;
                    double longitude = 0.0;
                    String securityType = "";
                    String mac = "";
                    String ssid = "";
                    String type = "";

                    // Map each column based on the header name
                    for (int i = 0; i < headers.length; i++) {
                        String header = headers[i].trim();

                        // Mapping by column name
                        if ("CurrentLatitude".equalsIgnoreCase(header)) {
                            latitude = Double.parseDouble(data[i].trim());
                        } else if ("CurrentLongitude".equalsIgnoreCase(header)) {
                            longitude = Double.parseDouble(data[i].trim());
                        } else if ("AuthMode".equalsIgnoreCase(header)) {
                            securityType = data[i].trim(); // AuthMode
                        } else if ("MAC".equalsIgnoreCase(header)) {
                            mac = data[i].trim();
                        } else if ("SSID".equalsIgnoreCase(header)) {
                            ssid = data[i].trim();
                        } else if ("Type".equalsIgnoreCase(header)) {
                            type = data[i].trim();
                        }
                    }
                    
                    // Classify the security type
                    String securityClassification = classifySecurity(securityType);
                    
                    // Add the coordinate to the list
                    coordinates.add(new Coordinate(latitude, longitude, securityClassification, mac, ssid, type));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return coordinates;
    }
    
    // Method to classify the security type into Open, WEP, WPA, WPA2, WPA3/WPA2 EAP Only
    public static String classifySecurity(String securityType) {

    	// Convert the string to lower case
        securityType = securityType.toLowerCase();

        // If it contains WEP, classify it as WEP
        if (securityType.contains("wep")) {
            return "WEP";
        }

        // If it contains any WPA keys, but not WPA2 or WPA3, classify it as WPA
        if (securityType.contains("wpa") && !securityType.contains("wpa2") && !securityType.contains("wpa3")) {
            return "WPA";
        }

            
        // If it contains WPA2 or RSN, classify it as WPA2
        if (securityType.contains("wpa2") || securityType.contains("rsn")) {
            return "WPA2";
        }

        // If it contains WPA3 or EAP, classify it as WPA3/WPA2 EAP Only
        if (securityType.contains("wpa3") || securityType.contains("eap")|| securityType.contains("sae") || securityType.contains("ccmp")) {
            return "WPA3/WPA2 EAP Only";
        }
        

        // If it only contains ESS, classify it as Open
        if (securityType.contains("ess")|| securityType.contains("wps")) {
            return "Open";
        }

        // If it doesn't match any of the above, classify it as Unknown
        return "Unknown";
    }

    public static void countSecurityTypes(List<Coordinate> coordinates) {
        Map<String, Integer> securityCount = new HashMap<>();
        

        for (Coordinate c : coordinates) {
            securityCount.put(c.securityType, securityCount.getOrDefault(c.securityType, 0) + 1);
        }
        

        System.out.println("Network by security type:");
        for (Map.Entry<String, Integer> entry : securityCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Generate the HTML for Google Maps
    public static String generateHTML(List<Coordinate> coordinates) {
    	StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
        	.append("<html lang=\"en\"><head><meta charset=\"UTF-8\">")
        	.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>WiFi Network Map</title>")
            .append("<script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyA5bE08DJczUk98-vbtqWguHNOSwyDzt2E\" async defer></script>")
            //.append("<script src=\"https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js\"></script>")
            .append("<style>")
            .append("html, body {")
            .append("height: 100%; margin: 0;")
            .append("}")
            .append("#map {")
            .append("height: 100%; width: 100%;")
            .append("}")
            .append("</style>")
            .append("</head><body>")
            .append("<div id=\"map\"></div>");
        
        html.append("<script type=\"text/javascript\">")
        	.append("let map, markerCluster;")  // Define the map and markerCluster globally
        	.append("let essLayer = [];")  // ESS Layer
            .append("let wepLayer = [];")  // WEP Layer
            .append("let wpaLayer = [];")  // WPA Layer
            .append("let allMarkers = {};")  // Store all markers
            .append("function initMap() {")
            .append("map = new google.maps.Map(document.getElementById('map'), {")
            .append("center: {lat: 31.9973, lng: -102.0779}, zoom: 12")
            .append("});")
            .append("const markers = [];");  // Array of markers

        // Add markers for each WiFi network
        for (Coordinate c : coordinates) {
            String markerColor = getColorBySecurity(c.securityType);
            
            // Debugging the coordinates of each marker
            html.append("console.log('Creando marcador en: ' + ").append(c.latitude).append(" + ', ' + ").append(c.longitude).append(");");

            html.append("var marker = new google.maps.Marker({")
                .append("position: {lat: ").append(c.latitude).append(", lng: ").append(c.longitude).append("},")
                .append("map: map,")
                .append("title: '").append(c.securityType).append("',")
                .append("icon: {")
                .append("path: google.maps.SymbolPath.CIRCLE,")
                .append("fillColor: '").append(markerColor).append("',")
                .append("fillOpacity: 0.8,")
                .append("strokeColor: '").append(markerColor).append("',")
                .append("strokeOpacity: 0.8,")
                .append("strokeWeight: 2,")
                .append("scale: 6}")
                .append("});");
            
            // Add the marker to the corresponding layers
            html.append("if ('").append(c.securityType).append("' === 'ESS') {")
                .append("essLayer.push(marker);")
                .append("} else if ('").append(c.securityType).append("' === 'WEP') {")
                .append("wepLayer.push(marker);")
                .append("} else if ('").append(c.securityType).append("' === 'WPA') {")
                .append("wpaLayer.push(marker);")
                .append("}");
            
            html.append("markers.push(marker);");

            // Create Infowindow with network information
            html.append("var infowindow = new google.maps.InfoWindow({")
	            .append("content: '<div><strong>MAC: </strong>").append(c.mac.replace("'", "\\'")).append("<br>")
	            .append("<strong>SSID: </strong>").append(c.ssid.replace("'", "\\'")).append("<br>")
	            .append("<strong>AuthMode: </strong>").append(c.securityType.replace("'", "\\'")).append("<br>")
	            .append("<strong>Latitude: </strong>").append(c.latitude).append("<br>")
	            .append("<strong>Longitude: </strong>").append(c.longitude).append("<br>")
	            .append("<strong>Type: </strong>").append(c.type.replace("'", "\\'")).append("</div>'")
	            .append("});");

	            
            	// Associate infowindow with marker
	            html.append("marker.addListener('click', (function(marker, infowindow) {")
	                .append("    return function() {") // Click function
	                .append("        var position = marker.getPosition();") // Get marker position
	                .append("        console.log('Centrando el mapa en: ' + position.lat() + ', ' + position.lng());") // Print coordinates
	                .append("        map.setCenter(position);") // Centralize the map in the marker
	                .append("        map.setZoom(15);")
	                .append("        infowindow.open(map, marker);") // Show infowindow
	                .append("    };")
	                .append("})(marker, infowindow));");  // Click to access each marker
        }
        
        // Clusters for each layer
        html.append("markerClusterESS = new MarkerClusterer(map, essLayer, {")
            .append("imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});")
            .append("markerClusterWEP = new MarkerClusterer(map, wepLayer, {")
            .append("imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});")
            .append("markerClusterWPA = new MarkerClusterer(map, wpaLayer, {")
            .append("imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});");

        // Control for each layer
        html.append("var layerControlDiv = document.createElement('div');")
            .append("var layerControl = new google.maps.ControlPosition();")
            .append("layerControlDiv.style.padding = '5px';")
            .append("layerControlDiv.innerHTML = '<div><button onclick=\"toggleLayer(essLayer, markerClusterESS)\">Toggle ESS</button><button onclick=\"toggleLayer(wepLayer, markerClusterWEP)\">Toggle WEP</button><button onclick=\"toggleLayer(wpaLayer, markerClusterWPA)\">Toggle WPA</button></div>';")
            .append("map.controls[google.maps.ControlPosition.TOP_RIGHT].push(layerControlDiv);");

        // Function to change the visibility of the layer
        html.append("function toggleLayer(layer, markerCluster) {")
            .append("    if (markerCluster.getMarkers()[0].getMap() === null) {")  // hidden markers
            .append("        markerCluster.addMarkers(layer);")
            .append("    } else {")
            .append("        markerCluster.removeMarkers(layer);")
            .append("    }")
            .append("}");

        html.append("}");; // End InitMap

        // Call initMap once the map is loaded
        html.append("window.onload = function() {")
            .append("initMap();")
            .append("};");
        
        // Call initMap after the scripts load
        html.append("</script>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
    
    // Marker color depending on the security type
    public static String getColorBySecurity(String securityType) {
    	switch (securityType) {
        case "WPA3/WPA2 EAP Only":
            return "blue";   // blue for WPA3/WPA2 EAP Only
        case "WPA2":
            return "green";  // Green for WPA2
        case "WPA":
            return "orange"; // Orange for WPA
        case "WEP":
            return "purple";    // Purple for WEP
        case "Open":
            return "red";   // Red for Open
        default:
            return "gray";   // Gray for Unknown
    	}
    }

    // Method to save the HTML file
    public static void saveHTML(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            writer.write(content);
            System.out.println("File successfully saved at: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Coordinate {
    double latitude;
    double longitude;
    String securityType;
    String mac;
    String ssid;
    String type;

    public Coordinate(double latitude, double longitude, String securityType,String mac, String ssid, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.securityType = securityType;
        this.mac = mac;
        this.ssid = ssid;
        this.type = type;
    }
}
