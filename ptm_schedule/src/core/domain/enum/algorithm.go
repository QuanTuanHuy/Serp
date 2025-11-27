package enum

type Algorithm string

const (
	HeuristicAlgorithm Algorithm = "HEURISTIC"
	CPSATAlgorithm     Algorithm = "CP-SAT"
	HybridAlgorithm    Algorithm = "HYBRID"
)
