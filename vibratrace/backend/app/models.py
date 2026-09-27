import enum
from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Text, Enum
)
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()

class UserRole(str, enum.Enum):
    FARMER = "FARMER"
    TRANSPORTER = "TRANSPORTER"
    STORAGE_OPERATOR = "STORAGE_OPERATOR"
    DISTRIBUTOR = "DISTRIBUTOR"
    RETAILER = "RETAILER"
    ADMIN = "ADMIN"

class ShipmentStage(str, enum.Enum):
    FARM = "FARM"
    PACKING = "PACKING"
    TRANSPORT = "TRANSPORT"
    COLD_STORAGE = "COLD_STORAGE"
    DISTRIBUTION = "DISTRIBUTION"
    BUYER = "BUYER"

class ConditionStatus(str, enum.Enum):
    GOOD = "GOOD"
    WARNING = "WARNING"
    CRITICAL = "CRITICAL"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String(120), nullable=False)
    email = Column(String(120), unique=True, index=True, nullable=False)
    phone = Column(String(30), nullable=False)
    hashed_password = Column(String(255), nullable=False)
    role = Column(Enum(UserRole), default=UserRole.FARMER, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class Device(Base):
    __tablename__ = "devices"

    id = Column(String(64), primary_key=True, index=True) # e.g. VT-ESP32-001
    assigned_batch_id = Column(String(64), nullable=True)
    firmware_version = Column(String(32), default="v1.4.2")
    battery_level = Column(Integer, default=100)
    gsm_signal = Column(Integer, default=-72)
    network_status = Column(String(32), default="ONLINE") # ONLINE, OFFLINE, RECONNECTING
    mqtt_status = Column(String(32), default="ONLINE")
    sd_card_status = Column(String(32), default="ONLINE")
    enclosure_status = Column(String(32), default="CLOSED")
    tamper_detected = Column(Boolean, default=False)
    last_communication = Column(DateTime, default=datetime.utcnow)
    created_at = Column(DateTime, default=datetime.utcnow)

class Batch(Base):
    __tablename__ = "batches"

    id = Column(String(64), primary_key=True, index=True) # e.g. FD2026-001
    product_name = Column(String(120), nullable=False)
    product_category = Column(String(64), default="Fresh Produce")
    farmer_supplier = Column(String(120), nullable=False)
    quantity = Column(Float, default=100.0)
    unit = Column(String(20), default="kg")
    harvest_date = Column(String(32), nullable=False)
    packing_date = Column(String(32), nullable=False)
    destination = Column(String(120), nullable=False)
    transporter = Column(String(120), nullable=False)
    assigned_device_id = Column(String(64), ForeignKey("devices.id"), nullable=True)
    current_stage = Column(Enum(ShipmentStage), default=ShipmentStage.FARM)
    qr_token = Column(String(128), unique=True, index=True)
    condition_status = Column(Enum(ConditionStatus), default=ConditionStatus.GOOD)
    integrity_status = Column(String(32), default="VERIFIED")
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class SensorReading(Base):
    __tablename__ = "sensor_readings"

    id = Column(Integer, primary_key=True, index=True)
    batch_id = Column(String(64), ForeignKey("batches.id"), index=True, nullable=False)
    device_id = Column(String(64), nullable=False)
    event_id = Column(String(64), unique=True, index=True, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    temperature = Column(Float, nullable=False)
    humidity = Column(Float, nullable=False)
    ethylene = Column(Float, nullable=True) # optional
    ammonia = Column(Float, nullable=False)
    battery = Column(Integer, default=100)
    tamper = Column(Boolean, default=False)
    signal = Column(Integer, default=-72)
    hash = Column(String(64), nullable=False)
    previous_hash = Column(String(64), nullable=False)

class SupplyChainEvent(Base):
    __tablename__ = "supply_chain_events"

    id = Column(Integer, primary_key=True, index=True)
    batch_id = Column(String(64), ForeignKey("batches.id"), index=True, nullable=False)
    stage = Column(Enum(ShipmentStage), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    location = Column(String(120), default="Location unavailable")
    responsible_party = Column(String(120), nullable=False)
    status = Column(String(64), default="COMPLETED")
    sensor_condition = Column(String(32), default="NORMAL")
    verification_status = Column(String(32), default="VERIFIED")
    hash = Column(String(64), nullable=True)

class TamperEvent(Base):
    __tablename__ = "tamper_events"

    id = Column(Integer, primary_key=True, index=True)
    event_id = Column(String(64), unique=True, index=True, nullable=False)
    batch_id = Column(String(64), ForeignKey("batches.id"), nullable=False)
    device_id = Column(String(64), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    location = Column(String(120), default="Location unavailable")
    severity = Column(String(32), default="HIGH")
    hash = Column(String(64), nullable=False)
    sync_status = Column(String(32), default="SYNCED")
    acknowledged = Column(Boolean, default=False)

class Alert(Base):
    __tablename__ = "alerts"

    id = Column(Integer, primary_key=True, index=True)
    batch_id = Column(String(64), nullable=False)
    device_id = Column(String(64), nullable=False)
    alert_type = Column(String(64), nullable=False) # TEMP_HIGH, ETHYLENE_HIGH, TAMPER, etc.
    severity = Column(String(32), default="CRITICAL") # INFO, WARNING, CRITICAL
    message = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    is_resolved = Column(Boolean, default=False)
    resolved_at = Column(DateTime, nullable=True)

class HashRecord(Base):
    __tablename__ = "hash_records"

    id = Column(Integer, primary_key=True, index=True)
    record_id = Column(String(64), unique=True, index=True, nullable=False)
    batch_id = Column(String(64), nullable=False)
    device_id = Column(String(64), nullable=False)
    current_hash = Column(String(64), nullable=False)
    previous_hash = Column(String(64), nullable=False)
    event_type = Column(String(64), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    verification_status = Column(String(32), default="VERIFIED") # VERIFIED, MISMATCH

class BlockchainRecord(Base):
    __tablename__ = "blockchain_records"

    id = Column(Integer, primary_key=True, index=True)
    batch_id = Column(String(64), nullable=False)
    record_id = Column(String(64), nullable=False)
    hash = Column(String(64), nullable=False)
    transaction_id = Column(String(128), unique=True, nullable=False)
    ledger_type = Column(String(32), default="DEMO_LEDGER")
    timestamp = Column(DateTime, default=datetime.utcnow)
    status = Column(String(32), default="COMMITTED")

class EnergyEvent(Base):
    __tablename__ = "energy_events"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(64), nullable=False)
    battery_percentage = Column(Integer, default=85)
    vibration_detected = Column(Boolean, default=True)
    harvesting_active = Column(Boolean, default=True)
    charging_status = Column(String(32), default="SUPPLEMENTARY_CHARGING")
    timestamp = Column(DateTime, default=datetime.utcnow)

class AppSettings(Base):
    __tablename__ = "app_settings"

    id = Column(Integer, primary_key=True)
    temp_min = Column(Float, default=2.0)
    temp_max = Column(Float, default=10.0)
    humidity_min = Column(Float, default=60.0)
    humidity_max = Column(Float, default=85.0)
    ethylene_threshold = Column(Float, default=0.5)
    nh3_threshold = Column(Float, default=2.0)
    battery_warning = Column(Integer, default=20)
    sampling_interval = Column(Integer, default=5)
    transmission_interval = Column(Integer, default=15)
    language = Column(String(10), default="en")
    demo_mode = Column(Boolean, default=False)
