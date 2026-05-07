package com.yucairoad.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.dto.*;

import java.util.List;

public interface ExamService {

    void processMonthlyExam(Long saveId);

    void processFinalExam(Long saveId);

    void processGaokao(Long saveId);

    void processMiddleSchoolExam(Long saveId);

    void processPrimarySchoolExam(Long saveId);

    List<ExamResultDTO> getExamResults(Long saveId, String examType, Integer year);

    Page<ExamHistoryDTO> getExamHistory(Long saveId, Long studentId, int page, int size);

    List<RankingDTO> getRankings(Long saveId, String examType, Integer year);

    ExamSummaryDTO getExamSummary(Long saveId, String examType, Integer year);

    ExamTriggerResult triggerExam(Long saveId, String examType);
}
