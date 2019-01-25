package com.school.schooldb.web.controller;

import com.school.schooldb.model.Course;
import com.school.schooldb.model.User;
import com.school.schooldb.service.CourseService;
import com.school.schooldb.service.UserService;
import com.school.schooldb.util.CustomErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    CourseService courseService;

    @Autowired
    UserService userService;

    // Get all courses
    @GetMapping("/courses")
    public List<Course> getRecipes() {
        return courseService.findAll();
    }

    // Get details for a single course
    @GetMapping("courses/{id}")
    public Course courseDetails(@PathVariable Long id) {
        return courseService.findById(id);
    }

    // Add a new course
    @PostMapping("/courses")
    public ResponseEntity<?> postRecipe(@Valid @RequestBody Course course, BindingResult bindingResult,
                                        Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName());

        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            List<String> message = new ArrayList<>();
            for (FieldError e : errors) {
                message.add(e.getField().toUpperCase() + ":" + e.getDefaultMessage());
            }
            return new ResponseEntity<>(new CustomErrorType("Create failed due to: " + message.toString()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Course _course = courseService.createCourse(course, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/courses/{id}")
                .buildAndExpand(_course.getId()).toUri();

        return ResponseEntity.created(location).body("Course created Successfully");

    }

    // Update a course
    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateRecipe(@Valid @RequestBody Course course, BindingResult bindingResult,
                                          @PathVariable("id") Long id, Authentication
                                                  authentication) {
        User currentUser = userService.findByEmail(authentication.getName());
        User user = courseService.findById(id).getUser();

        System.out.println(currentUser + " " + user);

        if (currentUser != user) {
            return new ResponseEntity<>(new CustomErrorType("You can only update courses which you created"),
                    HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            List<String> message = new ArrayList<>();
            for (FieldError e : errors) {
                message.add(e.getField().toUpperCase() + ":" + e.getDefaultMessage());
            }
            return new ResponseEntity<>(new CustomErrorType("Update failed due to: " + message.toString()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        courseService.update(course, currentUser, id);

        return new ResponseEntity<>("Course successfully updated", HttpStatus.OK);
    }

    // Delete a course
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable("id") Long id, Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName());
        User recipeUser = courseService.findById(id).getUser();

        if (currentUser != recipeUser) {
            return new ResponseEntity<>(new CustomErrorType("You can only delete recipes which you created"),
                    HttpStatus.FORBIDDEN);
        }

        courseService.delete(courseService.findById(id));

        return new ResponseEntity<>("Recipe has been deleted!", HttpStatus.OK);
    }
}