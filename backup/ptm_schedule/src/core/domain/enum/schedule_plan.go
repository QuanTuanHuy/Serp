package enum

type PlanStatus string

const (
	PlanDraft      PlanStatus = "DRAFT"
	PlanProcessing PlanStatus = "PROCESSING"
	PlanProposed   PlanStatus = "PROPOSED"
	PlanActive     PlanStatus = "ACTIVE"
	PlanCompleted  PlanStatus = "COMPLETED"
	PlanArchived   PlanStatus = "ARCHIVED"
	PlanDiscarded  PlanStatus = "DISCARDED"
	PlanFailed     PlanStatus = "FAILED"
)

func (ps PlanStatus) IsValid() bool {
	switch ps {
	case PlanDraft, PlanProcessing, PlanProposed, PlanActive, PlanCompleted, PlanArchived, PlanDiscarded, PlanFailed:
		return true
	default:
		return false
	}
}

func (ps PlanStatus) IsFinal() bool {
	return ps == PlanCompleted || ps == PlanArchived || ps == PlanDiscarded || ps == PlanFailed
}

func (ps PlanStatus) CanTransitionTo(target PlanStatus) bool {
	if ps == target {
		return false
	}

	switch ps {
	// 1. Từ DRAFT (Mới tạo)
	case PlanDraft:
		return target == PlanProcessing || // Bắt đầu chạy
			target == PlanDiscarded // Hủy ngay khi tạo

	// 2. Từ PROCESSING (Đang chạy)
	case PlanProcessing:
		return target == PlanProposed || // Chạy xong, ra kết quả chờ duyệt
			target == PlanActive || // Chạy xong, Auto-apply luôn
			target == PlanFailed // Lỗi

	// 3. Từ PROPOSED (Đang xem trước)
	case PlanProposed:
		return target == PlanActive || // User bấm "Apply"
			target == PlanDiscarded || // User bấm "Cancel"
			target == PlanProcessing // User bấm "Re-optimize" (chạy lại)

	// 4. Từ ACTIVE (Đang chạy chính thức)
	case PlanActive:
		return target == PlanArchived || // Có bản mới đè lên
			target == PlanCompleted || // Hết thời gian (Hết tuần)
			target == PlanProcessing // Trigger Re-schedule (Incremental Update)

		// 5. Từ FAILED (Lỗi)
	case PlanFailed:
		return target == PlanProcessing || // Retry
			target == PlanDiscarded // Bỏ cuộc

	// 6. Các trạng thái kết thúc (Terminal States) - Không thể đổi đi đâu được nữa
	case PlanCompleted, PlanArchived, PlanDiscarded:
		return false

	default:
		return false
	}
}
