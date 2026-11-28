package com.smartumbrella.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartumbrella.app.data.LoanEventEntity
import com.smartumbrella.app.data.UmbrellaSlotEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: UmbrellaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartUmbrellaTheme { UmbrellaScreen(viewModel) } }
    }
}

@Composable
fun UmbrellaScreen(viewModel: UmbrellaViewModel) {
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val pending by viewModel.pendingSync.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val flowMode by viewModel.flowMode.collectAsStateWithLifecycle()
    val selectedSlot by viewModel.selectedSlot.collectAsStateWithLifecycle()
    var choosingMode by remember { mutableStateOf<FlowMode?>(null) }

    val activeMode = flowMode ?: choosingMode
    val canBorrow = slots.any { it.hasUmbrella }
    val canReturn = slots.any { !it.hasUmbrella }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("智能雨伞柜", style = MaterialTheme.typography.headlineMedium)
            Text("请选择服务，按屏幕提示完成操作", style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("可借雨伞", slots.count { it.hasUmbrella }.toString(), Modifier.weight(1f))
                SummaryCard("可归还空位", slots.count { !it.hasUmbrella }.toString(), Modifier.weight(1f))
                SummaryCard("待同步", pending.size.toString(), Modifier.weight(1f))
            }

            if (activeMode == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { choosingMode = FlowMode.BORROW }, enabled = canBorrow && !busy, modifier = Modifier.weight(1f).height(64.dp)) { Text("我要借伞") }
                    Button(onClick = { choosingMode = FlowMode.RETURN }, enabled = canReturn && !busy, modifier = Modifier.weight(1f).height(64.dp)) { Text("我要还伞") }
                }
                Text("设备状态：本地离线可用 · 硬件模拟模式", style = MaterialTheme.typography.bodyMedium)
                RecentEvents(events)
            } else {
                val borrowing = activeMode == FlowMode.BORROW
                Text(if (borrowing) "借伞 · 选择一把可借雨伞" else "还伞 · 选择一个空闲伞位", style = MaterialTheme.typography.titleLarge)

                if (flowMode == null) {
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(slots, key = { it.slotNumber }) { slot ->
                            SlotCard(slot, !busy && if (borrowing) slot.hasUmbrella else !slot.hasUmbrella, if (borrowing) "选择借用" else "选择归还位") {
                                if (borrowing) viewModel.startBorrow(slot.slotNumber) else viewModel.startReturn(slot.slotNumber)
                            }
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("操作确认", style = MaterialTheme.typography.headlineSmall)
                            Text("伞位 ${selectedSlot ?: "—"}", style = MaterialTheme.typography.titleLarge)
                            Text(if (busy) if (borrowing) "正在验证指纹并开启柜锁…" else "正在读取 RFID 并登记归还…" else if (borrowing) "请确认后按压指纹。验证通过后伞柜将解锁，请及时取走雨伞。" else "请将雨伞放入所选伞位。确认后将读取 RFID 标签并登记归还。", style = MaterialTheme.typography.bodyLarge)
                            slots.firstOrNull { it.slotNumber == selectedSlot }?.let { Text("当前状态：${if (it.hasUmbrella) "有伞" else "空位"} · ${if (it.locked) "已锁定" else "已解锁"}") }
                            if (busy) CircularProgressIndicator()
                            Spacer(Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { viewModel.cancelFlow(); choosingMode = null }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("取消") }
                                Button(onClick = { choosingMode = null; if (borrowing) viewModel.confirmBorrow() else viewModel.confirmReturn() }, enabled = !busy && selectedSlot != null, modifier = Modifier.weight(1f)) { Text(if (borrowing) "确认借伞" else "确认归还") }
                            }
                        }
                    }
                }

                Text(statusMessage, style = MaterialTheme.typography.bodyLarge)
                if (flowMode == null) OutlinedButton(onClick = { choosingMode = null }, enabled = !busy) { Text("返回首页") }
            }

            Text("指纹、门锁、RFID 与同步服务当前均为模拟实现", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) { Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.bodyMedium); Text(value, style = MaterialTheme.typography.headlineSmall) } }
}

@Composable
private fun SlotCard(slot: UmbrellaSlotEntity, enabled: Boolean, actionLabel: String, onClick: () -> Unit) {
    Card { Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("伞位 ${slot.slotNumber}", style = MaterialTheme.typography.titleMedium)
        Text(if (slot.hasUmbrella) "有伞 · 可借" else "空位 · 可归还")
        Text(if (slot.locked) "柜锁：已锁定" else "柜锁：已解锁")
        Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(if (enabled) actionLabel else "不可操作") }
    } }
}

@Composable
private fun RecentEvents(events: List<LoanEventEntity>) {
    Card(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("最近记录", style = MaterialTheme.typography.titleMedium)
            if (events.isEmpty()) Text("暂无借还记录") else events.take(3).forEach { event ->
                val action = if (event.action == "BORROW") "借出" else "归还"
                val time = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(event.occurredAt))
                Text("$time · $action · 伞位 ${event.slotNumber}")
            }
        }
    }
}
