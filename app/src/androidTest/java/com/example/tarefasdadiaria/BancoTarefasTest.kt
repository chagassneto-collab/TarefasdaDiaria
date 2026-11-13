package com.example.tarefasdadiaria

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BancoTarefasTest {

    @Test
    fun salvarConcluirEReabrirPreservaDados() = runBlocking {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        val nomeBanco = "teste_tarefas_${UUID.randomUUID()}.db"
        var banco = abrirBanco(contexto, nomeBanco)

        try {
            val primeira = Tarefa(
                titulo = "Mesmo título",
                descricao = "Descrição A"
            )
            val segunda = Tarefa(titulo = "Mesmo título")

            val dao = banco.tarefaDao()
            val idPrimeira = dao.inserir(primeira).toInt()
            val idSegunda = dao.inserir(segunda).toInt()

            assertTrue(idPrimeira > 0)
            assertTrue(idSegunda > 0)
            assertTrue(idPrimeira != idSegunda)

            assertEquals(
                0,
                dao.atualizarConclusao(id = 999, concluida = true)
            )

            assertEquals(
                listOf(
                    primeira.copy(id = idPrimeira),
                    segunda.copy(id = idSegunda)
                ),
                dao.observarTodas().first()
            )

            assertEquals(
                1,
                dao.atualizarConclusao(idPrimeira, true)
            )

            banco.close()
            banco = abrirBanco(contexto, nomeBanco)

            assertEquals(
                listOf(
                    primeira.copy(id = idPrimeira, concluida = true),
                    segunda.copy(id = idSegunda)
                ),
                banco.tarefaDao().observarTodas().first()
            )

            assertEquals(
                1,
                banco.tarefaDao().atualizarConclusao(idPrimeira, false)
            )

            banco.close()
            banco = abrirBanco(contexto, nomeBanco)

            assertEquals(
                listOf(
                    primeira.copy(id = idPrimeira),
                    segunda.copy(id = idSegunda)
                ),
                banco.tarefaDao().observarTodas().first()
            )
        } finally {
            banco.close()
            contexto.deleteDatabase(nomeBanco)
        }
    }

    private fun abrirBanco(
        contexto: Context,
        nomeBanco: String
    ): BancoTarefas {
        return Room.databaseBuilder(
            contexto,
            BancoTarefas::class.java,
            nomeBanco
        ).build()
    }
}