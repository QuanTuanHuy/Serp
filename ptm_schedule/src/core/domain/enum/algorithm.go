package enum

type Algorithm string

const (
	HeuristicAlgorithm     Algorithm = "HEURISTIC"
	LocalSearchAlgorithm   Algorithm = "LOCAL_SEARCH"
	MILPAlgorithm          Algorithm = "MILP"
	CPSATAlgorithm         Algorithm = "CP-SAT"
	HybridAlgorithm        Algorithm = "HYBRID"
	DeepOptimizeAlgorithm  Algorithm = "DEEP_OPTIMIZE"  // Uses ptm_optimization with specific strategy
	FallbackChainAlgorithm Algorithm = "FALLBACK_CHAIN" // Uses ptm_optimization with fallback chain
)
