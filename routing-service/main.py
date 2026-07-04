import json
import time
import traceback
import logging
from confluent_kafka import Consumer, Producer, KafkaError, KafkaException
from config import KAFKA_BROKER, INPUT_TOPIC, OUTPUT_TOPIC, CONSUMER_GROUP, MAX_POLL_INTERVAL_MS, SESSION_TIMEOUT_MS, HEARTBEAT_INTERVAL_MS
from services.routing_service import process_routing_request

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - [%(name)s] - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

def create_producer():
    """Hàm khởi tạo Producer bằng confluent-kafka"""
    while True:
        try:
            logger.info(f"[*] Đang kết nối Kafka Producer tới {KAFKA_BROKER}...")
            
            conf = {
                'bootstrap.servers': KAFKA_BROKER,
                'message.timeout.ms': 10000,
                'retries': 5
            }
            producer = Producer(conf)
            logger.info("[+] Kafka Producer kết nối thành công!")
            return producer
        except Exception as e:
            logger.error(f"[!] Lỗi kết nối Producer: {e}. Thử lại sau 5 giây...")
            time.sleep(5)

def create_consumer():
    """Hàm khởi tạo Consumer bằng confluent-kafka"""
    while True:
        try:
            logger.info(f"[*] Đang kết nối Kafka Consumer tới {KAFKA_BROKER}...")
            
            conf = {
                'bootstrap.servers': KAFKA_BROKER,
                'group.id': CONSUMER_GROUP,
                'auto.offset.reset': 'earliest',
                'enable.auto.commit': False,  # Vẫn giữ nguyên cơ chế tự commit thủ công
                'max.poll.interval.ms': MAX_POLL_INTERVAL_MS,
                'session.timeout.ms': SESSION_TIMEOUT_MS,
                'heartbeat.interval.ms': HEARTBEAT_INTERVAL_MS
            }
            consumer = Consumer(conf)
            
            consumer.subscribe([INPUT_TOPIC])
            
            logger.info("[+] Kafka Consumer kết nối thành công!")
            return consumer
        except Exception as e:
            logger.error(f"[!] Lỗi kết nối Consumer: {e}. Thử lại sau 5 giây...")
            time.sleep(5)

def main():
    logger.info("=== KHỞI ĐỘNG ROUTING SERVER ===")
    
    while True:
        try:
            producer = create_producer()
            consumer = create_consumer()

            logger.info(f"[*] Đang lắng nghe trên topic: {INPUT_TOPIC}...\n")

            while True:
                msg = consumer.poll(timeout=2.0)

                if msg is None:
                    continue

                # Xử lý lỗi cấp độ message/network
                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        # Sự kiện đọc đến cuối Partition -> Bỏ qua
                        continue
                    else:
                        raise KafkaException(msg.error())

                # Nếu message hợp lệ, bắt đầu trích xuất data
                try:
                    input_data = json.loads(msg.value().decode('utf-8'))
                    plan_id = input_data.get('plan_id', 'UNKNOWN')
                    
                    logger.info(f"[->] Bắt đầu xử lý Plan ID: {plan_id}")

                    # Thực thi logic tính toán
                    output_data = process_routing_request(input_data)

                    # Gửi kết quả (Tự dumps JSON và encode)
                    producer.produce(
                        OUTPUT_TOPIC, 
                        value=json.dumps(output_data).encode('utf-8')
                    )
                    producer.flush()
                    logger.info(f"[<-] Đã trả kết quả cho Plan ID: {plan_id}")

                    # Xác nhận hoàn thành (asynchronous=False tương đương sync commit)
                    consumer.commit(asynchronous=False)
                    logger.info(f"[V] Đã Commit thành công Plan ID: {plan_id}\n")

                except Exception as message_error:
                    logger.error(f"[LỖI DATA] Lỗi khi xử lý message: {message_error}")
                    traceback.print_exc()
                    
                    try:
                        consumer.commit(asynchronous=False)
                    except Exception as commit_err:
                        logger.error(f"Lỗi khi cố commit message lỗi: {commit_err}")

        except Exception as fatal_error:
            logger.error(f"\n[LỖI HỆ THỐNG MẠNG/KAFKA] Mất kết nối: {fatal_error}")
            logger.info("[*] Sắp xếp lại kết nối trong 10 giây...\n")
            
            try:
                consumer.close()
                producer.flush()
            except:
                pass
            
            time.sleep(10)

if __name__ == "__main__":
    main()