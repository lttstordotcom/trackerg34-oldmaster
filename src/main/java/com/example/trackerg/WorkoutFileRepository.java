package com.example.trackerg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutFileRepository {

    private static final String WORKOUTS_FILE = "workouts.csv";
    private static final String INTERVALS_FILE = "intervals.csv";

    public WorkoutFileRepository() {
        ensureFilesExist();
    }

    private void ensureFilesExist() {
        try {
            if (!Files.exists(Path.of(WORKOUTS_FILE))) {
                Files.writeString(
                        Path.of(WORKOUTS_FILE),
                        "id,date,interval,favorite,distanceMeters,timeSeconds,splitSeconds,watts,strokeRate,notes\n"
                );
            }

            if (!Files.exists(Path.of(INTERVALS_FILE))) {
                Files.writeString(
                        Path.of(INTERVALS_FILE),
                        "workoutId,index,workDistanceMeters,workTimeSeconds,restTimeSeconds,strokeRate\n"
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data files", e);
        }
    }

    public List<Workout> loadWorkouts() {
        List<Workout> workouts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(WORKOUTS_FILE))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                Workout workout = new Workout();
                workout.setId(Integer.parseInt(parts[0]));
                workout.setDate(LocalDate.parse(parts[1]));
                workout.setInterval(Boolean.parseBoolean(parts[2]));
                workout.setFavorite(Boolean.parseBoolean(parts[3]));
                workout.setDistanceMeters(Integer.parseInt(parts[4]));
                workout.setTimeSeconds(Integer.parseInt(parts[5]));
                workout.setSplitSeconds(Double.parseDouble(parts[6]));
                workout.setWatts(Double.parseDouble(parts[7]));
                workout.setStrokeRate(Integer.parseInt(parts[8]));
                workout.setNotes(unescape(parts[9]));

                workouts.add(workout);
            }
        } catch (FileNotFoundException e) {
            return workouts;
        } catch (IOException e) {
            throw new RuntimeException("Failed reading workouts.csv", e);
        }

        return workouts;
    }

    public void saveWorkouts(List<Workout> workouts) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(WORKOUTS_FILE))) {
            writer.println("id,date,interval,favorite,distanceMeters,timeSeconds,splitSeconds,watts,strokeRate,notes");

            for (Workout workout : workouts) {
                writer.printf(
                        "%d,%s,%b,%b,%d,%d,%.2f,%.2f,%d,%s%n",
                        workout.getId(),
                        workout.getDate(),
                        workout.isInterval(),
                        workout.isFavorite(),
                        workout.getDistanceMeters(),
                        workout.getTimeSeconds(),
                        workout.getSplitSeconds(),
                        workout.getWatts(),
                        workout.getStrokeRate(),
                        escape(workout.getNotes())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing workouts.csv", e);
        }
    }

    public List<Interval> loadIntervals() {
        List<Interval> intervals = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(INTERVALS_FILE))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                Interval interval = new Interval();
                interval.setWorkoutId(Integer.parseInt(parts[0]));
                interval.setIndex(Integer.parseInt(parts[1]));
                interval.setWorkDistanceMeters(Integer.parseInt(parts[2]));
                interval.setWorkTimeSeconds(Integer.parseInt(parts[3]));
                interval.setRestTimeSeconds(Integer.parseInt(parts[4]));
                interval.setStrokeRate(parts.length > 5 ? Integer.parseInt(parts[5]) : 0);

                intervals.add(interval);
            }
        } catch (FileNotFoundException e) {
            return intervals;
        } catch (IOException e) {
            throw new RuntimeException("Failed reading intervals.csv", e);
        }

        return intervals;
    }

    public void saveIntervals(List<Interval> intervals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INTERVALS_FILE))) {
            writer.println("workoutId,index,workDistanceMeters,workTimeSeconds,restTimeSeconds,strokeRate");

            for (Interval interval : intervals) {
                writer.printf(
                        "%d,%d,%d,%d,%d,%d%n",
                        interval.getWorkoutId(),
                        interval.getIndex(),
                        interval.getWorkDistanceMeters(),
                        interval.getWorkTimeSeconds(),
                        interval.getRestTimeSeconds(),
                        interval.getStrokeRate()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing intervals.csv", e);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\n", "\\n").replace(",", ";");
    }

    private String unescape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\n", "\n").replace(";", ",");
    }
}