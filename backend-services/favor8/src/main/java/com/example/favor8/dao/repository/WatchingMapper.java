package com.example.favor8.dao.repository;

import com.example.favor8.dao.entity.WatchingLogPo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WatchingMapper {
    @Select("SELECT watching.user_id, rr.status, movie_title, minute, rr.requested_at " +
            "FROM watching " +
            "    JOIN recommendation_request rr ON watching.user_id = rr.user_id " +
            "WHERE rr.user_id = #{userId}; ")
    // the results annotation is required only if we use a customized object (WatchingLogPo).
    // if the results can be stored in string, integer, boolean, etc. then no need to use the Results annotation.
    @Results({
            @Result(property = "userId", column = "userId"),
            @Result(property = "status", column = "status"),
            @Result(property = "movieTitle", column = "movie_title"),
            @Result(property = "minute", column = "minute"),
            @Result(property = "requestedAt", column = "requested_at")
    })
    List<WatchingLogPo> findWatchingLogsByUserId(@Param("userId") Integer userId);
}
