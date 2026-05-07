package com.yucairoad.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.common.Result;
import com.yucairoad.dto.*;
import com.yucairoad.service.TeachingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teaching")
public class TeachingController {

    private final TeachingService teachingService;

    public TeachingController(TeachingService teachingService) {
        this.teachingService = teachingService;
    }

    @GetMapping("/policy")
    public Result<TeachingPolicy> getTeachingPolicy(@RequestParam("saveId") Long saveId) {
        TeachingPolicy policy = teachingService.getTeachingPolicy(saveId);
        return Result.success(policy);
    }

    @PutMapping("/policy")
    public Result<TeachingPolicy> updateTeachingPolicy(
            @RequestParam("saveId") Long saveId,
            @RequestBody TeachingPolicy policy) {
        TeachingPolicy updated = teachingService.updateTeachingPolicy(saveId, policy);
        return Result.success("教学政策更新成功", updated);
    }

    @GetMapping("/teachers")
    public Result<Page<TeacherInfo>> getTeachers(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<TeacherInfo> teachers = teachingService.getTeachers(saveId, page, size);
        return Result.success(teachers);
    }

    @PostMapping("/teachers/hire")
    public Result<TeacherInfo> hireTeacher(
            @RequestParam("saveId") Long saveId,
            @RequestBody HireRequest request) {
        TeacherInfo teacher = teachingService.hireTeacher(saveId, request);
        return Result.success("招聘成功", teacher);
    }

    @PostMapping("/teachers/{teacherId}/train")
    public Result<TeacherInfo> trainTeacher(
            @RequestParam("saveId") Long saveId,
            @PathVariable Long teacherId,
            @RequestBody TrainRequest request) {
        TeacherInfo teacher = teachingService.trainTeacher(saveId, teacherId, request);
        return Result.success("培训完成", teacher);
    }

    @PostMapping("/teachers/{teacherId}/dismiss")
    public Result<Void> dismissTeacher(
            @RequestParam("saveId") Long saveId,
            @PathVariable Long teacherId) {
        teachingService.dismissTeacher(saveId, teacherId);
        return Result.success("解聘成功", null);
    }

    @GetMapping("/students")
    public Result<Page<StudentInfo>> getStudents(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "grade", required = false) String grade) {
        Page<StudentInfo> students = teachingService.getStudents(saveId, page, size, grade);
        return Result.success(students);
    }

    @PostMapping("/students/{studentId}/focus")
    public Result<Void> focusStudent(
            @RequestParam("saveId") Long saveId,
            @PathVariable Long studentId) {
        teachingService.focusStudent(saveId, studentId);
        return Result.success("已将该学生设为重点关注", null);
    }

    @GetMapping("/prediction")
    public Result<TeachingPrediction> getPrediction(@RequestParam("saveId") Long saveId) {
        TeachingPrediction prediction = teachingService.getPrediction(saveId);
        return Result.success(prediction);
    }
}
