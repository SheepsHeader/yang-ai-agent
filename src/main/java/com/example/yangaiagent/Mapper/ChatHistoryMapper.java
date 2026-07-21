package com.example.yangaiagent.Mapper;


import com.example.yangaiagent.entity.ChatHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatHistoryMapper {

    @Insert("insert into chat_history (conversation_id, user_id, message, message_type, is_delete, create_time) values (#{conversationId}, #{userId}, #{message}, #{messageType}, #{isDelete}, #{createTime})")
    int insert(ChatHistory chatHistory);

    @Select("select * from chat_history where conversation_id = #{conversationId} and user_id = #{userId} and is_delete = 0")
    List<ChatHistory> selectByConversationIdAndUserId(String conversationId, String userId);

    @Update("update chat_history set is_delete = 1 where conversation_id = #{conversationId} and user_id = #{userId}")
    void deleteByConversationIdAndUserId(String conversationId, String number);
}
