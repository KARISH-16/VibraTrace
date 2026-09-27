package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.remote.ConnectionStatus
import com.example.data.repository.VibraTraceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DiagnosticsStatus(
    val esp32: String = "PASS",
    val aht20: String = "PASS",
    val ethylene: String = "PASS",
    val nh3: String = "PASS",
    val reedSwitch: String = "PASS",
    val microSD: String = "PASS",
    val sim800c: String = "PASS",
    val gsmSignal: String = "PASS",
    val mqtt: String = "PASS",
    val backend: String = "PASS",
    val postgres: String = "PASS",
    val webSocket: String = "PASS",
    val hashEngine: String = "PASS",
    val blockchainLedger: String = "PASS"
)

class VibraTraceViewModel(
    val repository: VibraTraceRepository
) : ViewModel() {

    private val _selectedBatchId = MutableStateFlow("FD2026-001")
    val selectedBatchId: StateFlow<String> = _selectedBatchId

    val allBatches: StateFlow<List<BatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlerts: StateFlow<List<AlertEntity>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allTamperEvents: StateFlow<List<TamperEventEntity>> = repository.allTamperEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHashRecords: StateFlow<List<HashRecordEntity>> = repository.allHashRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockchainRecords: StateFlow<List<BlockchainRecordEntity>> = repository.allBlockchainRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEnergyEvents: StateFlow<List<EnergyEventEntity>> = repository.recentEnergyEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettingsEntity?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeUser: StateFlow<UserEntity?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val connectionState: StateFlow<ConnectionStatus> = repository.connectionState

    // Active batch details
    val activeBatch: StateFlow<BatchEntity?> = _selectedBatchId.flatMapLatest { id ->
        repository.getBatch(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestReading: StateFlow<SensorReadingEntity?> = _selectedBatchId.flatMapLatest { id ->
        repository.getLatestReading(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readingsForActiveBatch: StateFlow<List<SensorReadingEntity>> = _selectedBatchId.flatMapLatest { id ->
        repository.getReadingsForBatch(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timelineEvents: StateFlow<List<SupplyChainEventEntity>> = _selectedBatchId.flatMapLatest { id ->
        repository.getTimelineEvents(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDevice: StateFlow<DeviceEntity?> = repository.getDevice("VT-ESP32-001")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    private val _isLiveSimulationRunning = MutableStateFlow(true)
    val isLiveSimulationRunning: StateFlow<Boolean> = _isLiveSimulationRunning

    init {
        // Start continuous background telemetry updates by default for realistic IoT experience
        repository.simulationEngine.startContinuousTelemetry(4000)
    }

    fun selectBatch(batchId: String) {
        _selectedBatchId.value = batchId
    }

    fun createBatch(
        id: String,
        name: String,
        category: String,
        farmer: String,
        qty: Double,
        unit: String,
        destination: String,
        transporter: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val batch = BatchEntity(
                id = id,
                productName = name,
                productCategory = category,
                farmerSupplier = farmer,
                quantity = qty,
                unit = unit,
                harvestDate = "2026-09-26",
                packingDate = "2026-09-26",
                destination = destination,
                transporter = transporter,
                assignedDeviceId = "VT-ESP32-001",
                currentStage = "FARM",
                qrToken = "VT-TRACE-$id-$now",
                conditionStatus = "GOOD",
                integrityStatus = "VERIFIED"
            )
            repository.createBatch(batch)
            selectBatch(id)
        }
    }

    fun advanceShipmentStage(nextStage: String) {
        viewModelScope.launch {
            repository.simulationEngine.advanceShipmentStage(_selectedBatchId.value, nextStage)
        }
    }

    fun resolveAlert(alertId: Long) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
        }
    }

    fun acknowledgeTamper(eventId: String) {
        viewModelScope.launch {
            repository.acknowledgeTamper(eventId)
        }
    }

    // Demo Mode Actions
    fun triggerSimulatedReading() {
        viewModelScope.launch {
            repository.simulationEngine.generateSensorReading(batchId = _selectedBatchId.value)
        }
    }

    fun setNetworkState(online: Boolean) {
        viewModelScope.launch {
            repository.simulationEngine.setNetworkState(online)
        }
    }

    fun seed127OfflineRecords() {
        viewModelScope.launch {
            repository.simulationEngine.seed127OfflineBufferRecords()
            _syncMessage.value = "Offline records waiting: 127"
        }
    }

    fun synchronizeOfflineQueue() {
        viewModelScope.launch {
            val (synced, total) = repository.simulationEngine.synchronizeOfflineQueue()
            _syncMessage.value = if (total > 0) "$synced/$total records synchronized." else "All records up to date."
        }
    }

    fun triggerTamperEvent() {
        viewModelScope.launch {
            repository.simulationEngine.triggerTamperEvent(_selectedBatchId.value)
        }
    }

    fun triggerTemperatureExcursion() {
        viewModelScope.launch {
            repository.simulationEngine.triggerTemperatureExcursionAlert(_selectedBatchId.value)
        }
    }

    fun triggerEthyleneAlert() {
        viewModelScope.launch {
            repository.simulationEngine.triggerEthyleneAlert(_selectedBatchId.value)
        }
    }

    fun triggerAmmoniaAlert() {
        viewModelScope.launch {
            repository.simulationEngine.triggerAmmoniaAlert(_selectedBatchId.value)
        }
    }

    fun triggerLowBattery() {
        viewModelScope.launch {
            repository.simulationEngine.triggerLowBattery(_selectedBatchId.value)
        }
    }

    fun simulateIntegrityMismatch(recordId: String) {
        viewModelScope.launch {
            repository.simulationEngine.simulateIntegrityMismatch(recordId)
        }
    }

    fun toggleContinuousTelemetry() {
        if (_isLiveSimulationRunning.value) {
            repository.simulationEngine.stopContinuousTelemetry()
            _isLiveSimulationRunning.value = false
        } else {
            repository.simulationEngine.startContinuousTelemetry(4000)
            _isLiveSimulationRunning.value = true
        }
    }

    fun saveSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    fun login(email: String, name: String, role: String) {
        viewModelScope.launch {
            repository.loginUser(email, name, role, "token-$role-${System.currentTimeMillis()}")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}

class VibraTraceViewModelFactory(
    private val repository: VibraTraceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VibraTraceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VibraTraceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
