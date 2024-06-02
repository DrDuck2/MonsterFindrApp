package com.example.monsterfindrapp

import com.example.monsterfindrapp.model.MonsterItem
import com.google.android.gms.common.api.Scope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object MonsterRepository {

    private val _monsterItems = MutableStateFlow<List<MonsterItem>>(emptyList())
    val monsterItems: StateFlow<List<MonsterItem>> = _monsterItems.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchMonsterItems().collect{ items ->
                _monsterItems.value = items
            }
        }
    }

    private fun fetchMonsterItems(): Flow<List<MonsterItem>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("MonsterItems").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                val data = document.data
                MonsterItem(
                    document.id,
                    data?.get("name") as? String ?: "",
                    data?.get("desc") as? String ?: "",
                    data?.get("image") as? String ?: ""
                )
            }
        }
    }

}