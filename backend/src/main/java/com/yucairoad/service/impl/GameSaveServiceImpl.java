package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.entity.GameSave;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.service.GameSaveService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSaveServiceImpl implements GameSaveService {

    private static final int MAX_SAVE_COUNT = 10;

    private final GameSaveMapper gameSaveMapper;

    public GameSaveServiceImpl(GameSaveMapper gameSaveMapper) {
        this.gameSaveMapper = gameSaveMapper;
    }

    @Override
    public GameSave createSave(Long userId, String saveName) {
        LambdaQueryWrapper<GameSave> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(GameSave::getUserId, userId);
        Long count = gameSaveMapper.selectCount(countWrapper);
        if (count >= MAX_SAVE_COUNT) {
            throw new BusinessException("存档数量已达上限，最多支持" + MAX_SAVE_COUNT + "个存档");
        }

        String initialGameState = "{\"currentYear\":1,\"currentMonth\":9,\"school\":null}";

        GameSave gameSave = new GameSave();
        gameSave.setUserId(userId);
        gameSave.setSaveName(saveName);
        gameSave.setCurrentYear(1);
        gameSave.setCurrentMonth(9);
        gameSave.setGameState(initialGameState);
        gameSave.setIsActive(1);
        gameSave.setCreatedAt(LocalDateTime.now());
        gameSave.setUpdatedAt(LocalDateTime.now());

        deactivateOtherSaves(userId);
        gameSaveMapper.insert(gameSave);

        return gameSave;
    }

    @Override
    public List<GameSave> getSaveList(Long userId) {
        LambdaQueryWrapper<GameSave> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameSave::getUserId, userId)
               .orderByDesc(GameSave::getUpdatedAt);
        return gameSaveMapper.selectList(wrapper);
    }

    @Override
    public GameSave getSaveById(Long userId, Long saveId) {
        GameSave gameSave = gameSaveMapper.selectById(saveId);
        if (gameSave == null) {
            throw new BusinessException("存档不存在");
        }
        if (!gameSave.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此存档");
        }

        deactivateOtherSaves(userId);

        gameSave.setIsActive(1);
        gameSave.setUpdatedAt(LocalDateTime.now());
        gameSaveMapper.updateById(gameSave);

        return gameSave;
    }

    @Override
    public GameSave updateSave(Long userId, Long saveId, String gameState) {
        GameSave gameSave = gameSaveMapper.selectById(saveId);
        if (gameSave == null) {
            throw new BusinessException("存档不存在");
        }
        if (!gameSave.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此存档");
        }

        gameSave.setGameState(gameState);
        gameSave.setUpdatedAt(LocalDateTime.now());
        gameSaveMapper.updateById(gameSave);

        return gameSave;
    }

    @Override
    public void deleteSave(Long userId, Long saveId) {
        GameSave gameSave = gameSaveMapper.selectById(saveId);
        if (gameSave == null) {
            throw new BusinessException("存档不存在");
        }
        if (!gameSave.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此存档");
        }
        gameSaveMapper.deleteById(saveId);
    }

    private void deactivateOtherSaves(Long userId) {
        LambdaUpdateWrapper<GameSave> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GameSave::getUserId, userId)
                     .eq(GameSave::getIsActive, 1)
                     .set(GameSave::getIsActive, 0);
        gameSaveMapper.update(null, updateWrapper);
    }
}
