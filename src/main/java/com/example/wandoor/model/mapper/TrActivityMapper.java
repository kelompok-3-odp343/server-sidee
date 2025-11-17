package com.example.wandoor.model.mapper;

import com.example.wandoor.model.entity.TrActivity;
import com.example.wandoor.model.response.ActivityListResponse;

public class TrActivityMapper {
        public static ActivityListResponse.ActivityData toDto(TrActivity entity) {
            return ActivityListResponse.ActivityData.builder()
                    .id(entity.getId())
                    .menuId(entity.getMenuId())
                    .menuName(entity.getMenuName())
                    .actionFlow(entity.getActionFlow())
                    .createdTime(entity.getCreatedTime() != null ? entity.getCreatedTime().toString() : null)
                    .createdBy(entity.getCreatedBy())
                    .checkerId(entity.getCheckerId() == null ? "" : entity.getCheckerId())
                    .approverId(entity.getApproverId() == null ? "" : entity.getApproverId())
                    .activityStatus(entity.getStatus())
                    .build();
        }
}
