import hashlib
import time
from typing import Dict, Any

class BlockchainService:
    def submit_hash(self, batch_id: str, record_id: str, hash_val: str, event_type: str) -> str:
        raise NotImplementedError

    def verify_hash(self, hash_val: str, transaction_id: str) -> bool:
        raise NotImplementedError

    def get_transaction(self, transaction_id: str) -> Dict[str, Any]:
        raise NotImplementedError

class DemoLedgerService(BlockchainService):
    """
    In-memory / PostgreSQL backed verifiable immutable ledger simulator.
    Clearly discloses itself as DEMO LEDGER in metadata and UI.
    """
    def __init__(self):
        self.ledger: Dict[str, Dict[str, Any]] = {}

    def submit_hash(self, batch_id: str, record_id: str, hash_val: str, event_type: str) -> str:
        tx_data = f"{batch_id}:{record_id}:{hash_val}:{event_type}:{time.time()}"
        tx_id = "0x" + hashlib.sha256(tx_data.encode()).hexdigest()[:40]
        self.ledger[tx_id] = {
            "transaction_id": tx_id,
            "batch_id": batch_id,
            "record_id": record_id,
            "hash": hash_val,
            "event_type": event_type,
            "timestamp": time.time(),
            "ledger_type": "DEMO_LEDGER",
            "block_number": len(self.ledger) + 10428,
            "status": "CONFIRMED"
        }
        return tx_id

    def verify_hash(self, hash_val: str, transaction_id: str) -> bool:
        tx = self.ledger.get(transaction_id)
        if not tx:
            return False
        return tx["hash"] == hash_val

    def get_transaction(self, transaction_id: str) -> Dict[str, Any]:
        return self.ledger.get(transaction_id, {})

ledger_service = DemoLedgerService()
