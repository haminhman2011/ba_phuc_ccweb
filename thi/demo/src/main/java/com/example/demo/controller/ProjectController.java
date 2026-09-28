package com.example.demo.controller;

import com.example.demo.model.Project;
import com.example.demo.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final OrganizationService service;

    public ProjectController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return service.getAllProjects();
    }

    @GetMapping("/{id}/staffs")
    public ResponseEntity<Project> getProjectStaffs(@PathVariable Long id) {
        return service.getProjectWithStaffs(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}