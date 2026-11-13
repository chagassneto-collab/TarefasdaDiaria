package com.example.tarefasdadiaria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
class TarefasViewModel(
    private val tarefaDao: TarefaDao
) : ViewModel() {
    var salvando by mutableStateOf(false)
        private set

    var cadastroSalvo by mutableStateOf(false)
        private set

    var mensagemErro by mutableStateOf<String?>(null)
        private set

    var carregando by mutableStateOf(true)
        private set

    var erroLeitura by mutableStateOf<String?>(null)
        private set

    val tarefas: StateFlow<List<Tarefa>> =
        tarefaDao.observarTodas()
            .onEach {
                carregando = false
                erroLeitura = null
            }
            .catch { erro ->
                Log.e("TarefasViewModel", "Erro ao carregar tarefas", erro)
                erroLeitura = "Não foi possível carregar as tarefas."
                carregando = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun salvarTarefa(titulo: String, descricao: String) {
        if (salvando || !tituloValido(titulo)) {
            return
        }

        salvando = true
        cadastroSalvo = false
        mensagemErro = null

        viewModelScope.launch {
            try {
                val novaTarefa = Tarefa(
                    titulo = titulo.trim(),
                    descricao = descricao.trim()
                )

                tarefaDao.inserir(novaTarefa)
                cadastroSalvo = true
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                Log.e("TarefasViewModel", "Erro ao salvar tarefa", erro)
                mensagemErro = "Não foi possível salvar a tarefa. Tente novamente."
            } finally {
                salvando = false
            }
        }
    }

    fun atualizarConclusao(id: Int, concluida: Boolean) {
        mensagemErro = null

        viewModelScope.launch {
            try {
                val quantidadeAlterada =
                    tarefaDao.atualizarConclusao(id, concluida)

                if (quantidadeAlterada == 0) {
                    mensagemErro = "A tarefa não foi encontrada."
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                Log.e("TarefasViewModel", "Erro ao atualizar tarefa", erro)
                mensagemErro = "Não foi possível atualizar a tarefa. Tente novamente."
            }
        }
    }

    fun consumirCadastroSalvo() {
        cadastroSalvo = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacao = checkNotNull(this[APPLICATION_KEY])
                val banco = BancoTarefas.obterInstancia(aplicacao)

                TarefasViewModel(
                    tarefaDao = banco.tarefaDao()
                )
            }
        }
    }


}

