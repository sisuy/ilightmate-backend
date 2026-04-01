package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmBehavioralEventMapper {

    @Insert("INSERT INTO ilm_behavioral_events (session_id, user_id, event_type, companion_id, payload, event_time) " +
            "VALUES (#{sessionId}, #{userId}, #{eventType}, #{companionId}, #{payload}, #{eventTime})")
    void insert(@Param("sessionId") String sessionId,
                @Param("userId") Long userId,
                @Param("eventType") String eventType,
                @Param("companionId") String companionId,
                @Param("payload") String payload,
                @Param("eventTime") String eventTime);
}
