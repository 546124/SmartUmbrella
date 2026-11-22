package com.smartumbrella.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartumbrella.app.data.UmbrellaRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as SmartUmbrellaApplication).repository
        setContent { SmartUmbrellaTheme { UmbrellaScreen(repository) } }
    }
}

@Composable
fun UmbrellaScreen(repository: UmbrellaRepository) {
    val slots by repository.slots.collectAsState(initial = emptyList())
    val pending by repository.pendingSync.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var statusMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("就绪") }

    LaunchedEffect(repository) { repository.initialize() }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("智能雨伞柜", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("可借：${slots.count { it.hasUmbrella }}  / 空位：${slots.count { !it.hasUmbrella }}")
            Text("待同步记录：${pending.size} · 离线队列已启用")
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
                                    enabled = slot.hasUmbrella,
                                    onClick = { scope.launch { statusMessage = repository.borrow(slot.slotNumber).message } }
                                ) { Text("借出") }
                                Button(
                                    enabled = !slot.hasUmbrella,
                                    onClick = { scope.launch { statusMessage = repository.returnUmbrella(slot.slotNumber).message } }
                                ) { Text("归还") }
                            }
                        }
                    }
                }
            }
            Text(statusMessage, modifier = Modifier.padding(vertical = 8.dp))
            Text("指纹、门锁与 RFID 当前使用本地模拟器")
        }
    }
}
