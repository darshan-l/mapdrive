package com.example.mapdrive.Models;

import android.location.Location;

public class SavedLocation {
    // can be stored and retrieved through FirebaseAuth
    // get/setLong; get/setLat
    private android.location.Location Location;
    public SavedLocation(android.location.Location location) {
        this.Location = location;
    }
    public android.location.Location getLocation() {
        return Location;
    }
}
