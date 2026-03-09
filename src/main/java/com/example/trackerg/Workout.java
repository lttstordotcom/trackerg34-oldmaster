package com.example.trackerg;

import java.time.LocalDate;

public class Workout {
    private int id;
    private LocalDate date;

    private boolean interval;
    private boolean favorite;

    private int distanceMeters;
    private int timeSeconds;

    private double splitSeconds;
    private double watts;

    private int strokeRate;
    private String notes;

    public Workout() {}

    // gets and sets
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isInterval() { return interval; }
    public void setInterval(boolean interval) { this.interval = interval; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public int getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(int distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(int timeSeconds) { this.timeSeconds = timeSeconds; }

    public double getSplitSeconds() { return splitSeconds; }
    public void setSplitSeconds(double splitSeconds) { this.splitSeconds = splitSeconds; }

    public double getWatts() { return watts; }
    public void setWatts(double watts) { this.watts = watts; }

    public int getStrokeRate() { return strokeRate; }
    public void setStrokeRate(int strokeRate) { this.strokeRate = strokeRate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
