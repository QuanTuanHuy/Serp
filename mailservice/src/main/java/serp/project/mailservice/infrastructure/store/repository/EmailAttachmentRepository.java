/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.mailservice.infrastructure.store.model.EmailAttachmentModel;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailAttachmentRepository extends JpaRepository<EmailAttachmentModel, Long> {
    List<EmailAttachmentModel> findByEmailId(Long emailId);

    List<EmailAttachmentModel> findByExpiresAtBefore(LocalDateTime expiresAt);

    void deleteByEmailId(Long emailId);

    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM EmailAttachmentModel e WHERE e.emailId = :emailId")
    long getTotalSizeByEmailId(@Param("emailId") Long emailId);
}
