import json
import logging
from typing import Callable, List

logger = logging.getLogger("mqtt")

class MockOrRealMQTTClient:
    def __init__(self, broker: str = "localhost", port: int = 1883):
        self.broker = broker
        self.port = port
        self.subscribers: List[Callable[[str, dict], None]] = []
        self.connected = False

    def add_subscriber(self, callback: Callable[[str, dict], None]):
        self.subscribers.append(callback)

    def publish(self, topic: str, payload: dict):
        logger.info(f"MQTT Publish [{topic}]: {payload}")
        for sub in self.subscribers:
            try:
                sub(topic, payload)
            except Exception as e:
                logger.error(f"Subscriber dispatch error: {e}")

mqtt_client = MockOrRealMQTTClient()
