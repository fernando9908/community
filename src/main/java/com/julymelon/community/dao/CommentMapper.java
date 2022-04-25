package com.julymelon.community.dao;

import com.julymelon.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 和discusspost一样
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    // 课后作业
    Comment selectCommentById(int id);

    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    int selectCountByUser(int userId);
}
