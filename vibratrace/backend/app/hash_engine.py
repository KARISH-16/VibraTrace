import hashlib

class HashEngine:
    @staticmethod
    def canonical_string(batch_id: str, device_id: str, event_id: str,
                         temp: float, hum: float, c2h4: float, nh3: float,
                         tamper: bool, previous_hash: str) -> str:
        """Deterministic canonical format strictly identical between ESP32, Android and Backend"""
        c2h4_str = f"{c2h4:.3f}" if c2h4 is not None else "0.000"
        return (
            f"batchId:{batch_id},"
            f"deviceId:{device_id},"
            f"eventId:{event_id},"
            f"temp:{temp:.2f},"
            f"hum:{hum:.1f},"
            f"c2h4:{c2h4_str},"
            f"nh3:{nh3:.2f},"
            f"tamper:{'true' if tamper else 'false'},"
            f"prevHash:{previous_hash}"
        )

    @staticmethod
    def calculate_sha256(data_str: str) -> str:
        return hashlib.sha256(data_str.encode("utf-8")).hexdigest()

    @classmethod
    def verify_record(cls, record_dict: dict, previous_hash: str) -> bool:
        canonical = cls.canonical_string(
            batch_id=record_dict["batchId"],
            device_id=record_dict["deviceId"],
            event_id=record_dict["eventId"],
            temp=float(record_dict["temperature"]),
            hum=float(record_dict["humidity"]),
            c2h4=record_dict.get("ethylene"),
            nh3=float(record_dict["ammonia"]),
            tamper=bool(record_dict.get("tamper", False)),
            previous_hash=previous_hash
        )
        expected = cls.calculate_sha256(canonical)
        return expected == record_dict.get("hash")
