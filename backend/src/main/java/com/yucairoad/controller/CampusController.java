package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.BuildingDTO;
import com.yucairoad.dto.CampusStatsDTO;
import com.yucairoad.service.CampusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/campus")
public class CampusController {

    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @GetMapping("/buildings")
    public Result<List<BuildingDTO>> getBuildings(@RequestParam("saveId") Long saveId) {
        List<BuildingDTO> buildings = campusService.getBuildings(saveId);
        return Result.success(buildings);
    }

    @PostMapping("/buildings")
    public Result<BuildingDTO> createBuilding(@RequestParam("saveId") Long saveId,
                                              @RequestBody Map<String, Object> body) {
        String buildingType = (String) body.get("buildingType");
        Integer level = (Integer) body.get("level");
        if (buildingType == null || level == null) {
            return Result.error("参数不完整，需要buildingType和level");
        }
        BuildingDTO building = campusService.createBuilding(saveId, buildingType, level);
        return Result.success("建筑建造申请成功", building);
    }

    @PutMapping("/buildings/{buildingId}/upgrade")
    public Result<BuildingDTO> upgradeBuilding(@RequestParam("saveId") Long saveId,
                                               @PathVariable Long buildingId) {
        BuildingDTO building = campusService.upgradeBuilding(saveId, buildingId);
        return Result.success("建筑升级成功", building);
    }

    @DeleteMapping("/buildings/{buildingId}")
    public Result<BuildingDTO> demolishBuilding(@RequestParam("saveId") Long saveId,
                                                @PathVariable Long buildingId) {
        BuildingDTO building = campusService.demolishBuilding(saveId, buildingId);
        return Result.success("建筑拆除成功", building);
    }

    @PostMapping("/expand")
    public Result<CampusStatsDTO> expandCampus(@RequestParam("saveId") Long saveId) {
        CampusStatsDTO stats = campusService.expandCampus(saveId);
        return Result.success("校园扩建成功", stats);
    }

    @GetMapping("/stats")
    public Result<CampusStatsDTO> getCampusStats(@RequestParam("saveId") Long saveId) {
        CampusStatsDTO stats = campusService.getCampusStats(saveId);
        return Result.success(stats);
    }
}
