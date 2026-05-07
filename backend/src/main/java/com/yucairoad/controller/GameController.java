package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.GameState;
import com.yucairoad.service.GameEngineService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {

    private final GameEngineService gameEngineService;

    public GameController(GameEngineService gameEngineService) {
        this.gameEngineService = gameEngineService;
    }

    @GetMapping("/state")
    public Result<GameState> getGameState(@RequestParam("saveId") Long saveId) {
        GameState state = gameEngineService.getGameState(saveId);
        return Result.success(state);
    }

    @PostMapping("/advance-month")
    public Result<GameState> advanceMonth(@RequestParam("saveId") Long saveId) {
        GameState state = gameEngineService.advanceMonth(saveId);
        return Result.success("时间推进成功", state);
    }

    @PostMapping("/set-speed")
    public Result<GameState> setSpeed(@RequestParam("saveId") Long saveId,
                                      @RequestParam("speed") int speed) {
        GameState state = gameEngineService.setSpeed(saveId, speed);
        return Result.success("速度设置成功", state);
    }

    @PostMapping("/pause")
    public Result<GameState> togglePause(@RequestParam("saveId") Long saveId) {
        GameState state = gameEngineService.togglePause(saveId);
        return Result.success(state);
    }
}
