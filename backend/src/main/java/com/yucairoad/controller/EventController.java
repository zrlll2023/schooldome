package com.yucairoad.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.common.Result;
import com.yucairoad.dto.*;
import com.yucairoad.service.EventEngineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventEngineService eventEngineService;

    public EventController(EventEngineService eventEngineService) {
        this.eventEngineService = eventEngineService;
    }

    @GetMapping("/active")
    public Result<List<EventDTO>> getActiveEvents(@RequestParam("saveId") Long saveId) {
        List<EventDTO> events = eventEngineService.getActiveEvents(saveId);
        return Result.success(events);
    }

    @PostMapping("/{eventId}/resolve")
    public Result<EventResultDTO> resolveEvent(
            @PathVariable Long eventId,
            @RequestParam("saveId") Long saveId,
            @RequestParam("choiceId") int choiceId) {
        EventResultDTO result = eventEngineService.processEventChoice(eventId, choiceId, saveId);
        return Result.success(result);
    }

    @GetMapping("/history")
    public Result<Page<EventDTO>> getEventHistory(
            @RequestParam("saveId") Long saveId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "type", required = false) String type) {
        Page<EventDTO> history = eventEngineService.getEventHistory(saveId, page, size, type);
        return Result.success(history);
    }

    @GetMapping("/templates")
    public Result<List<EventTemplateDTO>> getEventTemplates(
            @RequestParam(value = "type", required = false) String type) {
        List<EventTemplateDTO> templates = eventEngineService.getEventTemplates(type);
        return Result.success(templates);
    }
}
