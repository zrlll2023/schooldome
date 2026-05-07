package com.yucairoad.service;

import com.yucairoad.dto.GameState;

public interface GameEngineService {

    GameState advanceMonth(Long saveId);

    GameState getGameState(Long saveId);

    GameState setSpeed(Long saveId, int speed);

    GameState togglePause(Long saveId);
}
