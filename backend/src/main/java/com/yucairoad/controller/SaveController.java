package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.entity.GameSave;
import com.yucairoad.service.GameSaveService;
import com.yucairoad.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/save")
public class SaveController {

    private final GameSaveService gameSaveService;

    public SaveController(GameSaveService gameSaveService) {
        this.gameSaveService = gameSaveService;
    }

    @PostMapping("/new")
    public Result<GameSave> createSave(@RequestParam("save_name") String saveName) {
        Long userId = SecurityUtils.getCurrentUserId();
        GameSave gameSave = gameSaveService.createSave(userId, saveName);
        return Result.success("存档创建成功", gameSave);
    }

    @GetMapping("/list")
    public Result<List<GameSave>> getSaveList() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<GameSave> saveList = gameSaveService.getSaveList(userId);
        return Result.success(saveList);
    }

    @GetMapping("/{id}")
    public Result<GameSave> getSaveById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        GameSave gameSave = gameSaveService.getSaveById(userId, id);
        return Result.success(gameSave);
    }

    @PutMapping("/{id}")
    public Result<GameSave> updateSave(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String gameState = body.get("game_state");
        if (gameState == null || gameState.isBlank()) {
            throw new IllegalArgumentException("game_state不能为空");
        }
        GameSave gameSave = gameSaveService.updateSave(userId, id, gameState);
        return Result.success("存档保存成功", gameSave);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSave(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        gameSaveService.deleteSave(userId, id);
        return Result.success("存档删除成功", null);
    }
}
