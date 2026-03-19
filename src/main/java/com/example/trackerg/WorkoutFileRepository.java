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
                Files.writeString(Path.of(WORKOUTS_FILE),
                        "id,date,interval,favorite,distanceMeters,timeSeconds,splitSeconds,watts,strokeRate,notes\n");
            }
            if (!Files.exists(Path.of(INTERVALS_FILE))) {
                Files.writeString(Path.of(INTERVALS_FILE),
                        "workoutId,index,workDistanceMeters,workTimeSeconds,restTimeSeconds\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data files", e);
        }
    }

    public List<Workout> loadWorkouts() {
        List<Workout> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(WORKOUTS_FILE))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                Workout w = new Workout();
                w.setId(Integer.parseInt(p[0]));
                w.setDate(LocalDate.parse(p[1]));
                w.setInterval(Boolean.parseBoolean(p[2]));
                w.setFavorite(Boolean.parseBoolean(p[3]));
                w.setDistanceMeters(Integer.parseInt(p[4]));
                w.setTimeSeconds(Integer.parseInt(p[5]));
                w.setSplitSeconds(Double.parseDouble(p[6]));
                w.setWatts(Double.parseDouble(p[7]));
                w.setStrokeRate(Integer.parseInt(p[8]));
                w.setNotes(unescape(p[9]));
                list.add(w);
            }
        } catch (FileNotFoundException e) {
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed reading workouts.csv", e);
        }
        return list;
    }

    public void saveWorkouts(List<Workout> workouts) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(WORKOUTS_FILE))) {
            pw.println("id,date,interval,favorite,distanceMeters,timeSeconds,splitSeconds,watts,strokeRate,notes");
            for (Workout w : workouts) {
                pw.printf(
                        "%d,%s,%b,%b,%d,%d,%.2f,%.2f,%d,%s%n",
                        w.getId(),
                        w.getDate(),
                        w.isInterval(),
                        w.isFavorite(),
                        w.getDistanceMeters(),
                        w.getTimeSeconds(),
                        w.getSplitSeconds(),
                        w.getWatts(),
                        w.getStrokeRate(),
                        escape(w.getNotes())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing workouts.csv", e);
        }
    }

    public List<Interval> loadIntervals() {
        List<Interval> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(INTERVALS_FILE))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                Interval it = new Interval();
                it.setWorkoutId(Integer.parseInt(p[0]));
                it.setIndex(Integer.parseInt(p[1]));
                it.setWorkDistanceMeters(Integer.parseInt(p[2]));
                it.setWorkTimeSeconds(Integer.parseInt(p[3]));
                it.setRestTimeSeconds(Integer.parseInt(p[4]));
                list.add(it);
            }
        } catch (FileNotFoundException e) {
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed reading intervals.csv", e);
        }
        return list;
    }

    public void saveIntervals(List<Interval> intervals) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(INTERVALS_FILE))) {
            pw.println("workoutId,index,workDistanceMeters,workTimeSeconds,restTimeSeconds");
            for (Interval it : intervals) {
                pw.printf(
                        "%d,%d,%d,%d,%d%n",
                        it.getWorkoutId(),
                        it.getIndex(),
                        it.getWorkDistanceMeters(),
                        it.getWorkTimeSeconds(),
                        it.getRestTimeSeconds()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing intervals.csv", e);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        // Replace commas so CSV stays safe
        return s.replace("\n", "\\n").replace(",", ";");
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace(";", ",");
    }
}
