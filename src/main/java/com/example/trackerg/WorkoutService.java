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

    public List<Workout> listWorkouts(String query, String sortKey) {
        List<Workout> filtered = filter(workouts, query);
        sort(filtered, sortKey);
        return filtered;
    }

    public Workout getWorkout(int id) {
        return workouts.stream()
                .filter(workout -> workout.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Workout not found: " + id));
    }

    public List<Interval> getIntervalsForWorkout(int workoutId) {
        return intervals.stream()
                .filter(interval -> interval.getWorkoutId() == workoutId)
                .sorted(Comparator.comparingInt(Interval::getIndex))
                .collect(Collectors.toList());
    }

    public void deleteWorkout(int id) {
        workouts.removeIf(workout -> workout.getId() == id);
        intervals.removeIf(interval -> interval.getWorkoutId() == id);
        persist();
    }

    public void createFromForm(WorkoutForm form) {
        Workout workout = new Workout();
        workout.setId(nextId());
        applyFormToWorkoutAndIntervals(form, workout);
        workouts.add(workout);
        persist();
    }

    public void updateFromForm(WorkoutForm form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("Missing id for update");
        }

        Workout workout = getWorkout(form.getId());

        intervals.removeIf(interval -> interval.getWorkoutId() == workout.getId());

        applyFormToWorkoutAndIntervals(form, workout);
        persist();
    }

    public Map<String, Workout> bestPRs() {
        Map<String, Workout> best = new HashMap<>();

        for (Workout workout : workouts) {
            String category = categoryFor(workout);
            best.compute(
                    category,
                    (key, current) -> current == null || workout.getWatts() > current.getWatts() ? workout : current
            );
        }

        return best;
    }

    private void applyFormToWorkoutAndIntervals(WorkoutForm form, Workout workout) {
        if (form.getDate() == null) {
            form.setDate(LocalDate.now());
        }

        workout.setDate(form.getDate());
        workout.setInterval(form.isInterval());
        workout.setFavorite(form.isFavorite());
        workout.setStrokeRate(safeInt(form.getStrokeRate()));
        workout.setNotes(form.getNotes() == null ? "" : form.getNotes());

        if (!form.isInterval()) {
            int totalSeconds = toSeconds(form.getTimeMinutes(), form.getTimeSeconds());
            int distance = safeInt(form.getDistanceMeters());

            workout.setTimeSeconds(totalSeconds);
            workout.setDistanceMeters(distance);

            computeSplitAndWatts(workout);
            return;
        }

        int totalTime = 0;
        int totalDistance = 0;

        List<IntervalForm> rows = form.getIntervals() == null ? List.of() : form.getIntervals();
        int intervalIndex = 1;

        for (IntervalForm row : rows) {
            if (row == null) {
                continue;
            }

            int workDistance = safeInt(row.getWorkDistanceMeters());
            int workTime = toSeconds(row.getWorkMinutes(), row.getWorkSeconds());
            int restTime = toSeconds(row.getRestMinutes(), row.getRestSeconds());
            int intervalStrokeRate = safeInt(row.getStrokeRate());

            if (workDistance == 0 && workTime == 0 && restTime == 0 && intervalStrokeRate == 0) {
                continue;
            }

            totalDistance += Math.max(0, workDistance);
            totalTime += Math.max(0, workTime) + Math.max(0, restTime);

            intervals.add(new Interval(
                    workout.getId(),
                    intervalIndex++,
                    workDistance,
                    workTime,
                    restTime,
                    intervalStrokeRate
            ));
        }

        int fallbackTime = toSeconds(form.getTimeMinutes(), form.getTimeSeconds());
        int fallbackDistance = safeInt(form.getDistanceMeters());

        workout.setTimeSeconds(totalTime > 0 ? totalTime : fallbackTime);
        workout.setDistanceMeters(totalDistance > 0 ? totalDistance : fallbackDistance);

        computeSplitAndWatts(workout);
    }

    private List<Workout> filter(List<Workout> list, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(list);
        }

        String normalizedQuery = query.trim().toLowerCase();

        return list.stream()
                .filter(workout -> {
                    String notes = workout.getNotes() == null ? "" : workout.getNotes().toLowerCase();
                    String date = workout.getDate() == null ? "" : workout.getDate().toString();
                    String type = workout.isInterval() ? "interval" : "non-interval";

                    boolean numericMatch = false;
                    try {
                        int numericQuery = Integer.parseInt(normalizedQuery);
                        numericMatch =
                                workout.getDistanceMeters() == numericQuery
                                        || (int) Math.round(workout.getWatts()) == numericQuery;
                    } catch (NumberFormatException ignored) {
                    }

                    return notes.contains(normalizedQuery)
                            || date.contains(normalizedQuery)
                            || type.contains(normalizedQuery)
                            || numericMatch;
                })
                .collect(Collectors.toList());
    }

    private void sort(List<Workout> list, String sortKey) {
        if (sortKey == null) {
            sortKey = "";
        }

        switch (sortKey) {
            case "distance" -> list.sort(Comparator.comparingInt(Workout::getDistanceMeters).reversed());
            case "time" -> list.sort(Comparator.comparingInt(Workout::getTimeSeconds));
            case "split" -> list.sort(Comparator.comparingDouble(Workout::getSplitSeconds));
            case "interval" -> list.sort(Comparator.comparing(Workout::isInterval).reversed());
            case "noninterval" -> list.sort(Comparator.comparing(Workout::isInterval));
            case "watts" -> list.sort(Comparator.comparingDouble(Workout::getWatts).reversed());
            case "oldest" -> list.sort(Comparator.comparing(Workout::getDate));
            case "newest" -> list.sort(Comparator.comparing(Workout::getDate).reversed());
            default -> list.sort(Comparator.comparing(Workout::getDate).reversed());
        }
    }

    private void computeSplitAndWatts(Workout workout) {
        int distance = workout.getDistanceMeters();
        int time = workout.getTimeSeconds();

        if (distance > 0 && time > 0) {
            double split = ((double) time / distance) * 500.0;
            workout.setSplitSeconds(split);

            double ratio = split / 500.0;
            double watts = 2.8 / Math.pow(ratio, 3);
            workout.setWatts(watts);
        } else {
            workout.setSplitSeconds(0);
            workout.setWatts(0);
        }
    }

    private String categoryFor(Workout workout) {
        if (workout.getDistanceMeters() % 1000 == 0) {
            return workout.getDistanceMeters() / 1000 + "k";
        }
        if (workout.isInterval()) {
            return "Intervals";
        }
        return "Fastest Split";
    }

    private int nextId() {
        return workouts.stream()
                .mapToInt(Workout::getId)
                .max()
                .orElse(0) + 1;
    }

    private void persist() {
        repo.saveWorkouts(workouts);
        repo.saveIntervals(intervals);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private int toSeconds(Integer minutes, Integer seconds) {
        int safeMinutes = minutes == null ? 0 : Math.max(0, minutes);
        int safeSeconds = seconds == null ? 0 : Math.max(0, seconds);

        if (safeSeconds >= 60) {
            safeMinutes += safeSeconds / 60;
            safeSeconds = safeSeconds % 60;
        }

        return safeMinutes * 60 + safeSeconds;
    }
}