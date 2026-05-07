package com.yucairoad.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.common.Result;
import com.yucairoad.dto.*;
import com.yucairoad.service.ExamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/results")
    public Result<List<ExamResultDTO>> getExamResults(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "examType", required = false) String examType,
            @RequestParam(value = "year", required = false) Integer year) {
        List<ExamResultDTO> results = examService.getExamResults(saveId, examType, year);
        return Result.success(results);
    }

    @GetMapping("/history")
    public Result<Page<ExamHistoryDTO>> getExamHistory(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<ExamHistoryDTO> history = examService.getExamHistory(saveId, studentId, page, size);
        return Result.success(history);
    }

    @GetMapping("/ranking")
    public Result<List<RankingDTO>> getRankings(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "examType", required = false) String examType,
            @RequestParam(value = "year", required = false) Integer year) {
        List<RankingDTO> rankings = examService.getRankings(saveId, examType, year);
        return Result.success(rankings);
    }

    @GetMapping("/summary")
    public Result<ExamSummaryDTO> getExamSummary(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "examType", required = false) String examType,
            @RequestParam(value = "year", required = false) Integer year) {
        ExamSummaryDTO summary = examService.getExamSummary(saveId, examType, year);
        return Result.success(summary);
    }

    @PostMapping("/trigger")
    public Result<ExamTriggerResult> triggerExam(
            @RequestParam("saveId") Long saveId,
            @RequestParam("examType") String examType) {
        ExamTriggerResult result = examService.triggerExam(saveId, examType);
        return Result.success("考试执行成功", result);
    }
}
