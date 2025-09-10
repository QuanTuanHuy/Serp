package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/service"
)

type ICommentUseCase interface {
	CreateComment(ctx context.Context, userID int64, request *request.CreateCommentDTO) (*entity.CommentEntity, error)
	UpdateComment(ctx context.Context, userID int64, commentID int64, request *request.UpdateCommentDTO) (*entity.CommentEntity, error)
	DeleteComment(ctx context.Context, userID int64, commentID int64) error
	GetCommentByID(ctx context.Context, userID, commentID int64) (*entity.CommentEntity, error)
	GetCommentsByTaskID(ctx context.Context, userID, taskID int64) ([]*entity.CommentEntity, error)
}

type CommentUseCase struct {
	commentService service.ICommentService
	taskService    service.ITaskService
}

func (c *CommentUseCase) GetCommentByID(ctx context.Context, userID int64, commentID int64) (*entity.CommentEntity, error) {
	comment, err := c.commentService.GetCommentByID(ctx, commentID)
	if err != nil {
		return nil, err
	}
	task, err := c.taskService.GetTaskByID(ctx, comment.TaskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to access comment ", commentID)
		return nil, errors.New(constant.GetCommentForbidden)
	}
	return comment, nil
}

func (c *CommentUseCase) CreateComment(ctx context.Context, userID int64, request *request.CreateCommentDTO) (*entity.CommentEntity, error) {
	task, err := c.taskService.GetTaskByID(ctx, request.TaskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to comment on task ", request.TaskID)
		return nil, errors.New(constant.CreateCommentForbidden)
	}
	comment, err := c.commentService.CreateComment(ctx, request)
	if err != nil {
		log.Error(ctx, "Failed to create comment for task ", request.TaskID, " error: ", err)
		return nil, err
	}
	return comment, nil
}

func (c *CommentUseCase) GetCommentsByTaskID(ctx context.Context, userID int64, taskID int64) ([]*entity.CommentEntity, error) {
	task, err := c.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get task by ID ", taskID, " error: ", err)
		return nil, err
	}
	if task.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to access comments for task ", taskID)
		return nil, errors.New(constant.GetCommentForbidden)
	}
	comments, err := c.commentService.GetCommentsByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get comments for task ID ", taskID, " error: ", err)
		return nil, err
	}
	return comments, nil
}

func (c *CommentUseCase) UpdateComment(ctx context.Context, userID int64, commentID int64, request *request.UpdateCommentDTO) (*entity.CommentEntity, error) {
	comment, err := c.commentService.GetCommentByID(ctx, commentID)
	if err != nil {
		return nil, err
	}
	task, err := c.taskService.GetTaskByID(ctx, comment.TaskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to update comment ", commentID)
		return nil, errors.New(constant.UpdateCommentForbidden)
	}
	comment = mapper.UpdateCommentMapper(request, comment)
	comment, err = c.commentService.UpdateComment(ctx, commentID, comment)
	if err != nil {
		log.Error(ctx, "Failed to update comment ID ", commentID, " error: ", err)
		return nil, err
	}
	return comment, nil
}

func (c *CommentUseCase) DeleteComment(ctx context.Context, userID int64, commentID int64) error {
	comment, err := c.commentService.GetCommentByID(ctx, commentID)
	if err != nil {
		return err
	}
	task, err := c.taskService.GetTaskByID(ctx, comment.TaskID)
	if err != nil {
		return err
	}
	if task.UserID != userID {
		return errors.New(constant.DeleteCommentForbidden)
	}
	err = c.commentService.DeleteComment(ctx, commentID)
	if err != nil {
		log.Error(ctx, "Failed to delete comment ID ", commentID, " error: ", err)
		return err
	}
	return nil
}

func NewCommentUseCase(commentService service.ICommentService, taskService service.ITaskService) ICommentUseCase {
	return &CommentUseCase{
		commentService: commentService,
		taskService:    taskService,
	}
}
