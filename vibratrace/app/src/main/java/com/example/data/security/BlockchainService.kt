package com.example.data.security

import java.security.MessageDigest

data class LedgerTransaction(
    val transactionId: String,
    val batchId: String,
    val recordId: String,
    val hash: String,
    val blockNumber: Long,
    val timestamp: Long,
    val ledgerType: String = "DEMO_LEDGER",
    val status: String = "COMMITTED"
)

interface BlockchainService {
    suspend fun submitHash(batchId: String, recordId: String, hash: String, eventType: String): LedgerTransaction
    suspend fun verifyHash(hash: String, transactionId: String): Boolean
    suspend fun getTransaction(transactionId: String): LedgerTransaction?
    fun getLedgerType(): String
}

class DemoLedgerService : BlockchainService {
    private val localLedger = mutableMapOf<String, LedgerTransaction>()
    private var currentBlock: Long = 10428

    override suspend fun submitHash(
        batchId: String,
        recordId: String,
        hash: String,
        eventType: String
    ): LedgerTransaction {
        val now = System.currentTimeMillis()
        val rawTx = "$batchId:$recordId:$hash:$eventType:$now"
        val digest = MessageDigest.getInstance("SHA-256")
        val txHash = digest.digest(rawTx.toByteArray()).joinToString("") { "%02x".format(it) }.take(40)
        val txId = "0x$txHash"

        currentBlock++
        val tx = LedgerTransaction(
            transactionId = txId,
            batchId = batchId,
            recordId = recordId,
            hash = hash,
            blockNumber = currentBlock,
            timestamp = now,
            ledgerType = "DEMO_LEDGER",
            status = "COMMITTED"
        )
        localLedger[txId] = tx
        return tx
    }

    override suspend fun verifyHash(hash: String, transactionId: String): Boolean {
        val tx = localLedger[transactionId] ?: return false
        return tx.hash.equals(hash, ignoreCase = true)
    }

    override suspend fun getTransaction(transactionId: String): LedgerTransaction? {
        return localLedger[transactionId]
    }

    override fun getLedgerType(): String = "DEMO_LEDGER"
}
