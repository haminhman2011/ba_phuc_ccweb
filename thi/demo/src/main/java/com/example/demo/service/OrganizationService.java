package com.example.demo.service;

import com.example.demo.model.Project;
import com.example.demo.model.Staff;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrganizationService {

    // Single source of truth
    private final List<Project> projectList = new ArrayList<>();
    private final AtomicLong staffIdCounter = new AtomicLong(1);

    public OrganizationService() {
        // Seed mock projects
        projectList.add(new Project(101L, "Engineering"));
        projectList.add(new Project(102L, "Human Resources"));

        // Seed initial staff
        createStaff(new Staff(null, "Alice Smith", "alice@example.com", 101L));
        createStaff(new Staff(null, "Bob Jones", "bob@example.com", 101L));
        createStaff(new Staff(null, "Charlie Brown", "charlie@example.com", 102L));
    }

    // Helper: Find  project by ID
    private Optional<Project> findProjectById(Long projectId) {
        return projectList.stream()
                .filter(proj -> proj.getId().equals(projectId))
                .findFirst();
    }

    // --- STAFF CRUD OPERATIONS ---

    // Get ALL staff across all projects
    public List<Staff> getAllStaff() {
        List<Staff> allStaff = new ArrayList<>();
        for (Project proj : projectList) {
            for (Staff staff : proj.getStaffs()) {
                staff.setProjectId(proj.getId());
                staff.setProjectName(proj.getName());
                allStaff.add(staff);
            }
        }
        return allStaff;
    }

    // Get a single staff by ID
    public Optional<Staff> getStaffById(Long staffId) {
        for (Project proj : projectList) {
            for (Staff staff : proj.getStaffs()) {
                if (staff.getId().equals(staffId)) {
                    return Optional.of(staff);
                }
            }
        }
        return Optional.empty();
    }

    // CREATE staff: Add to specified Project's staff list
    public Optional<Staff> createStaff(Staff staff) {
        Optional<Project> projOpt = findProjectById(staff.getProjectId());
        if (projOpt.isEmpty()) {
            return Optional.empty(); // Project not found
        }

        staff.setId(staffIdCounter.getAndIncrement());
        projOpt.get().getStaffs().add(staff);
        return Optional.of(staff);
    }

    // UPDATE staff: Handles field updates and moving staff if projectId changes
    public Optional<Staff> updateStaff(Long id, Staff updatedStaff) {
        // 1. Check if the new project exists
        Optional<Project> targetProjOpt = findProjectById(updatedStaff.getProjectId());
        if (targetProjOpt.isEmpty()) {
            return Optional.empty();
        }

        // 2. Find existing staff and current project
        for (Project proj : projectList) {
            List<Staff> projStaffs = proj.getStaffs();
            for (int i = 0; i < projStaffs.size(); i++) {
                Staff existing = projStaffs.get(i);
                if (existing.getId().equals(id)) {
                    
                    updatedStaff.setId(id);

                    // If project changed, move staff to new project list
                    if (!existing.getProjectId().equals(updatedStaff.getProjectId())) {
                        projStaffs.remove(i); // Remove from old project
                        targetProjOpt.get().getStaffs().add(updatedStaff); // Add to new project
                    } else {
                        projStaffs.set(i, updatedStaff); // Update in-place
                    }
                    return Optional.of(updatedStaff);
                }
            }
        }
        return Optional.empty(); // Staff ID not found
    }

    // DELETE staff: Removes staff directly from its project list
    public boolean deleteStaff(Long staffId) {
        for (Project proj : projectList) {
            boolean removed = proj.getStaffs().removeIf(s -> s.getId().equals(staffId));
            if (removed) {
                return true;
            }
        }
        return false;
    }

    // --- PROJECT OPERATIONS ---

    public List<Project> getAllProjects() {
        return new ArrayList<>(projectList);
    }

    public Optional<Project> getProjectWithStaffs(Long projectId) {
        return findProjectById(projectId);
    }
}