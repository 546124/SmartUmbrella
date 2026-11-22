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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartUmbrellaTheme { UmbrellaScreen() } }
    }
}

@Composable
fun UmbrellaScreen() {
    val slots = remember {
        mutableStateListOf(
            SlotUi(1, true, false), SlotUi(2, true, false),
            SlotUi(3, false, false), SlotUi(4, true, false),
            SlotUi(5, false, false), SlotUi(6, true, false)
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("智能雨伞柜", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("已占用：${slots.count { it.occupied }}  / 可借：${slots.count { !it.occupied }}")
            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f)) {
                items(slots) { slot ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("伞位 ${slot.number}", style = MaterialTheme.typography.titleMedium)
                            Text(if (slot.occupied) "有伞 · 红灯" else "空位 · 绿灯")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    val index = slots.indexOf(slot)
                                    slots[index] = slot.copy(occupied = true)
                                }) { Text("借出") }
                                Button(onClick = {
                                    val index = slots.indexOf(slot)
                                    slots[index] = slot.copy(occupied = false)
                                }) { Text("归还") }
                            }
                        }
                    }
                }
            }

            Button(onClick = { /* Later: vendor fingerprint SDK */ }, modifier = Modifier.fillMaxWidth()) {
                Text("模拟指纹登录")
            }
        }
    }
}

data class SlotUi(val number: Int, val occupied: Boolean, val locked: Boolean)
