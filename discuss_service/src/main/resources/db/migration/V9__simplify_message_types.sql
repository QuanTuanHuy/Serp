/*
Author: QuanTuanHuy
Description: Part of Serp Project - Simplify message types to STANDARD and SYSTEM only

This migration:
1. Removes the old valid_message_type constraint (TEXT, IMAGE, FILE, SYSTEM, CODE, POLL)
2. Removes the content_not_empty constraint (no longer needed with new validation)
3. Converts existing message types to new simplified values (STANDARD, SYSTEM)
4. Makes content column nullable (messages can have only attachments)
5. Adds new constraints for the simplified message type system
*/

-- Step 1: Drop old constraints
ALTER TABLE messages DROP CONSTRAINT IF EXISTS valid_message_type;
ALTER TABLE messages DROP CONSTRAINT IF EXISTS content_not_empty;

-- Step 2: Convert existing message types to new values
-- TEXT, IMAGE, FILE, CODE, POLL -> STANDARD
-- SYSTEM stays as SYSTEM
UPDATE messages 
SET message_type = 'STANDARD' 
WHERE message_type IN ('TEXT', 'IMAGE', 'FILE', 'CODE', 'POLL');

-- Step 3: Make content nullable (messages can have only attachments)
ALTER TABLE messages ALTER COLUMN content DROP NOT NULL;

-- Step 4: Add new constraint for simplified message types
ALTER TABLE messages ADD CONSTRAINT valid_message_type CHECK (
    message_type IN ('STANDARD', 'SYSTEM')
);

-- Step 5: Add constraint to ensure SYSTEM messages have content
-- (STANDARD messages are validated at application level - must have content OR attachments)
ALTER TABLE messages ADD CONSTRAINT system_message_requires_content CHECK (
    message_type != 'SYSTEM' OR (content IS NOT NULL AND char_length(trim(content)) > 0)
);

-- Add comment for documentation
COMMENT ON COLUMN messages.message_type IS 'Message type: STANDARD (user messages with optional text/attachments), SYSTEM (system notifications)';
COMMENT ON COLUMN messages.content IS 'Message text content. Required for SYSTEM messages, optional for STANDARD if attachments exist';
