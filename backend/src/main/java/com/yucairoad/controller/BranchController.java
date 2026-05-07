package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.BranchDTO;
import com.yucairoad.dto.BranchDetailDTO;
import com.yucairoad.dto.CityTypeDTO;
import com.yucairoad.service.BranchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/branch")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/list")
    public Result<List<BranchDTO>> getBranchList(@RequestParam("saveId") Long saveId) {
        List<BranchDTO> branches = branchService.getBranchList(saveId);
        return Result.success(branches);
    }

    @GetMapping("/city-types")
    public Result<List<CityTypeDTO>> getAvailableCityTypes() {
        List<CityTypeDTO> cityTypes = branchService.getAvailableCityTypes();
        return Result.success(cityTypes);
    }

    @PostMapping("/open")
    public Result<BranchDTO> openBranch(@RequestParam("saveId") Long saveId,
                                        @RequestBody Map<String, String> body) {
        String cityType = body.get("cityType");
        String name = body.get("name");
        if (cityType == null || cityType.isBlank()) {
            return Result.error("请选择城市类型");
        }
        BranchDTO branch = branchService.openBranch(saveId, cityType, name);
        return Result.success("分校开设成功", branch);
    }

    @GetMapping("/{branchId}")
    public Result<BranchDetailDTO> getBranchDetail(@RequestParam("saveId") Long saveId,
                                                   @PathVariable Long branchId) {
        BranchDetailDTO detail = branchService.getBranchDetail(saveId, branchId);
        return Result.success(detail);
    }

    @PutMapping("/{branchId}/mode")
    public Result<BranchDTO> changeManagementMode(@RequestParam("saveId") Long saveId,
                                                  @PathVariable Long branchId,
                                                  @RequestParam("mode") String mode) {
        if (mode == null || mode.isBlank()) {
            return Result.error("请选择管理模式");
        }
        BranchDTO branch = branchService.changeManagementMode(saveId, branchId, mode);
        return Result.success("管理模式更换成功", branch);
    }

    @DeleteMapping("/{branchId}")
    public Result<BranchDTO> closeBranch(@RequestParam("saveId") Long saveId,
                                         @PathVariable Long branchId) {
        BranchDTO branch = branchService.closeBranch(saveId, branchId);
        return Result.success("分校已关闭", branch);
    }
}
