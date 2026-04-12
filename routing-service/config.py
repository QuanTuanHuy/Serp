import os

# Kafka Config
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9092")
INPUT_TOPIC = os.getenv("INPUT_TOPIC", "routing_requests")
OUTPUT_TOPIC = os.getenv("OUTPUT_TOPIC", "routing_results")
CONSUMER_GROUP = os.getenv("CONSUMER_GROUP", "routing_service_group")

# OSRM Config
OSRM_BASE_URL = os.getenv("OSRM_BASE_URL", "http://localhost:5000")