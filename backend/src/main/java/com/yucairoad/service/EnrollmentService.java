package com.yucairoad.service;

import com.yucairoad.dto.EnrollmentPolicy;
import com.yucairoad.dto.EnrollmentPreview;
import com.yucairoad.dto.EnrollmentResult;

public interface EnrollmentService {

    EnrollmentPolicy saveEnrollmentPolicy(Long saveId, EnrollmentPolicy policy);

    EnrollmentPreview previewEnrollment(Long saveId);

    EnrollmentResult executeEnrollment(Long saveId);
}
