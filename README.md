# Tarefas da Diária

Aplicativo acadêmico Android desenvolvido em Kotlin e Jetpack Compose, com Material 3, Navigation Compose e persistência local usando Room/SQLite.

## Funcionalidades

- Cadastrar tarefas com título obrigatório e descrição opcional.
- Exibir pendentes, concluir e reabrir tarefas pelo identificador.
- Mostrar a quantidade de tarefas concluídas.
- Conservar as tarefas no banco local do dispositivo.
- Usar listas com rolagem e temas claro e escuro.
- Confirmar o descarte de um formulário preenchido ao voltar.

## Instalar no celular

Na área **Releases**, baixe `TarefasdaDiaria-1.0-debug.apk` e abra o arquivo no celular Android. O requisito mínimo é Android 7.0 (API 24).

O APK é uma versão de demonstração, assinada com chave de depuração. As arquiteturas incluídas são `arm64-v8a`, `armeabi-v7a`, `x86` e `x86_64`. As tarefas cadastradas ficam no dispositivo, em um banco Room/SQLite chamado `tarefas.db`.

## Abrir o projeto

1. Clone ou baixe o código deste repositório.
2. No Android Studio, abra a pasta que contém `settings.gradle.kts`.
3. Instale o Android SDK 37, se solicitado, e aguarde a sincronização do Gradle.
4. Selecione a configuração `app` e um celular ou emulador; execute com **Run**.

Ambiente usado no desenvolvimento: Android Studio 2026.2.1, JDK 25 para o Gradle, Gradle Wrapper 9.6.0 e Android Gradle Plugin 9.4.1. O código usa compatibilidade Java 11, `minSdk 24` e `targetSdk 37`. As dependências estão em `gradle/libs.versions.toml`.

Para gerar um APK no PowerShell, na raiz do projeto:

```powershell
.\gradlew.bat :app:assembleDebug
```

Também é possível usar **Build → Generate Bundle(s) / APK(s) → Generate APK(s)** no Android Studio. O APK compartilhável fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Testes e verificações

Comandos para executar os testes no PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

O segundo comando precisa de um emulador inicializado ou de um aparelho com depuração USB autorizada.

Verificações registradas durante o desenvolvimento:

- **02/11/2026:** três testes unitários de validação do título aprovados; um teste do banco aprovado, incluindo gravação, conclusão, reabertura e conservação dos dados após fechar e abrir o banco.
- **09/11/2026:** conferência visual dos temas claro e escuro nas três telas.
- **11/11/2026:** quatro testes de interface aprovados na mesma execução, no Medium Phone API 37.0: cadastro e limpeza dos campos; conclusão e reabertura; rolagem de listas longas; proteção do rascunho ao voltar.
- **11/11/2026:** verificação manual do formulário com teclado na horizontal e confirmação de descarte pelos botões do app e do Android.
- **11/11/2026:** aluno instalou o APK no próprio celular e confirmou o cadastro de uma tarefa com título e descrição. Modelo e versão do Android do celular não foram registrados.

Os testes de interface usam um banco isolado em memória. A conservação dos dados após encerramento real do processo do aplicativo foi verificada manualmente no emulador em 29/10/2026. Essas evidências correspondem a cenários e datas diferentes.

## Organização do código

| Arquivo | Responsabilidade |
| --- | --- |
| `MainActivity.kt` | Telas, navegação, formulário, itens e validação do título. |
| `Tarefa.kt` | Entidade com identificador, título, descrição e estado. |
| `TarefaDao.kt` | Consultas, inserção e atualização de conclusão por ID. |
| `BancoTarefas.kt` | Configuração e instância do banco Room. |
| `TarefasViewModel.kt` | Estado observado pelas telas e operações no banco. |
| `ui/theme/` | Cores, tema e tipografia. |

Ao salvar, o formulário chama o ViewModel, que insere a tarefa pelo DAO. O Room emite a lista atualizada; a interface deriva dela os grupos e o contador. A mudança de conclusão também usa o DAO e identifica a tarefa pelo ID.

Esta publicação contém uma cópia do código da versão de entrega. O histórico de estudo, as preferências locais do Android Studio e os materiais de apoio ficam no projeto local.
