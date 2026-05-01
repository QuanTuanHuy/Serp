import json
import time
import traceback
import logging
from kafka import KafkaConsumer, KafkaProducer
from config import KAFKA_BROKER, INPUT_TOPIC, OUTPUT_TOPIC, CONSUMER_GROUP
from services.routing_service import process_routing_request

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - [%(name)s] - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

def create_producer():
    """Hàm khởi tạo Producer với cơ chế Retry vô tận"""
    while True:
        try:
            logger.info(f"[*] Đang kết nối Kafka Producer tới {KAFKA_BROKER}...")
            producer = KafkaProducer(
                bootstrap_servers=KAFKA_BROKER,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                # Cấu hình thêm để Producer không bao giờ crash khi gửi lỗi
                retries=5, 
                request_timeout_ms=10000 
            )
            logger.info("[+] Kafka Producer kết nối thành công!")
            return producer
        except Exception as e:
            logger.error(f"[!] Lỗi kết nối Producer: {e}. Thử lại sau 5 giây...")
            time.sleep(5) # Đợi 5 giây rồi thử kết nối lại

def create_consumer():
    """Hàm khởi tạo Consumer với cơ chế Retry vô tận"""
    while True:
        try:
            logger.info(f"[*] Đang kết nối Kafka Consumer tới {KAFKA_BROKER}...")
            consumer = KafkaConsumer(
                INPUT_TOPIC,
                bootstrap_servers=KAFKA_BROKER,
                group_id=CONSUMER_GROUP,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                auto_offset_reset='earliest',
                # Tăng thời gian chịu đựng khi mất mạng
                session_timeout_ms=30000,
                heartbeat_interval_ms=10000
            )
            logger.info("[+] Kafka Consumer kết nối thành công!")
            return consumer
        except Exception as e:
            logger.error(f"[!] Lỗi kết nối Consumer: {e}. Thử lại sau 5 giây...")
            time.sleep(5)

def main():
    logger.info("=== KHỞI ĐỘNG ROUTING SERVER ===")
    
    # Vòng lặp ngoài cùng: Đảm bảo Server không bao giờ tắt
    while True:
        try:
            # 1. Khởi tạo kết nối (Sẽ bị block ở đây nếu Kafka chưa lên)
            producer = create_producer()
            consumer = create_consumer()

            logger.info(f"[*] Đang lắng nghe trên topic: {INPUT_TOPIC}...\n")

            # 2. Vòng lặp xử lý message
            for message in consumer:
                try:
                    input_data = message.value
                    plan_id = input_data.get('plan_id', 'UNKNOWN')
                    logger.info(f"[->] Bắt đầu xử lý Plan ID: {plan_id}")

                    # Thực thi logic tính toán
                    output_data = process_routing_request(input_data)

                    # Gửi kết quả
                    producer.send(OUTPUT_TOPIC, value=output_data)
                    producer.flush()
                    logger.info(f"[<-] Đã trả kết quả cho Plan ID: {plan_id}\n")

                except Exception as message_error:
                    # Lỗi ở cấp độ 1 message (VD: sai JSON, lỗi logic) -> Bỏ qua và chạy tiếp
                    logger.error(f"[LỖI DATA] Lỗi khi xử lý message: {message_error}")
                    traceback.print_exc()

        except Exception as fatal_error:
            # Lỗi ở cấp độ Hệ thống (VD: Kafka Broker chết hẳn, đứt cáp mạng)
            logger.error(f"\n[LỖI HỆ THỐNG MẠNG/KAFKA] Mất kết nối: {fatal_error}")
            logger.info("[*] Sắp xếp lại kết nối trong 10 giây...\n")

            # Đóng các kết nối cũ (nếu còn tồn tại) để tránh rò rỉ bộ nhớ
            try:
                consumer.close()
                producer.close()
            except:
                pass
            
            time.sleep(10)

if __name__ == "__main__":
    main()