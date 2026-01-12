/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Test data factory for creating test fixtures
 */

package serp.project.discuss_service.testutil;

import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.MessageType;
import serp.project.discuss_service.core.domain.enums.ScanStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating test data fixtures.
 * Provides consistent test data across all test classes.
 */
public class TestDataFactory {

    // Common test IDs
    public static final Long TENANT_ID = 1L;
    public static final Long USER_ID_1 = 100L;
    public static final Long USER_ID_2 = 200L;
    public static final Long USER_ID_3 = 300L;
    public static final Long CHANNEL_ID = 1000L;
    public static final Long MESSAGE_ID = 2000L;

    // ==================== CHANNEL FACTORIES ====================

    public static ChannelEntity createGroupChannel() {
        return createGroupChannel("Test Group", "Test Description");
    }

    public static ChannelEntity createGroupChannel(String name, String description) {
        long now = Instant.now().toEpochMilli();
        return ChannelEntity.builder()
                .id(CHANNEL_ID)
                .tenantId(TENANT_ID)
                .createdBy(USER_ID_1)
                .name(name)
                .description(description)
                .type(ChannelType.GROUP)
                .isPrivate(false)
                .isArchived(false)
                .memberCount(1)
                .messageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ChannelEntity createDirectChannel() {
        return createDirectChannel(USER_ID_1, USER_ID_2);
    }

    public static ChannelEntity createDirectChannel(Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        long now = Instant.now().toEpochMilli();
        
        return ChannelEntity.builder()
                .id(CHANNEL_ID)
                .tenantId(TENANT_ID)
                .createdBy(smallerId)
                .name(String.format("DM-%d-%d", smallerId, largerId))
                .type(ChannelType.DIRECT)
                .entityId(largerId)
                .isPrivate(true)
                .isArchived(false)
                .memberCount(2)
                .messageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ChannelEntity createTopicChannel(String entityType, Long entityId) {
        long now = Instant.now().toEpochMilli();
        return ChannelEntity.builder()
                .id(CHANNEL_ID)
                .tenantId(TENANT_ID)
                .createdBy(USER_ID_1)
                .name("Topic: " + entityType + "-" + entityId)
                .type(ChannelType.TOPIC)
                .entityType(entityType)
                .entityId(entityId)
                .isPrivate(false)
                .isArchived(false)
                .memberCount(1)
                .messageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ChannelEntity createArchivedChannel() {
        ChannelEntity channel = createGroupChannel();
        channel.setIsArchived(true);
        return channel;
    }

    // ==================== MESSAGE FACTORIES ====================

    public static MessageEntity createTextMessage() {
        return createTextMessage("Test message content");
    }

    public static MessageEntity createTextMessage(String content) {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .id(MESSAGE_ID)
                .channelId(CHANNEL_ID)
                .senderId(USER_ID_1)
                .tenantId(TENANT_ID)
                .content(content)
                .messageType(MessageType.TEXT)
                .mentions(new ArrayList<>())
                .isEdited(false)
                .isDeleted(false)
                .threadCount(0)
                .reactions(new ArrayList<>())
                .readBy(new ArrayList<>())
                .attachments(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static MessageEntity createMessageWithMentions(List<Long> mentionedUserIds) {
        MessageEntity message = createTextMessage("Message with @mentions");
        message.setMentions(new ArrayList<>(mentionedUserIds));
        return message;
    }

    public static MessageEntity createReplyMessage(Long parentId) {
        MessageEntity message = createTextMessage("This is a reply");
        message.setParentId(parentId);
        return message;
    }

    public static MessageEntity createSystemMessage() {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .id(MESSAGE_ID)
                .channelId(CHANNEL_ID)
                .senderId(0L)
                .tenantId(TENANT_ID)
                .content("System notification")
                .messageType(MessageType.SYSTEM)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static MessageEntity createDeletedMessage() {
        MessageEntity message = createTextMessage();
        message.setIsDeleted(true);
        message.setDeletedAt(Instant.now().toEpochMilli());
        message.setDeletedBy(USER_ID_1);
        return message;
    }

    public static MessageEntity createFileMessage(MessageType fileType) {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .id(MESSAGE_ID)
                .channelId(CHANNEL_ID)
                .senderId(USER_ID_1)
                .tenantId(TENANT_ID)
                .content("File caption")
                .messageType(fileType)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== MEMBER FACTORIES ====================

    public static ChannelMemberEntity createOwnerMember() {
        return createMember(USER_ID_1, MemberRole.OWNER);
    }

    public static ChannelMemberEntity createAdminMember() {
        return createMember(USER_ID_2, MemberRole.ADMIN);
    }

    public static ChannelMemberEntity createRegularMember() {
        return createMember(USER_ID_3, MemberRole.MEMBER);
    }

    public static ChannelMemberEntity createGuestMember() {
        return createMember(USER_ID_3, MemberRole.GUEST);
    }

    public static ChannelMemberEntity createMember(Long userId, MemberRole role) {
        long now = Instant.now().toEpochMilli();
        return ChannelMemberEntity.builder()
                .id(userId)
                .channelId(CHANNEL_ID)
                .userId(userId)
                .tenantId(TENANT_ID)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .joinedAt(now)
                .unreadCount(0)
                .isMuted(false)
                .isPinned(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ChannelMemberEntity createLeftMember() {
        ChannelMemberEntity member = createRegularMember();
        member.setStatus(MemberStatus.LEFT);
        member.setLeftAt(Instant.now().toEpochMilli());
        return member;
    }

    public static ChannelMemberEntity createMutedMember() {
        ChannelMemberEntity member = createRegularMember();
        member.setIsMuted(true);
        member.setStatus(MemberStatus.MUTED);
        return member;
    }

    // ==================== ATTACHMENT FACTORIES ====================

    public static AttachmentEntity createImageAttachment() {
        return createAttachment("image.png", "image/png", 1024L);
    }

    public static AttachmentEntity createDocumentAttachment() {
        return createAttachment("document.pdf", "application/pdf", 2048L);
    }

    public static AttachmentEntity createVideoAttachment() {
        return createAttachment("video.mp4", "video/mp4", 10485760L); // 10 MB
    }

    public static AttachmentEntity createAttachment(String fileName, String fileType, Long fileSize) {
        long now = Instant.now().toEpochMilli();
        String extension = fileName.contains(".") 
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() 
                : "";
        return AttachmentEntity.builder()
                .id(1L)
                .messageId(MESSAGE_ID)
                .channelId(CHANNEL_ID)
                .tenantId(TENANT_ID)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .fileExtension(extension)
                .s3Bucket("test-bucket")
                .s3Key("attachments/" + fileName)
                .s3Url("https://s3.example.com/test-bucket/attachments/" + fileName)
                .scanStatus(ScanStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static AttachmentEntity createCleanAttachment() {
        AttachmentEntity attachment = createImageAttachment();
        attachment.markClean();
        return attachment;
    }

    public static AttachmentEntity createInfectedAttachment() {
        AttachmentEntity attachment = createDocumentAttachment();
        attachment.markInfected();
        return attachment;
    }
}
