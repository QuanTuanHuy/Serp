/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type ScheduleWindowResponse struct {
	ID        int64 `json:"id"`
	UserID    int64 `json:"userId"`
	DateMs    int64 `json:"dateMs"`
	StartMin  int   `json:"startMin"`
	EndMin    int   `json:"endMin"`
	CreatedAt int64 `json:"createdAt"`
	UpdatedAt int64 `json:"updatedAt"`
}
