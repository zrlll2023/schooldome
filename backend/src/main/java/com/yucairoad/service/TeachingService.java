package com.yucairoad.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.dto.*;
import com.yucairoad.entity.Teacher;

public interface TeachingService {

    TeachingPolicy getTeachingPolicy(Long saveId);

    TeachingPolicy updateTeachingPolicy(Long saveId, TeachingPolicy policy);

    Page<TeacherInfo> getTeachers(Long saveId, int page, int size);

    TeacherInfo hireTeacher(Long saveId, HireRequest request);

    TeacherInfo trainTeacher(Long saveId, Long teacherId, TrainRequest request);

    void dismissTeacher(Long saveId, Long teacherId);

    Page<StudentInfo> getStudents(Long saveId, int page, int size, String grade);

    void focusStudent(Long saveId, Long studentId);

    DisciplineResult disciplineStudent(Long saveId, Long studentId, DisciplineRequest request);

    ExpelResult expelStudent(Long saveId, Long studentId);

    Page<StudentInfo> getAtRiskStudents(Long saveId, int page, int size);

    StudentStatisticsDTO getStudentStatistics(Long saveId);

    TeachingPrediction getPrediction(Long saveId);
}
