let map;
let markers = [];
let allNetworks = [];
const categoryIcons = {
  None: "https://maps.google.com/mapfiles/ms/icons/red-dot.png",
  WEP: "https://maps.google.com/mapfiles/ms/icons/orange-dot.png",
  Unknown: "https://maps.google.com/mapfiles/ms/icons/ltblue-dot.png",
  WPA: "https://maps.google.com/mapfiles/ms/icons/yellow-dot.png",
  WPA2: "https://maps.google.com/mapfiles/ms/icons/blue-dot.png",
  WPA3: "https://maps.google.com/mapfiles/ms/icons/green-dot.png",
};

function initMap() {
  const centerCoords = { lat: 31.876423, lng: -102.3185 };
  // Initialize the map
  map = new google.maps.Map(document.getElementById("map"), {
    zoom: 15.2,
    center: centerCoords,
    mapId: "86c89399e7a5afc8",
  });

  console.log("Map initialized successfully.");

  // Load data from wifi_data.json and initialize markers
  loadNetworkData();

  // Coordinates for the four corners of the 1-mile by 1-mile box
  const boxCoords = [
    { lat: 31.879479, lng: -102.329488 }, // Top-left
    { lat: 31.883428, lng: -102.313596 }, // Top-right
    { lat: 31.874085, lng: -102.310145 }, // Bottom-right
    { lat: 31.86676, lng: -102.32564 }, // Bottom-left
  ];

  // Create and display the polygon
  const boxPolygon = new google.maps.Polygon({
    paths: boxCoords,
    strokeColor: "#FF0000", // Red outline
    strokeOpacity: 1,
    strokeWeight: 1,
    fillColor: "pink", // Semi-transparent pink
    fillOpacity: 0.2,
  });
  boxPolygon.setMap(map);

  // Add all markers initially
  showAllNetworks();
}

// Load the JSON file dynamically
async function loadNetworkData() {
  try {
    const response = await fetch("wifi_data.json");
    if (!response.ok) {
      throw new Error("Failed to load JSON file");
    }

    // Parse the JSON data
    allNetworks = await response.json();

    // Automatically display all networks after loading data
    showAllNetworks();
  } catch (error) {
    console.error("Error loading network data:", error);
  }
}

// Fetch the parsed CSV file, then add all markers to the map
function showAllNetworks() {
  console.log("Showing all networks...");

  // Clear existing markers
  clearMarkers();

  // Call `showCategory` for each category
  for (const category in data) {
    if (data.hasOwnProperty(category) && Array.isArray(data[category])) {
      showCategory(category);
    }
  }

  // Add networks from `allNetworks` (wifi_data.json)
  allNetworks.forEach((item) => {
    const encryptionMatch = item.description?.match(/Encryption:\s*(\w+)/);
    const encryptionType = encryptionMatch
      ? encryptionMatch[1].trim()
      : "Unknown";

    // Get the icon URL for the encryption type
    const iconUrl = categoryIcons[encryptionType] || categoryIcons["Unknown"];
    //console.log(`Encryption: ${encryptionType}, Icon URL: ${iconUrl}`); // Debugging

    // Create the marker
    const marker = new google.maps.Marker({
      position: {
        lat: parseFloat(item.Y),
        lng: parseFloat(item.X),
      },
      map: map,
      title: item.Name,
      icon: {
        url: iconUrl,
        scaledSize: new google.maps.Size(15, 15),
      },
    });

    const infoWindow = new google.maps.InfoWindow({
      content: `<div>
        <strong>${item.Name}</strong>
        <p>${item.description?.replace(/\n/g, "<br>")}</p>
      </div>`,
    });

    marker.addListener("click", () => {
      infoWindow.open(map, marker);
    });

    markers.push(marker);
  });
}

function showCategory(category) {
  //console.log(`Showing category: ${category}`);

  // Clear existing markers
  clearMarkers();

  // Get networks for the selected category
  const filteredNetworks = data[category] || [];

  //console.log(`Filtered Networks Count: ${filteredNetworks.length}`); // Debugging

  // Add markers for the filtered networks
  filteredNetworks.forEach((item) => {
    const iconUrl = categoryIcons[item.Encryption] || categoryIcons["Unknown"];

    const marker = new google.maps.Marker({
      position: {
        lat: parseFloat(item.Y),
        lng: parseFloat(item.X),
      },
      map: map,
      title: item.Name,
      icon: {
        url: iconUrl,
        scaledSize: new google.maps.Size(15, 15),
      },
    });

    const infoWindow = new google.maps.InfoWindow({
      content: `<div>
        <strong>${item.Name}</strong>
        <p>Encryption: ${item.Encryption}</p>
      </div>`,
    });

    marker.addListener("click", () => {
      infoWindow.open(map, marker);
    });

    markers.push(marker);
  });
}

// Clear existing markers from the map
function clearMarkers() {
  markers.forEach((marker) => marker.setMap(null));
  markers = [];
}
