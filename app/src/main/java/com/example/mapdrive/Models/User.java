package com.example.mapdrive.Models;

// User POJO for username/password and other attributes needed to be stored in the database
// Adding extra attributes through a firestore object instead of appending the firebase auth properties (claims)
// https://firebase.google.com/docs/auth/admin/custom-claims#best_practices_for_custom_claims
// docs say it is best practice to to not overwrite the claims if need to store data thats not involved in auth

// Making this Model a Singleton to ensure that

import java.util.List;

public class User {
    private String Username;
    private String PreferredUnit;
    private String PreferredLandmark;
    //WIP
    // should be nested within the User Document so there's no need to try can create some kind of relational model
    private List<SavedLocation> SavedLocations;
    //User Id is assigned by the generated UUID in Firebase Auth and assigned into a hashmap at creation
    // so no need to give an id in this object

    // Changing the default constructor so that this instance must have an ID to exist
    public User(String Username) {
        this.Username = Username;
    }

    public String getUsername() {
        return Username;
    }

    public String getPreferredUnit() {
        return PreferredUnit;
    }

    public void setPreferredUnit(String preferredUnit) {
        PreferredUnit = preferredUnit;
    }

    public String getPreferredLandmark() {
        return PreferredLandmark;
    }

    public void setPreferredLandmark(String preferredLandmark) {
        PreferredLandmark = preferredLandmark;
    }

    public List<SavedLocation> getSavedLocations() {
        return SavedLocations;
    }

    public void setSavedLocations(List<SavedLocation> savedLocations) {
        SavedLocations = savedLocations;
    }
}
