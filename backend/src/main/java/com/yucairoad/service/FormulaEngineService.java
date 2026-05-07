package com.yucairoad.service;

import com.yucairoad.dto.FinancialReport;
import com.yucairoad.dto.ScoreResult;
import com.yucairoad.dto.ReputationChange;
import com.yucairoad.dto.EnrollmentPolicy;
import com.yucairoad.dto.TeachingPolicy;
import com.yucairoad.entity.Building;
import com.yucairoad.entity.ExamRecord;
import com.yucairoad.entity.School;
import com.yucairoad.entity.Student;
import com.yucairoad.entity.Teacher;

import java.util.List;

public interface FormulaEngineService {

    ScoreResult calculateScore(Student student, List<Teacher> teachers, List<Building> buildings, TeachingPolicy policy);

    ReputationChange calculateReputation(School school, List<ExamRecord> examRecords);

    FinancialReport calculateFinance(Long saveId);

    double calculateEnrollmentQuality(School school, EnrollmentPolicy policy);

    List<Student> updateStudentAttributes(List<Student> students, TeachingPolicy policy);
}
