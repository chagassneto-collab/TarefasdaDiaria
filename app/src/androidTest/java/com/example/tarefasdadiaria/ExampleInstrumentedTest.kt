package com.example.tarefasdadiaria

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasAnyAncestor
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tarefasdadiaria.ui.theme.TarefasdaDiariaTheme
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import androidx.compose.ui.test.performScrollTo

@RunWith(AndroidJUnit4::class)

    class ExampleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var bancoTeste: BancoTarefas
    private lateinit var viewModelTeste: TarefasViewModel

    @Before
    fun prepararTela() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()

        bancoTeste = Room.inMemoryDatabaseBuilder(
            contexto,
            BancoTarefas::class.java
        ).build()

        runBlocking {
            val dao = bancoTeste.tarefaDao()

            dao.inserir(Tarefa(titulo = "Estudar Kotlin"))
            dao.inserir(Tarefa(titulo = "Revisar Compose"))
            dao.inserir(Tarefa(titulo = "Praticar Listas"))
        }

        viewModelTeste = TarefasViewModel(bancoTeste.tarefaDao())

        composeRule.setContent {
            TarefasdaDiariaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Tarefas da Diária",
                        modifier = Modifier.padding(innerPadding),
                        tarefasViewModel = viewModelTeste
                    )
                }
            }
        }

        aguardarTexto("Estudar Kotlin")
    }

    @After
    fun encerrarTeste() {
        try {
            if (::viewModelTeste.isInitialized) {
                runBlocking {
                    viewModelTeste.viewModelScope.coroutineContext.job
                        .cancelAndJoin()
                }
            }
        } finally {
            if (::bancoTeste.isInitialized) {
                bancoTeste.close()
            }
        }
    }

    private fun aguardarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(texto)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun salvarTarefaAtualizaListaELimpaCampos() {
        composeRule.onNodeWithText("Nova tarefa")
            .performClick()

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsNotEnabled()

        composeRule.onNodeWithText("Título da tarefa")
            .performTextReplacement("   ")

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsNotEnabled()

        composeRule.onNodeWithText("Título da tarefa")
            .performTextReplacement("   Testar cadastro automático   ")

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsEnabled()

        composeRule.onNodeWithText("Descrição (opcional)")
            .performTextReplacement("Ler sobre testes")

        composeRule.onNodeWithText("Salvar tarefa")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        aguardarTexto("Testar cadastro automático")

        val tarefaCadastrada = composeRule.onNode(
            hasText("Testar cadastro automático") and
                    hasAnyAncestor(hasTestTag("lista_pendentes"))
        )

        tarefaCadastrada.assertIsDisplayed()
        tarefaCadastrada.assertIsOff()

        composeRule.onNodeWithText("Concluídas: 0")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Título da tarefa")
            .assertDoesNotExist()

        composeRule.onNodeWithText("Nova tarefa")
            .performClick()

        composeRule.onNodeWithText("Título da tarefa")
            .assertTextContains("")

        composeRule.onNodeWithText("Descrição (opcional)")
            .assertTextContains("")

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsNotEnabled()

        composeRule.onNodeWithText("Voltar")
            .performClick()

        tarefaCadastrada.assertIsDisplayed()
    }

    @Test
    fun concluirEReabrirTarefaPelaTela() {
        val estudarPendente = composeRule.onNode(
            hasText("Estudar Kotlin") and
                    hasAnyAncestor(hasTestTag("lista_pendentes"))
        )
        val estudarConcluida = composeRule.onNode(
            hasText("Estudar Kotlin") and
                    hasAnyAncestor(hasTestTag("lista_concluidas"))
        )
        val revisar = composeRule.onNodeWithText("Revisar Compose")
        val praticar = composeRule.onNodeWithText("Praticar Listas")

        estudarPendente.assertIsOff()
        revisar.assertIsOff()
        praticar.assertIsOff()
        composeRule.onNodeWithText("Concluídas: 0").assertIsDisplayed()

        composeRule.onNodeWithText("Ver concluídas").performClick()

        estudarConcluida.assertDoesNotExist()
        composeRule.onNodeWithText("Concluídas: 0").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa concluída").assertIsDisplayed()

        composeRule.onNodeWithText("Voltar").performClick()

        estudarPendente.performClick()
        aguardarTexto("Concluídas: 1")

        estudarPendente.assertDoesNotExist()
        revisar.assertIsOff()
        praticar.assertIsOff()
        composeRule.onNodeWithText("Concluídas: 1").assertIsDisplayed()

        composeRule.onNodeWithText("Ver concluídas").performClick()

        estudarConcluida.assertIsOn()
        composeRule.onNodeWithText("Concluídas: 1").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa concluída").assertDoesNotExist()

        composeRule.onNodeWithText("Voltar").performClick()

        revisar.performClick()
        aguardarTexto("Concluídas: 2")

        revisar.assertDoesNotExist()
        praticar.assertIsOff()
        composeRule.onNodeWithText("Concluídas: 2").assertIsDisplayed()

        praticar.performClick()
        aguardarTexto("Concluídas: 3")

        praticar.assertDoesNotExist()
        composeRule.onNodeWithText("Concluídas: 3").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa pendente").assertIsDisplayed()

        composeRule.onNodeWithText("Ver concluídas").performClick()

        estudarConcluida.assertIsOn()
        revisar.assertIsOn()
        praticar.assertIsOn()
        composeRule.onNodeWithText("Concluídas: 3").assertIsDisplayed()

        estudarConcluida.performClick()
        aguardarTexto("Concluídas: 2")

        estudarConcluida.assertDoesNotExist()
        composeRule.onNodeWithText("Concluídas: 2").assertIsDisplayed()

        composeRule.onNodeWithText("Voltar").performClick()

        estudarPendente.assertIsOff()
        composeRule.onNodeWithText("Concluídas: 2").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa pendente").assertDoesNotExist()

        composeRule.onNodeWithText("Ver concluídas").performClick()

        estudarConcluida.assertDoesNotExist()
        revisar.assertIsOn()
        praticar.assertIsOn()

        revisar.performClick()
        aguardarTexto("Concluídas: 1")

        revisar.assertDoesNotExist()
        praticar.assertIsOn()
        composeRule.onNodeWithText("Concluídas: 1").assertIsDisplayed()

        praticar.performClick()
        aguardarTexto("Concluídas: 0")

        praticar.assertDoesNotExist()
        composeRule.onNodeWithText("Concluídas: 0").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa concluída").assertIsDisplayed()

        composeRule.onNodeWithText("Voltar").performClick()

        estudarPendente.assertIsOff()
        revisar.assertIsOff()
        praticar.assertIsOff()
        composeRule.onNodeWithText("Concluídas: 0").assertIsDisplayed()
    }

    @Test
    fun listasLongasPermitemAcessarUltimasTarefas() {
        runBlocking {
            val dao = bancoTeste.tarefaDao()

            for (numero in 1..20) {
                dao.inserir(
                    Tarefa(titulo = "Pendente extra $numero")
                )

                dao.inserir(
                    Tarefa(
                        titulo = "Concluída extra $numero",
                        concluida = true
                    )
                )
            }
        }

        aguardarTexto("Concluídas: 20")

        composeRule.onNode(
            hasText("Pendente extra 20") and
                    hasAnyAncestor(hasTestTag("lista_pendentes"))
        )
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsOff()

        composeRule.onNodeWithText("Ver concluídas")
            .performClick()

        composeRule.onNode(
            hasText("Concluída extra 20") and
                    hasAnyAncestor(hasTestTag("lista_concluidas"))
        )
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsOn()
    }

    @Test
    fun voltarProtegeRascunhoAteConfirmarDescarte() {
        val tituloRascunho = "Rascunho automático"
        val descricaoRascunho = "Descrição que deve ser preservada."

        composeRule.onNodeWithText("Nova tarefa")
            .performClick()

        composeRule.onNodeWithText("Título da tarefa")
            .performTextReplacement(tituloRascunho)

        composeRule.onNodeWithText("Descrição (opcional)")
            .performTextReplacement(descricaoRascunho)

        composeRule.onNodeWithText("Voltar")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText("Descartar rascunho?")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Continuar editando")
            .performClick()

        composeRule.onNodeWithText("Descartar rascunho?")
            .assertDoesNotExist()

        composeRule.onNodeWithText("Título da tarefa")
            .assertTextContains(tituloRascunho)

        composeRule.onNodeWithText("Descrição (opcional)")
            .assertTextContains(descricaoRascunho)

        // Aciona o tratamento do Voltar do Android.
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("Descartar rascunho?")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Descartar")
            .performClick()

        aguardarTexto("Estudar Kotlin")

        composeRule.onNodeWithText(tituloRascunho)
            .assertDoesNotExist()

        composeRule.onNodeWithText("Concluídas: 0")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Nova tarefa")
            .performClick()

        composeRule.onNodeWithText("Título da tarefa")
            .assertTextContains("")

        composeRule.onNodeWithText("Descrição (opcional)")
            .assertTextContains("")

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsNotEnabled()

        // Somente a descrição preenchida também exige confirmação.
        composeRule.onNodeWithText("Descrição (opcional)")
            .performTextReplacement("Descrição sem título")

        composeRule.onNodeWithText("Salvar tarefa")
            .assertIsNotEnabled()

        composeRule.onNodeWithText("Voltar")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText("Descartar rascunho?")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Descartar")
            .performClick()

        aguardarTexto("Estudar Kotlin")

        composeRule.onNodeWithText("Descrição sem título")
            .assertDoesNotExist()
    }

}