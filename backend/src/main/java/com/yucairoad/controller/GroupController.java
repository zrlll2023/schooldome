package com.yucairoad.controller;

import com.yucairoad.common.Result;
import com.yucairoad.dto.GroupStatusDTO;
import com.yucairoad.service.GroupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/status")
    public Result<GroupStatusDTO> getGroupStatus(@RequestParam("saveId") Long saveId) {
        GroupStatusDTO status = groupService.getGroupStatus(saveId);
        return Result.success(status);
    }
}
