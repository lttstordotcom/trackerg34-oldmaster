package com.example.trackerg;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutForm {
    private Integer id; // null for new
    private LocalDate date;

    private boolean interval;
    private boolean favorite;

    // Non-interval fields
    private Integer timeMinutes;
    private Integer timeSeconds;

    private Integer distanceMeters;
    private Integer strokeRate;
    private String notes;

    // Interval rows
    private List<IntervalForm> intervals = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isInterval() { return interval; }
    public void setInterval(boolean interval) { this.interval = interval; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public Integer getTimeMinutes() { return timeMinutes; }
    public void setTimeMinutes(Integer timeMinutes) { this.timeMinutes = timeMinutes; }

    public Integer getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(Integer timeSeconds) { this.timeSeconds = timeSeconds; }

    public Integer getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }

    public Integer getStrokeRate() { return strokeRate; }
    public void setStrokeRate(Integer strokeRate) { this.strokeRate = strokeRate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<IntervalForm> getIntervals() { return intervals; }
    public void setIntervals(List<IntervalForm> intervals) { this.intervals = intervals; }
}
