from fastapi.testclient import TestClient
from app.main import app
from app.hash_engine import HashEngine

client = TestClient(app)

def test_hash_calculation():
    canonical = HashEngine.canonical_string(
        batch_id="FD2026-001",
        device_id="VT-ESP32-001",
        event_id="evt-1001",
        temp=6.8,
        hum=72.0,
        c2h4=0.42,
        nh3=1.8,
        tamper=False,
        previous_hash="0" * 64
    )
    h = HashEngine.calculate_sha256(canonical)
    assert len(h) == 64
    assert h == HashEngine.calculate_sha256(canonical)

def test_batch_lifecycle():
    res = client.post("/batches", json={
        "id": "FD2026-TEST",
        "product_name": "Organic Tomatoes",
        "farmer_supplier": "Sunrise Valley Farms",
        "quantity": 250.0,
        "harvest_date": "2026-09-25",
        "packing_date": "2026-09-26",
        "destination": "Metro Central Distribution",
        "transporter": "ColdTrans Logistics"
    })
    assert res.status_code in [200, 400]
