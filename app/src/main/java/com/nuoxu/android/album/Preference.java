package com.nuoxu.android.album;

public class Preference {
    private String preferenceStart;
    private String preferenceJoin;

    public Preference() {
    }

    public Preference(String preferenceStart, String preferenceJoin) {
        this.preferenceStart = preferenceStart;
        this.preferenceJoin = preferenceJoin;
    }

    public String getPreferenceStart() {
        return preferenceStart;
    }

    public String getPreferenceJoin() {
        return preferenceJoin;
    }

    public void setPreferenceStart(String preferenceStart) {
        this.preferenceStart = preferenceStart;
    }

    public void setPreferenceJoin(String preferenceJoin) {
        this.preferenceJoin = preferenceJoin;
    }
}
