package com.smartumbrella.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartumbrella.app.data.LoanEventEntity
import com.smartumbrella.app.data.OperationResult
import com.smartumbrella.app.data.SyncQueueEntity
import com.smartumbrella.app.data.UmbrellaSlotEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FlowMode { BORROW, RETURN }

class UmbrellaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartUmbrellaApplication).repository

    val slots: StateFlow<List<UmbrellaSlotEntity>> = repository.slots.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val events: StateFlow<List<LoanEventEntity>> = repository.events.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val pendingSync: StateFlow<List<SyncQueueEntity>> = repository.pendingSync.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    private val _statusMessage = MutableStateFlow("就绪")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _flowMode = MutableStateFlow<FlowMode?>(null)
    val flowMode: StateFlow<FlowMode?> = _flowMode.asStateFlow()
    private val _selectedSlot = MutableStateFlow<Int?>(null)
    val selectedSlot: StateFlow<Int?> = _selectedSlot.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.initialize() }
                .onFailure { _statusMessage.value = "初始化失败：${it.localizedMessage ?: "请稍后重试"}" }
        }
    }

    fun startBorrow(slotNumber: Int) {
        if (_busy.value) return
        _flowMode.value = FlowMode.BORROW
        _selectedSlot.value = slotNumber
        _statusMessage.value = "请按压指纹，确认借伞"
    }

    fun startReturn(slotNumber: Int) {
        if (_busy.value) return
        _flowMode.value = FlowMode.RETURN
        _selectedSlot.value = slotNumber
        _statusMessage.value = "请将雨伞放入伞位并靠近 RFID 读写区"
    }

    fun confirmBorrow() {
        val slotNumber = _selectedSlot.value ?: return
        perform("借伞成功，请取走雨伞") { repository.borrow(slotNumber) }
    }

    fun confirmReturn() {
        val slotNumber = _selectedSlot.value ?: return
        perform("归还成功，请关闭柜门") { repository.returnUmbrella(slotNumber) }
    }

    fun cancelFlow() {
        if (_busy.value) return
        _flowMode.value = null
        _selectedSlot.value = null
        _statusMessage.value = "操作已取消"
    }

    fun borrow(slotNumber: Int) = perform("借伞成功") { repository.borrow(slotNumber) }

    fun returnUmbrella(slotNumber: Int) = perform("归还成功") { repository.returnUmbrella(slotNumber) }

    private fun perform(successText: String, operation: suspend () -> OperationResult) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                val result = operation()
                _statusMessage.value = result.message
                if (result.success) {
                    _statusMessage.value = "$successText · ${result.message}"
                    _flowMode.value = null
                    _selectedSlot.value = null
                }
            } catch (error: Exception) {
                _statusMessage.value = "操作失败：${error.localizedMessage ?: "请稍后重试"}"
            } finally {
                _busy.value = false
            }
        }
    }
}
