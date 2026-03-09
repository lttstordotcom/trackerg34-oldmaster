package com.example.trackerg;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutFileRepository repo = new WorkoutFileRepository();
    private final List<Workout> workouts;
    private final List<Interval> intervals;

    public WorkoutService() {
        this.workouts = repo.loadWorkouts();
        this.intervals = repo.loadIntervals();
    }

    // ---------- Public API ----------

    public List<Workout> listWorkouts(String query, String sortKey) {
        List<Workout> filtered = filter(workouts, query);
        sort(filtered, sortKey);
        return filtered;
    }

    public Workout getWorkout(int id) {
        return workouts.stream()
                .filter(w -> w.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Workout not found: " + id));
    }

    public List<Interval> getIntervalsForWorkout(int workoutId) {
        return intervals.stream()
                .filter(i -> i.getWorkoutId() == workoutId)
                .sorted(Comparator.comparingInt(Interval::getIndex))
                .collect(Collectors.toList());
    }

    public void deleteWorkout(int id) {
        workouts.removeIf(w -> w.getId() == id);
        intervals.removeIf(i -> i.getWorkoutId() == id);
        persist();
    }

    public void createFromForm(WorkoutForm form) {
        Workout w = new Workout();
        w.setId(nextId());
        applyFormToWorkoutAndIntervals(form, w, null);
        workouts.add(w);
        persist();
    }

    public void updateFromForm(WorkoutForm form) {
        if (form.getId() == null) throw new IllegalArgumentException("Missing id for update");
        Workout w = getWorkout(form.getId());

        // remove old intervals (if any), then re-add
        intervals.removeIf(i -> i.getWorkoutId() == w.getId());

        applyFormToWorkoutAndIntervals(form, w, w.getId());
        persist();
    }

    public Map<String, Workout> bestPRs() {
        // PR = best watts per cat(2k, 5k, 6...)
        Map<String, Workout> best = new HashMap<>();
        for (Workout w : workouts) {
            String cat = categoryFor(w);
            best.compute(cat, (k, current) -> (current == null || w.getWatts() > current.getWatts()) ? w : current);
        }
        return best;
    }

    // ---------- Core form mapping ----------

    private void applyFormToWorkoutAndIntervals(WorkoutForm form, Workout w, Integer workoutIdForIntervals) {
        if (form.getDate() == null) form.setDate(LocalDate.now());

        w.setDate(form.getDate());
        w.setInterval(form.isInterval());
        w.setFavorite(form.isFavorite());
        w.setStrokeRate(safeInt(form.getStrokeRate()));
        w.setNotes(form.getNotes() == null ? "" : form.getNotes());

        if (!form.isInterval()) {
            int totalSeconds = toSeconds(form.getTimeMinutes(), form.getTimeSeconds());
            int dist = safeInt(form.getDistanceMeters());

            w.setTimeSeconds(totalSeconds);
            w.setDistanceMeters(dist);

            computeSplitAndWatts(w);

        } else {
            // Interval mode total time = interval work times + rests if provided
            //total distance if provided
            int totalTime = 0;
            int totalDistance = 0;

            List<IntervalForm> rows = form.getIntervals() == null ? List.of() : form.getIntervals();
            int idx = 1;

            for (IntervalForm row : rows) {
                if (row == null) continue;

                int workDist = safeInt(row.getWorkDistanceMeters());
                int workTime = toSeconds(row.getWorkMinutes(), row.getWorkSeconds());
                int restTime = toSeconds(row.getRestMinutes(), row.getRestSeconds());

                // skip empty rows
                if (workDist == 0 && workTime == 0 && restTime == 0) continue;

                totalDistance += Math.max(0, workDist);
                totalTime += Math.max(0, workTime) + Math.max(0, restTime);

                intervals.add(new Interval(
                        w.getId(),
                        idx++,
                        workDist,
                        workTime,
                        restTime
                ));
            }

            // Allow user to optionally enter a total distance/time in the main fields too;
            int fallbackTime = toSeconds(form.getTimeMinutes(), form.getTimeSeconds());
            int fallbackDist = safeInt(form.getDistanceMeters());

            w.setTimeSeconds(totalTime > 0 ? totalTime : fallbackTime);
            w.setDistanceMeters(totalDistance > 0 ? totalDistance : fallbackDist);

            computeSplitAndWatts(w);
        }
    }

    //Filtering & Sorting

    private List<Workout> filter(List<Workout> list, String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(list);
        String q = query.trim().toLowerCase();

        return list.stream().filter(w -> {
            String notes = (w.getNotes() == null) ? "" : w.getNotes().toLowerCase();
            String date = (w.getDate() == null) ? "" : w.getDate().toString();
            String type = w.isInterval() ? "interval" : "non-interval";

            // allow numeric search for distance or watts
            boolean numericMatch = false;
            try {
                int n = Integer.parseInt(q);
                numericMatch = (w.getDistanceMeters() == n) || ((int)Math.round(w.getWatts()) == n);
            } catch (NumberFormatException ignored) {}

            return notes.contains(q) || date.contains(q) || type.contains(q) || numericMatch;
        }).collect(Collectors.toList());
    }

    private void sort(List<Workout> list, String sortKey) {
        if (sortKey == null) sortKey = "";

        switch (sortKey) {
            case "distance" -> list.sort(Comparator.comparingInt(Workout::getDistanceMeters).reversed());
            case "time" -> list.sort(Comparator.comparingInt(Workout::getTimeSeconds)); // lower time = better, but just consistent
            case "split" -> list.sort(Comparator.comparingDouble(Workout::getSplitSeconds));
            case "interval" -> list.sort(Comparator.comparing(Workout::isInterval).reversed());
            case "noninterval" -> list.sort(Comparator.comparing(Workout::isInterval)); // false first
            case "watts" -> list.sort(Comparator.comparingDouble(Workout::getWatts).reversed());
            case "oldest" -> list.sort(Comparator.comparing(Workout::getDate));
            case "newest" -> list.sort(Comparator.comparing(Workout::getDate).reversed());
            default -> list.sort(Comparator.comparing(Workout::getDate).reversed());
        }
    }

    //Calculations

    private void computeSplitAndWatts(Workout w) {
        int dist = w.getDistanceMeters();
        int time = w.getTimeSeconds();

        if (dist > 0 && time > 0) {
            double split = ((double) time / dist) * 500.0; // sec per 500m
            w.setSplitSeconds(split);

            // Concept2: watts = 2.8 / (pace/500)^3 where pace = splitSeconds
            double ratio = split / 500.0;
            double watts = 2.8 / Math.pow(ratio, 3);
            w.setWatts(watts);
        } else {
            w.setSplitSeconds(0);
            w.setWatts(0);
        }
    }

    private String categoryFor(Workout w) {
        // PR page categories
        if (w.getDistanceMeters() == 2000) return "2k";
        if (w.getDistanceMeters() == 5000) return "5k";
        if (w.getDistanceMeters() == 6000) return "6k";
        if (w.isInterval()) return "Intervals";
        return "Other";
    }
    //Error catching stuff 12/28
    private int nextId() {
        return workouts.stream().mapToInt(Workout::getId).max().orElse(0) + 1;
    }

    private void persist() {
        repo.saveWorkouts(workouts);
        repo.saveIntervals(intervals);
    }

    private int safeInt(Integer v) { return v == null ? 0 : Math.max(0, v); }

    private int toSeconds(Integer minutes, Integer seconds) {
        int m = (minutes == null ? 0 : Math.max(0, minutes));
        int s = (seconds == null ? 0 : Math.max(0, seconds));
        if (s >= 60) {
            m += s / 60;
            s = s % 60;
        }
        return m * 60 + s;
    }
}
