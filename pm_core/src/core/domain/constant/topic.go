package constant

type KafkaTopic string

const (
	PROJECT_TOPIC   KafkaTopic = "serp.pm.project.events"
	WORKITEM_TOPIC  KafkaTopic = "serp.pm.workitem.events"
	SPRINT_TOPIC    KafkaTopic = "serp.pm.sprint.events"
	MILESTONE_TOPIC KafkaTopic = "serp.pm.milestone.events"
	COMMENT_TOPIC   KafkaTopic = "serp.pm.comment.events"
	MEMBER_TOPIC    KafkaTopic = "serp.pm.member.events"
)
