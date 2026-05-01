import os

# Kafka Config
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "openerp:9092")
INPUT_TOPIC = os.getenv("INPUT_TOPIC", "routing_requests")
OUTPUT_TOPIC = os.getenv("OUTPUT_TOPIC", "routing_results")
CONSUMER_GROUP = os.getenv("CONSUMER_GROUP", "routing_service_group2")

# OSRM Config
OSRM_BASE_URL = os.getenv("OSRM_BASE_URL", "http://openerp:5000")

# OR-Tools Config
ORTOOLS_TIME_LIMIT_SEC = int(os.getenv("ORTOOLS_TIME_LIMIT_SEC", "60"))  # 60 giây