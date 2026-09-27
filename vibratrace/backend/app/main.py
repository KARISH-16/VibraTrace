import json
from typing import List
from datetime import datetime
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from app.database import engine, get_db
from app.models import Base, Batch, Device, SensorReading, Alert, SupplyChainEvent, TamperEvent, HashRecord, BlockchainRecord, User, UserRole
from app.schemas import (
    UserRegister, UserLogin, Token, UserOut,
    SensorReadingPayload, BatchCreate, BatchUpdate, SyncPayload,
    HashVerifyRequest, BlockchainSubmitRequest
)
from app.security import verify_password, get_password_hash, create_access_token, decode_token
from app.hash_engine import HashEngine
from app.blockchain_service import ledger_service
from app.mqtt_client import mqtt_client

Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="VibraTrace Full-Stack IoT API",
    description="Sense → Secure → Store → Sync → Verify → Trace (Farm-to-Fork)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# WebSocket Connection Manager
class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        dead_connections = []
        for connection in self.active_connections:
            try:
                await connection.send_text(json.dumps(message))
            except Exception:
                dead_connections.append(connection)
        for dead in dead_connections:
            self.disconnect(dead)

ws_manager = ConnectionManager()

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await ws_manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            # Echo or process if needed
            await websocket.send_text(json.dumps({"status": "PONG", "received": data}))
    except WebSocketDisconnect:
        ws_manager.disconnect(websocket)

# ----------------- AUTH ROUTER -----------------
@app.post("/auth/register", response_model=Token)
def register(req: UserRegister, db: Session = Depends(get_db)):
    existing = db.query(User).filter(User.email == req.email).first()
    if existing:
        raise HTTPException(status_code=400, detail="Email already registered")
    user = User(
        full_name=req.full_name,
        email=req.email,
        phone=req.phone,
        hashed_password=get_password_hash(req.password),
        role=UserRole(req.role.upper()) if req.role.upper() in UserRole.__members__ else UserRole.FARMER
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    token = create_access_token({"sub": user.email, "role": user.role.value, "id": user.id})
    return Token(access_token=token, token_type="bearer", role=user.role.value, user_id=user.id, full_name=user.full_name)

@app.post("/auth/login", response_model=Token)
def login(req: UserLogin, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == req.email).first()
    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid email or password")
    token = create_access_token({"sub": user.email, "role": user.role.value, "id": user.id})
    return Token(access_token=token, token_type="bearer", role=user.role.value, user_id=user.id, full_name=user.full_name)

@app.get("/auth/me", response_model=UserOut)
def get_current_user(token_data: dict = Depends(decode_token), db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == token_data.get("sub")).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return UserOut(id=user.id, full_name=user.full_name, email=user.email, phone=user.phone, role=user.role.value)

# ----------------- BATCHES ROUTER -----------------
@app.get("/batches")
def list_batches(db: Session = Depends(get_db)):
    return db.query(Batch).all()

@app.post("/batches")
def create_batch(b: BatchCreate, db: Session = Depends(get_db)):
    existing = db.query(Batch).filter(Batch.id == b.id).first()
    if existing:
        raise HTTPException(status_code=400, detail="Batch ID already exists")
    token = f"VT-TRACE-{b.id}-{int(datetime.utcnow().timestamp())}"
    batch = Batch(
        id=b.id,
        product_name=b.product_name,
        product_category=b.product_category,
        farmer_supplier=b.farmer_supplier,
        quantity=b.quantity,
        unit=b.unit,
        harvest_date=b.harvest_date,
        packing_date=b.packing_date,
        destination=b.destination,
        transporter=b.transporter,
        assigned_device_id=b.assigned_device_id,
        qr_token=token
    )
    db.add(batch)
    db.commit()
    db.refresh(batch)
    return batch

@app.get("/batches/{id}")
def get_batch(id: str, db: Session = Depends(get_db)):
    batch = db.query(Batch).filter(Batch.id == id).first()
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    return batch

@app.put("/batches/{id}")
def update_batch(id: str, b: BatchUpdate, db: Session = Depends(get_db)):
    batch = db.query(Batch).filter(Batch.id == id).first()
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    if b.product_name: batch.product_name = b.product_name
    if b.current_stage: batch.current_stage = b.current_stage
    if b.destination: batch.destination = b.destination
    if b.transporter: batch.transporter = b.transporter
    if b.assigned_device_id: batch.assigned_device_id = b.assigned_device_id
    db.commit()
    db.refresh(batch)
    return batch

@app.delete("/batches/{id}")
def delete_batch(id: str, db: Session = Depends(get_db)):
    batch = db.query(Batch).filter(Batch.id == id).first()
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    db.delete(batch)
    db.commit()
    return {"message": "Batch deleted"}

# ----------------- DEVICES ROUTER -----------------
@app.get("/devices")
def list_devices(db: Session = Depends(get_db)):
    return db.query(Device).all()

@app.post("/devices")
def register_device(device_id: str, db: Session = Depends(get_db)):
    dev = Device(id=device_id)
    db.add(dev)
    db.commit()
    db.refresh(dev)
    return dev

# ----------------- SENSORS & TELEMETRY ROUTER -----------------
@app.post("/sensor-readings")
async def ingest_sensor_reading(payload: SensorReadingPayload, db: Session = Depends(get_db)):
    # Validate idempotency
    existing = db.query(SensorReading).filter(SensorReading.event_id == payload.eventId).first()
    if existing:
        return {"status": "DUPLICATE_IGNORED", "eventId": payload.eventId}

    reading = SensorReading(
        batch_id=payload.batchId,
        device_id=payload.deviceId,
        event_id=payload.eventId,
        timestamp=payload.timestamp or datetime.utcnow(),
        temperature=payload.temperature,
        humidity=payload.humidity,
        ethylene=payload.ethylene,
        ammonia=payload.ammonia,
        battery=payload.battery,
        tamper=payload.tamper,
        signal=payload.signal,
        hash=payload.hash or "",
        previous_hash=payload.previousHash or ""
    )
    db.add(reading)

    # Check alert conditions
    if payload.temperature > 10.0 or payload.temperature < 2.0:
        alert = Alert(
            batch_id=payload.batchId,
            device_id=payload.deviceId,
            alert_type="TEMP_EXCURSION",
            severity="CRITICAL",
            message=f"Temperature excursion: {payload.temperature}°C (Limit: 2.0°C - 10.0°C)"
        )
        db.add(alert)

    if payload.tamper:
        tamper = TamperEvent(
            event_id=f"tamper-{payload.eventId}",
            batch_id=payload.batchId,
            device_id=payload.deviceId,
            hash=payload.hash or "",
            severity="HIGH"
        )
        db.add(tamper)

    db.commit()
    db.refresh(reading)

    # Broadcast over WebSocket
    await ws_manager.broadcast({
        "type": "TELEMETRY",
        "data": payload.model_dump()
    })
    return {"status": "SUCCESS", "id": reading.id}

@app.get("/batches/{id}/sensor-readings")
def get_sensor_readings(id: str, db: Session = Depends(get_db)):
    return db.query(SensorReading).filter(SensorReading.batch_id == id).order_by(SensorReading.timestamp.desc()).limit(100).all()

# ----------------- OFFLINE SYNC -----------------
@app.post("/sync")
def sync_offline_records(payload: SyncPayload, db: Session = Depends(get_db)):
    received = len(payload.records)
    synced = 0
    duplicates = 0
    failed = 0

    for item in payload.records:
        try:
            exists = db.query(SensorReading).filter(SensorReading.event_id == item.eventId).first()
            if exists:
                duplicates += 1
                continue
            r = SensorReading(
                batch_id=item.batchId,
                device_id=item.deviceId,
                event_id=item.eventId,
                timestamp=item.timestamp or datetime.utcnow(),
                temperature=item.temperature,
                humidity=item.humidity,
                ethylene=item.ethylene,
                ammonia=item.ammonia,
                battery=item.battery,
                tamper=item.tamper,
                signal=item.signal,
                hash=item.hash or "",
                previous_hash=item.previousHash or ""
            )
            db.add(r)
            synced += 1
        except Exception:
            failed += 1

    db.commit()
    return {
        "received": received,
        "synchronized": synced,
        "failed": failed,
        "duplicates": duplicates
    }

# ----------------- HASH VERIFICATION -----------------
@app.post("/verification/verify")
def verify_hash_record(req: HashVerifyRequest):
    expected_hash = HashEngine.calculate_sha256(req.canonical_data)
    matches = (expected_hash == req.hash_to_verify)
    return {
        "matches": matches,
        "expected_hash": expected_hash,
        "provided_hash": req.hash_to_verify,
        "record_id": req.record_id,
        "batch_id": req.batch_id,
        "status": "VERIFIED" if matches else "INTEGRITY_MISMATCH"
    }

# ----------------- BLOCKCHAIN LEDGER -----------------
@app.post("/blockchain/submit")
def submit_to_blockchain(req: BlockchainSubmitRequest, db: Session = Depends(get_db)):
    tx_id = ledger_service.submit_hash(req.batch_id, req.record_id, req.hash, req.event_type)
    rec = BlockchainRecord(
        batch_id=req.batch_id,
        record_id=req.record_id,
        hash=req.hash,
        transaction_id=tx_id,
        ledger_type="DEMO_LEDGER",
        status="COMMITTED"
    )
    db.add(rec)
    db.commit()
    return {"transaction_id": tx_id, "status": "COMMITTED", "ledger": "DEMO_LEDGER"}

# ----------------- PUBLIC TRACEABILITY VIEW -----------------
@app.get("/trace/{batch_id}")
def get_traceability(batch_id: str, db: Session = Depends(get_db)):
    batch = db.query(Batch).filter(Batch.id == batch_id).first()
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    readings = db.query(SensorReading).filter(SensorReading.batch_id == batch_id).order_by(SensorReading.timestamp.desc()).limit(50).all()
    tampers = db.query(TamperEvent).filter(TamperEvent.batch_id == batch_id).all()
    events = db.query(SupplyChainEvent).filter(SupplyChainEvent.batch_id == batch_id).order_by(SupplyChainEvent.timestamp.asc()).all()

    return {
        "batch": batch,
        "readings_count": len(readings),
        "latest_reading": readings[0] if readings else None,
        "tampers": tampers,
        "events": events,
        "integrity_status": batch.integrity_status,
        "ledger_type": "DEMO_LEDGER"
    }
