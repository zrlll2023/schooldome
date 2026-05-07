package com.yucairoad.service;

import com.yucairoad.dto.BuildProgressDTO;
import com.yucairoad.dto.K12StatusDTO;
import com.yucairoad.dto.PipelineDTO;
import com.yucairoad.dto.SynergyDTO;

public interface K12Service {

    K12StatusDTO getK12Status(Long saveId);

    BuildProgressDTO buildStage(Long saveId, String stage);

    PipelineDTO getPipeline(Long saveId);

    SynergyDTO getSynergy(Long saveId);

    void updateBuildProgress(Long saveId, String stageType);

    void processPipeline(Long saveId);

    void updateSynergyEffects(Long saveId);
}
