package com.yucairoad.service;

import com.yucairoad.dto.BranchDTO;
import com.yucairoad.dto.BranchDetailDTO;
import com.yucairoad.dto.CityTypeDTO;

import java.util.List;

public interface BranchService {

    List<BranchDTO> getBranchList(Long saveId);

    BranchDetailDTO getBranchDetail(Long saveId, Long branchId);

    BranchDTO openBranch(Long saveId, String cityType, String name);

    BranchDTO changeManagementMode(Long saveId, Long branchId, String mode);

    BranchDTO closeBranch(Long saveId, Long branchId);

    List<CityTypeDTO> getAvailableCityTypes();

    void updateAllBranches(Long saveId);
}
