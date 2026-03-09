/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message repository
 */

package serp.project.discuss_service.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.discuss_service.infrastructure.store.model.MessageModel;

import java.util.List;

@Repository
public interface IMessageRepository extends IBaseRepository<MessageModel> {

    Page<MessageModel> findByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    @Query("SELECT m FROM MessageModel m WHERE m.channelId = :channelId AND m.isDeleted = false " +
           "AND m.id < :beforeId ORDER BY m.createdAt DESC")
    List<MessageModel> findMessagesBeforeId(@Param("channelId") Long channelId,
                                            @Param("beforeId") Long beforeId,
                                            Pageable pageable);

    List<MessageModel> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentId);

    List<MessageModel> findBySenderIdAndIsDeletedFalse(Long senderId, Pageable pageable);

    @Query(value = "SELECT * FROM messages m WHERE :userId = ANY(m.mentions) " +
                   "AND m.is_deleted = false ORDER BY m.created_at DESC", nativeQuery = true)
    List<MessageModel> findByMentioningUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MessageModel m WHERE m.channelId = :channelId " +
           "AND m.id > :afterMessageId AND m.isDeleted = false")
    long countUnreadMessages(@Param("channelId") Long channelId, @Param("afterMessageId") Long afterMessageId);

    // Full-text search
    @Query(value = "SELECT * FROM messages m WHERE m.channel_id = :channelId " +
                   "AND m.search_vector @@ plainto_tsquery('english', :query) " +
                   "AND m.is_deleted = false ORDER BY ts_rank(m.search_vector, plainto_tsquery('english', :query)) DESC",
           nativeQuery = true)
    List<MessageModel> searchMessages(@Param("channelId") Long channelId,
                                      @Param("query") String query,
                                      Pageable pageable);

    @Modifying
    @Query("UPDATE MessageModel m SET m.isDeleted = true, m.deletedAt = :deletedAt WHERE m.channelId = :channelId")
    int softDeleteByChannelId(@Param("channelId") Long channelId, @Param("deletedAt") Long deletedAt);

    long countByChannelIdAndIsDeletedFalse(Long channelId);
}
