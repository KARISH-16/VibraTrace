# VibraTrace API Documentation (OpenAPI v3)

### Base URL: `https://ais-dev-czoimda43vp3qoqief52cg-154597152771.asia-southeast1.run.app` or `http://localhost:8000`

### Endpoints
- `POST /auth/register` - Create user with full name, email, phone, password, role
- `POST /auth/login` - Authenticate user, receive JWT Bearer token
- `GET /auth/me` - Current authenticated profile
- `GET /batches` - List all food shipment batches
- `POST /batches` - Register a new batch (e.g. FD2026-001)
- `GET /batches/{id}` - Retrieve details of single batch
- `PUT /batches/{id}` - Update batch stage, transporter, destination
- `DELETE /batches/{id}` - Remove batch
- `GET /devices` - List registered IoT nodes
- `POST /devices` - Register new node
- `POST /sensor-readings` - Ingest real-time telemetry from MQTT/HTTP
- `GET /batches/{id}/sensor-readings` - Historical telemetry stream
- `POST /sync` - Ingest buffered offline queue from MicroSD/Android
- `POST /verification/verify` - Cryptographic verification of SHA-256 hash
- `POST /blockchain/submit` - Anchor critical event hash into immutable ledger
- `GET /trace/{batchId}` - Public Farm-to-Fork consumer traceability view
- `WS /ws` - Full-duplex WebSocket stream for instant live dashboard telemetry
