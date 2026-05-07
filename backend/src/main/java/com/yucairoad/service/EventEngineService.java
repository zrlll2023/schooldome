package com.yucairoad.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.dto.*;
import java.util.List;
import java.util.Map;

public interface EventEngineService {

    List<EventDTO> generateMonthlyEvents(Long saveId);

    List<EventDTO> getActiveEvents(Long saveId);

    EventResultDTO processEventChoice(Long eventId, int choiceId, Long saveId);

    Page<EventDTO> getEventHistory(Long saveId, int page, int size, String type);

    List<EventTemplateDTO> getEventTemplates(String type);
}
