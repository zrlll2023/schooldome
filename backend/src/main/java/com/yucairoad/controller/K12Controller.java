package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.BuildProgressDTO;
import com.yucairoad.dto.K12StatusDTO;
import com.yucairoad.dto.PipelineDTO;
import com.yucairoad.dto.SynergyDTO;
import com.yucairoad.service.K12Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/k12")
public class K12Controller {

    private final K12Service k12Service;

    public K12Controller(K12Service k12Service) {
        this.k12Service = k12Service;
    }

    @GetMapping("/status")
    public Result<K12StatusDTO> getK12Status(@RequestParam("saveId") Long saveId) {
        K12StatusDTO status = k12Service.getK12Status(saveId);
        return Result.success(status);
    }

    @PostMapping("/build")
    public Result<BuildProgressDTO> buildStage(@RequestParam("saveId") Long saveId,
                                                @RequestParam("stage") String stage) {
        BuildProgressDTO progress = k12Service.buildStage(saveId, stage);
        return Result.success(("PRIMARY".equalsIgnoreCase(stage) ? "小学" : "初中") + "建设申请已提交", progress);
    }

    @GetMapping("/pipeline")
    public Result<PipelineDTO> getPipeline(@RequestParam("saveId") Long saveId) {
        PipelineDTO pipeline = k12Service.getPipeline(saveId);
        return Result.success(pipeline);
    }

    @GetMapping("/synergy")
    public Result<SynergyDTO> getSynergy(@RequestParam("saveId") Long saveId) {
        SynergyDTO synergy = k12Service.getSynergy(saveId);
        return Result.success(synergy);
    }
}
