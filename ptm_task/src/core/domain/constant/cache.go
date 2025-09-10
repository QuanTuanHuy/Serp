package constant

const (
	DefaultTTL = 60 * 60
	ShortTTL   = 5 * 60
)

const (
	ProjectsByUserID      = "ptm:users:%d:projects"
	GroupTasksByProjectID = "ptm:projects:%d:group-tasks"
)
