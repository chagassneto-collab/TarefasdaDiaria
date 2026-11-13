package com.example.tarefasdadiaria

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.concurrent.Volatile

@Database(
    entities = [Tarefa::class],
    version = 1,
    exportSchema = false
)
abstract class BancoTarefas : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var instancia: BancoTarefas? = null

        fun obterInstancia(contexto: Context): BancoTarefas {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BancoTarefas::class.java,
                    "tarefas.db"
                ).build().also { bancoCriado ->
                    instancia = bancoCriado
                }
            }
        }
    }
}