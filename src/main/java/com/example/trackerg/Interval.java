package com.example.trackerg;

public class Interval {
    private int workoutId;
    private int index;

    private int workDistanceMeters; // 0 if time-based
    private int workTimeSeconds;    // 0 if distance-based
    private int restTimeSeconds;    // rest after this interval

    public Interval() {}

    public Interval(int workoutId, int index, int workDistanceMeters, int workTimeSeconds, int restTimeSeconds) {
        this.workoutId = workoutId;
        this.index = index;
        this.workDistanceMeters = workDistanceMeters;
        this.workTimeSeconds = workTimeSeconds;
        this.restTimeSeconds = restTimeSeconds;
    }

    public int getWorkoutId() { return workoutId; }
    public void setWorkoutId(int workoutId) { this.workoutId = workoutId; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public int getWorkDistanceMeters() { return workDistanceMeters; }
    public void setWorkDistanceMeters(int workDistanceMeters) { this.workDistanceMeters = workDistanceMeters; }

    public int getWorkTimeSeconds() { return workTimeSeconds; }
    public void setWorkTimeSeconds(int workTimeSeconds) { this.workTimeSeconds = workTimeSeconds; }

    public int getRestTimeSeconds() { return restTimeSeconds; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }
}
