/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"regexp"
	"strings"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

const (
	MaxNoteContentLength   = 100000
	MaxAttachmentSize      = 10 * 1024 * 1024
	MaxAttachmentsPerNote  = 10
	MaxTotalAttachmentSize = 50 * 1024 * 1024
)

type INoteService interface {
	ValidateNoteData(note *entity.NoteEntity) error
	ValidateNoteOwnership(userID int64, note *entity.NoteEntity) error
	ValidateAttachments(attachments []entity.NoteAttachment) error

	StripMarkdownToPlainText(markdown string) string
	SanitizeMarkdown(markdown string) string

	CreateNote(ctx context.Context, tx *gorm.DB, userID int64, note *entity.NoteEntity) (*entity.NoteEntity, error)
	UpdateNote(ctx context.Context, tx *gorm.DB, userID int64, note *entity.NoteEntity) error
	TogglePin(ctx context.Context, tx *gorm.DB, userID int64, noteID int64) error
	DeleteNote(ctx context.Context, tx *gorm.DB, userID int64, noteID int64) error

	GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error)
	GetNotesByTaskID(ctx context.Context, taskID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByProjectID(ctx context.Context, projectID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
	SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *store.NoteFilter) ([]*entity.NoteEntity, error)
}

type noteService struct {
	notePort store.INotePort
}

func NewNoteService(notePort store.INotePort) INoteService {
	return &noteService{
		notePort: notePort,
	}
}

func (s *noteService) ValidateNoteData(note *entity.NoteEntity) error {
	if note.Content == "" {
		return errors.New(constant.NoteContentRequired)
	}
	if len(note.Content) > MaxNoteContentLength {
		return errors.New(constant.NoteContentTooLong)
	}
	if note.TaskID == nil && note.ProjectID == nil {
		return errors.New(constant.NoteMustAttachToTaskOrProject)
	}
	if note.TaskID != nil && note.ProjectID != nil {
		return errors.New(constant.NoteCannotAttachToBoth)
	}
	if !enum.ActiveStatus(note.ActiveStatus).IsValid() {
		return errors.New("invalid active status")
	}
	return nil
}

func (s *noteService) ValidateNoteOwnership(userID int64, note *entity.NoteEntity) error {
	if note.UserID != userID {
		return errors.New(constant.UpdateNoteForbidden)
	}
	return nil
}

func (s *noteService) ValidateAttachments(attachments []entity.NoteAttachment) error {
	if len(attachments) > MaxAttachmentsPerNote {
		return errors.New(constant.NoteTooManyAttachments)
	}

	var totalSize int64
	for _, attachment := range attachments {
		if attachment.Size > MaxAttachmentSize {
			return errors.New(constant.NoteAttachmentTooLarge)
		}
		totalSize += attachment.Size
	}

	if totalSize > MaxTotalAttachmentSize {
		return errors.New(constant.NoteAttachmentTooLarge)
	}

	return nil
}

func (s *noteService) StripMarkdownToPlainText(markdown string) string {
	text := markdown

	text = regexp.MustCompile(`\!\[.*?\]\(.*?\)`).ReplaceAllString(text, "")
	text = regexp.MustCompile(`\[([^\]]+)\]\([^)]+\)`).ReplaceAllString(text, "$1")
	text = regexp.MustCompile(`#{1,6}\s`).ReplaceAllString(text, "")
	text = regexp.MustCompile(`[\*_]{1,3}([^\*_]+)[\*_]{1,3}`).ReplaceAllString(text, "$1")
	text = regexp.MustCompile("```[\\s\\S]*?```").ReplaceAllString(text, "")
	text = regexp.MustCompile("`([^`]+)`").ReplaceAllString(text, "$1")
	text = regexp.MustCompile(`^\s*[-*+]\s+`).ReplaceAllString(text, "")
	text = regexp.MustCompile(`^\s*\d+\.\s+`).ReplaceAllString(text, "")
	text = regexp.MustCompile(`>\s+`).ReplaceAllString(text, "")
	text = regexp.MustCompile(`\s+`).ReplaceAllString(text, " ")

	return strings.TrimSpace(text)
}

func (s *noteService) SanitizeMarkdown(markdown string) string {
	sanitized := markdown
	sanitized = regexp.MustCompile(`<script[^>]*>.*?</script>`).ReplaceAllString(sanitized, "")
	sanitized = regexp.MustCompile(`<iframe[^>]*>.*?</iframe>`).ReplaceAllString(sanitized, "")
	sanitized = regexp.MustCompile(`javascript:`).ReplaceAllString(sanitized, "")
	sanitized = regexp.MustCompile(`on\w+\s*=`).ReplaceAllString(sanitized, "")

	return sanitized
}

func (s *noteService) CreateNote(ctx context.Context, tx *gorm.DB, userID int64, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	note.UserID = userID
	now := time.Now().UnixMilli()
	note.CreatedAt = now
	note.UpdatedAt = now

	if note.ActiveStatus == "" {
		note.ActiveStatus = string(enum.Active)
	}

	note.Content = s.SanitizeMarkdown(note.Content)
	plainText := s.StripMarkdownToPlainText(note.Content)
	note.ContentPlain = &plainText

	if err := s.ValidateNoteData(note); err != nil {
		return nil, err
	}
	if err := s.ValidateAttachments(note.Attachments); err != nil {
		return nil, err
	}

	note, err := s.notePort.CreateNote(ctx, tx, note)
	if err != nil {
		return nil, err
	}

	return note, nil
}

func (s *noteService) UpdateNote(ctx context.Context, tx *gorm.DB, userID int64, note *entity.NoteEntity) error {
	if err := s.ValidateNoteOwnership(userID, note); err != nil {
		return err
	}

	note.Content = s.SanitizeMarkdown(note.Content)
	plainText := s.StripMarkdownToPlainText(note.Content)
	note.ContentPlain = &plainText

	if err := s.ValidateNoteData(note); err != nil {
		return err
	}
	if err := s.ValidateAttachments(note.Attachments); err != nil {
		return err
	}

	note.UpdatedAt = time.Now().UnixMilli()
	return s.notePort.UpdateNote(ctx, tx, note)
}

func (s *noteService) TogglePin(ctx context.Context, tx *gorm.DB, userID int64, noteID int64) error {
	note, err := s.notePort.GetNoteByID(ctx, noteID)
	if err != nil {
		return err
	}
	if err := s.ValidateNoteOwnership(userID, note); err != nil {
		return err
	}

	return s.notePort.UpdateNotePin(ctx, tx, noteID, !note.IsPinned)
}

func (s *noteService) DeleteNote(ctx context.Context, tx *gorm.DB, userID int64, noteID int64) error {
	note, err := s.notePort.GetNoteByID(ctx, noteID)
	if err != nil {
		return err
	}
	if err := s.ValidateNoteOwnership(userID, note); err != nil {
		return err
	}

	return s.notePort.SoftDeleteNote(ctx, tx, noteID)
}

func (s *noteService) GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error) {
	return s.notePort.GetNoteByID(ctx, noteID)
}

func (s *noteService) GetNotesByTaskID(ctx context.Context, taskID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	return s.notePort.GetNotesByTaskID(ctx, taskID, filter)
}

func (s *noteService) GetNotesByProjectID(ctx context.Context, projectID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	return s.notePort.GetNotesByProjectID(ctx, projectID, filter)
}

func (s *noteService) GetNotesByUserID(ctx context.Context, userID int64, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	return s.notePort.GetNotesByUserID(ctx, userID, filter)
}

func (s *noteService) SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *store.NoteFilter) ([]*entity.NoteEntity, error) {
	if searchQuery == "" {
		return s.notePort.GetNotesByUserID(ctx, userID, filter)
	}
	return s.notePort.SearchNotes(ctx, userID, searchQuery, filter)
}
