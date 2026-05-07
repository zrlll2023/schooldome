package com.yucairoad.service;

import com.yucairoad.dto.GroupStatusDTO;

public interface GroupService {

    GroupStatusDTO getGroupStatus(Long saveId);
}
