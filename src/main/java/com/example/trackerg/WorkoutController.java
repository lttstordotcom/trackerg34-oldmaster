package com.example.trackerg;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/workouts";
    }

    //View Workouts
    @GetMapping("/workouts")
    public String workouts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "sort", required = false) String sort,
            Model model
    ) {
        model.addAttribute("workouts", service.listWorkouts(query, sort));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("sort", sort == null ? "" : sort);
        return "workouts";
    }

    //Add Workout
    @GetMapping("/workouts/new")
    public String newWorkout(Model model) {
        WorkoutForm form = new WorkoutForm();
        // Start with 1 interval row so the UI has something to show when toggled
        model.addAttribute("form", form);
        model.addAttribute("mode", "create");
        return "workout_form";
    }

    @PostMapping("/workouts")
    public String create(@ModelAttribute("form") WorkoutForm form) {
        service.createFromForm(form);
        return "redirect:/workouts";
    }

    //Edit Workout
    @GetMapping("/workouts/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        Workout w = service.getWorkout(id);
        List<Interval> intervalRows = service.getIntervalsForWorkout(id);

        WorkoutForm form = new WorkoutForm();
        form.setId(w.getId());
        form.setDate(w.getDate());
        form.setInterval(w.isInterval());
        form.setFavorite(w.isFavorite());
        form.setDistanceMeters(w.getDistanceMeters());
        form.setStrokeRate(w.getStrokeRate());
        form.setNotes(w.getNotes());

        // Convert total time into minutes + seconds
        int total = w.getTimeSeconds();
        form.setTimeMinutes(total / 60);
        form.setTimeSeconds(total % 60);

        // If interval workout show existing interval rows
        model.addAttribute("intervalData", intervalRows);
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        return "workout_form";
    }

    @PostMapping("/workouts/update")
    public String update(@ModelAttribute("form") WorkoutForm form) {
        service.updateFromForm(form);
        return "redirect:/workouts";
    }

    @GetMapping("/workouts/delete/{id}")
    public String delete(@PathVariable int id) {
        service.deleteWorkout(id);
        return "redirect:/workouts";
    }

    //View PR
    @GetMapping("/prs")
    public String prs(Model model) {
        Map<String, Workout> best = service.bestPRs();
        model.addAttribute("best", best);
        return "prs";
    }
}
