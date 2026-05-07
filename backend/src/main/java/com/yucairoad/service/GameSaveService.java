package com.yucairoad.service;

import com.yucairoad.entity.GameSave;
import java.util.List;

public interface GameSaveService {

    GameSave createSave(Long userId, String saveName);

    List<GameSave> getSaveList(Long userId);

    GameSave getSaveById(Long userId, Long saveId);

    GameSave updateSave(Long userId, Long saveId, String gameState);

    void deleteSave(Long userId, Long saveId);
}
