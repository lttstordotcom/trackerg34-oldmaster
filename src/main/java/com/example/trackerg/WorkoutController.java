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
	@GetMapping("/workouts")
	public String workouts(
    		@RequestParam(value = "query", required = false) String query,
        	@RequestParam(value = "sort", required = false) String sort,
        	Model model
	) {
    List<Workout> workoutList = service.listWorkouts(query, sort);

    Map<Integer, List<Interval>> intervalsByWorkout = new HashMap<>();
    for (Workout workout : workoutList) {
        if (workout.isInterval()) {
            intervalsByWorkout.put(workout.getId(), service.getIntervalsForWorkout(workout.getId()));
        }
    }

    	model.addAttribute("workouts", workoutList);
    	model.addAttribute("intervalsByWorkout", intervalsByWorkout);
    	model.addAttribute("query", query == null ? "" : query);
    	model.addAttribute("sort", sort == null ? "" : sort);

    	return "workouts";
	}

    @GetMapping("/workouts/new")
    public String newWorkout(Model model) {
        WorkoutForm form = new WorkoutForm();
        model.addAttribute("form", form);
        model.addAttribute("mode", "create");
        return "workout_form";
    }

    @PostMapping("/workouts")
    public String create(@ModelAttribute("form") WorkoutForm form) {
        service.createFromForm(form);
        return "redirect:/workouts";
    }

    @GetMapping("/workouts/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        Workout workout = service.getWorkout(id);
        List<Interval> intervalRows = service.getIntervalsForWorkout(id);

        WorkoutForm form = new WorkoutForm();
        form.setId(workout.getId());
        form.setDate(workout.getDate());
        form.setInterval(workout.isInterval());
        form.setFavorite(workout.isFavorite());
        form.setDistanceMeters(workout.getDistanceMeters());
        form.setStrokeRate(workout.getStrokeRate());
        form.setNotes(workout.getNotes());

        // Rebuild total time into minutes and seconds for the form.
        int totalTime = workout.getTimeSeconds();
        form.setTimeMinutes(totalTime / 60);
        form.setTimeSeconds(totalTime % 60);

        // Pass saved interval rows back to the form when editing.
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

    @GetMapping("/prs")
    public String prs(Model model) {
        Map<String, Workout> best = service.bestPRs();
        model.addAttribute("best", best);
        return "prs";
    }
}