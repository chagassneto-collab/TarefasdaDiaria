package com.example.tarefasdadiaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tarefasdadiaria.ui.theme.TarefasdaDiariaTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

enum class Tela {
    Inicio,
    Cadastro,
    Concluidas
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarefasdaDiariaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Tarefas da Diária",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    tarefasViewModel: TarefasViewModel =
        viewModel(factory = TarefasViewModel.Factory)
) {
    val tarefas by tarefasViewModel.tarefas.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    LaunchedEffect(tarefasViewModel, tarefasViewModel.cadastroSalvo) {
        if (tarefasViewModel.cadastroSalvo) {
            if (
                navController.currentBackStackEntry?.destination?.route ==
                Tela.Cadastro.name
            ) {
                navController.popBackStack()
            }

            tarefasViewModel.consumirCadastroSalvo()
        }
    }

    if (tarefasViewModel.carregando) {
        Text(
            text = "Carregando tarefas...",
            modifier = modifier
        )
        return
    }

    val erroLeitura = tarefasViewModel.erroLeitura
    if (erroLeitura != null) {
        Text(
            text = erroLeitura,
            modifier = modifier
        )
        return
    }

    val quantidadeConcluidas = tarefas.count { tarefa -> tarefa.concluida }
    val tarefasPendentes = tarefas.filter { tarefa -> !tarefa.concluida }
    val tarefasConcluidas = tarefas.filter { tarefa -> tarefa.concluida }
    val mensagemErro = tarefasViewModel.mensagemErro

    Column(modifier = modifier.fillMaxSize()) {
        if (mensagemErro != null) {
            Text(text = mensagemErro)
        }

        NavHost(
            navController = navController,
            startDestination = Tela.Inicio.name,
            modifier = Modifier.weight(1f)
        ) {
            composable(route = Tela.Inicio.name) {
                TelaInicial(
                    nome = name,
                    quantidadeConcluidas = quantidadeConcluidas,
                    tarefas = tarefasPendentes,
                    aoAbrirCadastro = {
                        navController.navigate(Tela.Cadastro.name)
                    },
                    aoAbrirConcluidas = {
                        navController.navigate(Tela.Concluidas.name)
                    },
                    aoMudarConclusao = { id, marcada ->
                        tarefasViewModel.atualizarConclusao(id, marcada)
                    }
                )
            }

            composable(route = Tela.Cadastro.name) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nova tarefa",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    TelaCadastro(
                        aoSalvar = { novoTitulo, novaDescricao ->
                            tarefasViewModel.salvarTarefa(
                                novoTitulo,
                                novaDescricao
                            )
                        },
                        aoVoltar = {
                            navController.popBackStack()
                        },
                        salvando = tarefasViewModel.salvando
                    )
                }
            }

            composable(route = Tela.Concluidas.name) {
                TelaConcluidas(
                    quantidadeConcluidas = quantidadeConcluidas,
                    tarefas = tarefasConcluidas,
                    aoVoltar = {
                        navController.popBackStack()
                    },
                    aoMudarConclusao = { id, marcada ->
                        tarefasViewModel.atualizarConclusao(id, marcada)
                    }
                )
            }
        } // fecha NavHost
    } // fecha Column principal
} // fecha Greeting


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        TarefasdaDiariaTheme {
            TelaInicial(
                nome = "Tarefas da Diária",
                quantidadeConcluidas = 0,
                tarefas = listOf(
                    Tarefa(id = 1, titulo = "Estudar Kotlin")
                ),
                aoAbrirCadastro = {},
                aoAbrirConcluidas = {},
                aoMudarConclusao = { _, _ -> }
            )
        }
    }

    fun tituloValido(titulo: String): Boolean {
        return titulo.isNotBlank()
    }



@Composable
fun ItemTarefa(
    tarefa: Tarefa,
    aoMudarConclusao: (Boolean) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = tarefa.concluida,
                    role = Role.Checkbox,
                    onValueChange = aoMudarConclusao
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarefa.concluida,
                onCheckedChange = null
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = tarefa.titulo,
                    style = MaterialTheme.typography.titleMedium
                )

                if (tarefa.descricao.isNotBlank()) {
                    Text(
                        text = tarefa.descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TelaCadastro(
    aoSalvar: (String, String) -> Unit,
    aoVoltar: () -> Unit,
    salvando: Boolean = false,
    modifier: Modifier = Modifier
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }

    val solicitarVolta: () -> Unit = {
        if (!salvando) {
            if (titulo.isNotBlank() || descricao.isNotBlank()) {
                confirmarSaida = true
            } else {
                aoVoltar()
            }
        }
    }

    BackHandler {
        solicitarVolta()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = solicitarVolta,
            enabled = !salvando
        ) {
            Text(text = "Voltar")
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { novoTitulo -> titulo = novoTitulo },
            label = { Text(text = "Título da tarefa") },
            singleLine = true,
            enabled = !salvando,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descricao,
            onValueChange = { novaDescricao ->
                descricao = novaDescricao
            },
            label = { Text(text = "Descrição (opcional)") },
            minLines = 3,
            enabled = !salvando,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                aoSalvar(titulo.trim(), descricao.trim())
            },
            enabled = tituloValido(titulo) && !salvando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (salvando) "Salvando..." else "Salvar tarefa"
            )
        }
    }

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = {
                Text(text = "Descartar rascunho?")
            },
            text = {
                Text(text = "O título e a descrição ainda não foram salvos.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                        aoVoltar()
                    }
                ) {
                    Text(
                        text = "Descartar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarSaida = false }
                ) {
                    Text(text = "Continuar editando")
                }
            }
        )
    }
}

@Composable
fun ListaTarefas(
    titulo: String,
    tarefas: List<Tarefa>,
    mensagemVazia: String,
    aoMudarConclusao: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium
        )

        if (tarefas.isEmpty()) {
            Text(
                text = mensagemVazia,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        for (tarefa in tarefas) {
            ItemTarefa(
                tarefa = tarefa,
                aoMudarConclusao = { marcada ->
                    aoMudarConclusao(tarefa.id, marcada)
                }
            )
        }
    }
}

    @Composable
    fun TelaInicial(
        nome: String,
        quantidadeConcluidas: Int,
        tarefas: List<Tarefa>,
        aoAbrirCadastro: () -> Unit,
        aoAbrirConcluidas: () -> Unit,
        aoMudarConclusao: (Int, Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = nome,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(text = "Concluídas: $quantidadeConcluidas")

            Button(
                onClick = aoAbrirCadastro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Nova tarefa")
            }

            Button(
                onClick = aoAbrirConcluidas,
                modifier = Modifier.fillMaxWidth()
                ) {
                Text(text = "Ver concluídas")
            }

            ListaTarefas(
                titulo = "Pendentes",
                tarefas = tarefas,
                mensagemVazia = "Nenhuma tarefa pendente",
                aoMudarConclusao = aoMudarConclusao,
                modifier = Modifier.testTag("lista_pendentes")
            )
        } // fecha a Column
    } // fecha TelaInicial

@Composable
fun TelaConcluidas(
    quantidadeConcluidas: Int,
    tarefas: List<Tarefa>,
    aoVoltar: () -> Unit,
    aoMudarConclusao: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Concluídas: $quantidadeConcluidas",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = aoVoltar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Voltar")
        }

        ListaTarefas(
            titulo = "Concluídas",
            tarefas = tarefas,
            mensagemVazia = "Nenhuma tarefa concluída",
            aoMudarConclusao = aoMudarConclusao,
            modifier = Modifier.testTag("lista_concluidas")
        )
    }
}