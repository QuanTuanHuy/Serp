import os

# Kafka Config
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "openerp:9092")
INPUT_TOPIC = os.getenv("INPUT_TOPIC", "routing_requests")
OUTPUT_TOPIC = os.getenv("OUTPUT_TOPIC", "routing_results")
CONSUMER_GROUP = os.getenv("CONSUMER_GROUP", "routing_service_group7")
MAX_POLL_INTERVAL_MS = int(os.getenv("MAX_POLL_INTERVAL_MS", "360000"))  # 6 phút
SESSION_TIMEOUT_MS = int(os.getenv("SESSION_TIMEOUT_MS", "45000"))  # 45 giây
HEARTBEAT_INTERVAL_MS = int(os.getenv("HEARTBEAT_INTERVAL_MS", "10000"))  # 10 giây

# OSRM Config
OSRM_BASE_URL = os.getenv("OSRM_BASE_URL", "http://router.project-osrm.org")

# OR-Tools Config
ORTOOLS_TIME_LIMIT_SEC = int(os.getenv("ORTOOLS_TIME_LIMIT_SEC", "60"))  # 60 giây

# Algorithm Config
VOLUME_SCALE_FACTOR = int(os.getenv("VOLUME_SCALE_FACTOR", "1000000"))