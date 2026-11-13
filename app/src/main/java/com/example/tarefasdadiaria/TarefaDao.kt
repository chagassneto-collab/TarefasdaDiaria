package com.example.tarefasdadiaria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas ORDER BY id ASC")
    fun observarTodas(): Flow<List<Tarefa>>

    @Insert
    suspend fun inserir(tarefa: Tarefa): Long

    @Query("UPDATE tarefas SET concluida = :concluida WHERE id = :id")
    suspend fun atualizarConclusao(id: Int, concluida: Boolean): Int
}