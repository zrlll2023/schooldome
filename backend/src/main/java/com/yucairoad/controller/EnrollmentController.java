package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.EnrollmentPolicy;
import com.yucairoad.dto.EnrollmentPreview;
import com.yucairoad.dto.EnrollmentResult;
import com.yucairoad.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PutMapping("/policy")
    public Result<EnrollmentPolicy> saveEnrollmentPolicy(@RequestParam("saveId") Long saveId,
                                                          @RequestBody EnrollmentPolicy policy) {
        EnrollmentPolicy savedPolicy = enrollmentService.saveEnrollmentPolicy(saveId, policy);
        return Result.success("招生政策保存成功", savedPolicy);
    }

    @GetMapping("/preview")
    public Result<EnrollmentPreview> previewEnrollment(@RequestParam("saveId") Long saveId) {
        EnrollmentPreview preview = enrollmentService.previewEnrollment(saveId);
        return Result.success(preview);
    }

    @PostMapping("/execute")
    public Result<EnrollmentResult> executeEnrollment(@RequestParam("saveId") Long saveId) {
        EnrollmentResult result = enrollmentService.executeEnrollment(saveId);
        return Result.success("招生执行成功", result);
    }
}
