from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, EmailStr

class UserRegister(BaseModel):
    full_name: str
    email: EmailStr
    phone: str
    password: str
    role: str

class UserLogin(BaseModel):
    email: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str
    role: str
    user_id: int
    full_name: str

class UserOut(BaseModel):
    id: int
    full_name: str
    email: str
    phone: str
    role: str

class SensorReadingPayload(BaseModel):
    deviceId: str
    batchId: str
    eventId: str
    timestamp: Optional[datetime] = None
    temperature: float
    humidity: float
    ethylene: Optional[float] = None
    ammonia: float
    battery: int
    tamper: bool = False
    signal: int = -72
    hash: Optional[str] = None
    previousHash: Optional[str] = None

class BatchCreate(BaseModel):
    id: str
    product_name: str
    product_category: str = "Fresh Produce"
    farmer_supplier: str
    quantity: float
    unit: str = "kg"
    harvest_date: str
    packing_date: str
    destination: str
    transporter: str
    assigned_device_id: Optional[str] = None

class BatchUpdate(BaseModel):
    product_name: Optional[str] = None
    current_stage: Optional[str] = None
    destination: Optional[str] = None
    transporter: Optional[str] = None
    assigned_device_id: Optional[str] = None

class SyncPayload(BaseModel):
    records: List[SensorReadingPayload]

class HashVerifyRequest(BaseModel):
    record_id: str
    batch_id: str
    canonical_data: str
    hash_to_verify: str

class BlockchainSubmitRequest(BaseModel):
    batch_id: str
    record_id: str
    hash: str
    event_type: str
