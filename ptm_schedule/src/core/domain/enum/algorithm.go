package enum

type Algorithm string

const (
	HeuristicAlgorithm   Algorithm = "HEURISTIC"
	LocalSearchAlgorithm Algorithm = "LOCAL_SEARCH"
	MILPAlgorithm        Algorithm = "MILP"
	CPSATAlgorithm       Algorithm = "CP-SAT"
	HybridAlgorithm      Algorithm = "HYBRID"
)
