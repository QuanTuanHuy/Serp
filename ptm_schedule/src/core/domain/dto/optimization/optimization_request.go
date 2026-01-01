/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimization Request DTOs
*/

package optimization

// StrategyType represents the optimization algorithm strategy
type StrategyType string

const (
	StrategyAuto        StrategyType = "AUTO"
	StrategyCpSat       StrategyType = "CP_SAT"
	StrategyMilp        StrategyType = "MILP"
	StrategyLocalSearch StrategyType = "LOCAL_SEARCH"
	StrategyHeuristic   StrategyType = "HEURISTIC"
)

// TaskInput represents a task to be scheduled (matches Java TaskInput)
type TaskInput struct {
	TaskID           int64   `json:"taskId"`
	DurationMin      int     `json:"durationMin"`
	PriorityScore    float64 `json:"priorityScore"`
	DeadlineMs       *int64  `json:"deadlineMs,omitempty"`
	EarliestStartMs  *int64  `json:"earliestStartMs,omitempty"`
	Effort           float64 `json:"effort"`
	Enjoyability     float64 `json:"enjoyability"`
	DependentTaskIds []int64 `json:"dependentTaskIds,omitempty"`
}

// Window represents an available time slot (matches Java Window)
type Window struct {
	DateMs     int64 `json:"dateMs"`
	StartMin   int   `json:"startMin"`
	EndMin     int   `json:"endMin"`
	IsDeepWork bool  `json:"isDeepWork"`
}

// Weights represents optimization weights (matches Java Weights)
type Weights struct {
	WPriority float64 `json:"wPriority"`
	WDeadline float64 `json:"wDeadline"`
	WSwitch   float64 `json:"wSwitch"`
	WFatigue  float64 `json:"wFatigue"`
	WEnjoy    float64 `json:"wEnjoy"`
}

// DefaultWeights returns default optimization weights
func DefaultWeights() *Weights {
	return &Weights{
		WPriority: 1.0,
		WDeadline: 0.8,
		WSwitch:   0.3,
		WFatigue:  0.5,
		WEnjoy:    0.4,
	}
}

// Params represents optimization parameters (matches Java Params)
type Params struct {
	SlotMin            *int     `json:"slotMin,omitempty"`
	MaxTimeSec         *int     `json:"maxTimeSec,omitempty"`
	InitialTemperature *float64 `json:"initialTemperature,omitempty"`
	CoolingRate        *float64 `json:"coolingRate,omitempty"`
	MaxIterations      *int     `json:"maxIterations,omitempty"`
}

// DefaultParams returns default optimization parameters
func DefaultParams() *Params {
	slotMin := 15
	maxTimeSec := 30

	return &Params{
		SlotMin:    &slotMin,
		MaxTimeSec: &maxTimeSec,
	}
}

// OptimizationRequest represents the request to ptm_optimization service
type OptimizationRequest struct {
	Tasks   []*TaskInput `json:"tasks"`
	Windows []*Window    `json:"windows"`
	Weights *Weights     `json:"weights"`
	Params  *Params      `json:"params"`
}

// NewOptimizationRequest creates a new request with defaults
func NewOptimizationRequest(tasks []*TaskInput, windows []*Window) *OptimizationRequest {
	return &OptimizationRequest{
		Tasks:   tasks,
		Windows: windows,
		Weights: DefaultWeights(),
		Params:  DefaultParams(),
	}
}
