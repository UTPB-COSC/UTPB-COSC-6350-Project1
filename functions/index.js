const functions = require('firebase-functions');

exports.getGoogleMapsApiKey = functions.https.onRequest((req, res) => {
    console.log("Fetching API key...");
    const apiKey = functions.google.mapsapikey?.trim();
    if (!apiKey) {
        console.error("API key not found!");
        return res.status(500).send({ error: "API key not found" });
    }
    console.log("API key fetched successfully:", apiKey);
    res.status(200).send({ apiKey });
});

