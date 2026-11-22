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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    val pending by viewModel.pendingSync.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("智能雨伞柜", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("可借：${slots.count { it.hasUmbrella }}  / 空位：${slots.count { !it.hasUmbrella }}")
            Text("待同步记录：${pending.size} · 后台同步已启用（演示模式）")
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
                items(slots, key = { it.slotNumber }) { slot ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("伞位 ${slot.slotNumber}", style = MaterialTheme.typography.titleMedium)
                            Text(if (slot.hasUmbrella) "有伞 · 可借" else "空位 · 可归还")
                            Text(if (slot.locked) "已锁定" else "已解锁")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = slot.hasUmbrella && !busy,
                                    onClick = { viewModel.borrow(slot.slotNumber) }
                                ) { Text("借出") }
                                Button(
                                    enabled = !slot.hasUmbrella && !busy,
                                    onClick = { viewModel.returnUmbrella(slot.slotNumber) }
                                ) { Text("归还") }
                            }
                        }
                    }
                }
            }
            Text(statusMessage, modifier = Modifier.padding(vertical = 8.dp))
            Text("指纹、门锁、RFID 与同步服务当前均为模拟实现")
        }
    }
}
