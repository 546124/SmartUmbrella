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

    init {
        viewModelScope.launch {
            runCatching { repository.initialize() }
                .onFailure { _statusMessage.value = "初始化失败：${it.localizedMessage ?: "请稍后重试"}" }
        }
    }

    fun borrow(slotNumber: Int) = perform { repository.borrow(slotNumber) }

    fun returnUmbrella(slotNumber: Int) = perform { repository.returnUmbrella(slotNumber) }

    private fun perform(operation: suspend () -> OperationResult) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                _statusMessage.value = operation().message
            } catch (error: Exception) {
                _statusMessage.value = "操作失败：${error.localizedMessage ?: "请稍后重试"}"
            } finally {
                _busy.value = false
            }
        }
    }
}
