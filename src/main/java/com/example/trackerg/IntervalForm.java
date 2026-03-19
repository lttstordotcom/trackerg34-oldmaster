package com.example.trackerg;

public class IntervalForm {
    // either distance OR time
    private Integer workDistanceMeters;

    private Integer workMinutes;
    private Integer workSeconds;

    // Rest time
    private Integer restMinutes;
    private Integer restSeconds;

    public Integer getWorkDistanceMeters() {
        return workDistanceMeters;
    }

    public void setWorkDistanceMeters(Integer workDistanceMeters) {
        this.workDistanceMeters = workDistanceMeters;
    }

    public Integer getWorkMinutes() {
        return workMinutes;
    }

    public void setWorkMinutes(Integer workMinutes) {
        this.workMinutes = workMinutes;
    }

    public Integer getWorkSeconds() {
        return workSeconds;
    }

    public void setWorkSeconds(Integer workSeconds) {
        this.workSeconds = workSeconds;
    }

    public Integer getRestMinutes() {
        return restMinutes;
    }

    public void setRestMinutes(Integer restMinutes) {
        this.restMinutes = restMinutes;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }
}
