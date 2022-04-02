package com.example.openmusic.models;

public class Song {
    private String title;
    private String artist;
    private String displayName;
    private String path;
    private int duration;
    private String album;


    public Song() {
    }



    public Song(String title, String artist, String displayName, String path, int duration,  String album) {
        this.title = title;
        this.artist = artist;
        this.displayName = displayName;
        this.path = path;
        this.duration = duration;
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
