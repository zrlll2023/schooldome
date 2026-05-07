package com.yucairoad.service;

import com.yucairoad.dto.BuildingDTO;
import com.yucairoad.dto.CampusStatsDTO;

import java.util.List;

public interface CampusService {

    List<BuildingDTO> getBuildings(Long saveId);

    BuildingDTO createBuilding(Long saveId, String buildingType, Integer level);

    BuildingDTO upgradeBuilding(Long saveId, Long buildingId);

    BuildingDTO demolishBuilding(Long saveId, Long buildingId);

    CampusStatsDTO expandCampus(Long saveId);

    CampusStatsDTO getCampusStats(Long saveId);

    void checkBuildingCompletion(Long saveId);
}
