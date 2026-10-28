# AC2 – Gerenciador de Filmes e Séries

App Android (Java) para cadastrar, listar, editar e excluir filmes e séries.
Os dados são salvos no **Cloud Firestore** (coleção `filmes`), então continuam
disponíveis depois de fechar e abrir o app.

## Como rodar

1. Abra a pasta do projeto no **Android Studio** (ele baixa o Gradle sozinho).
2. Crie um projeto no [Firebase Console](https://console.firebase.google.com).
3. Em **Visão geral > Adicionar app > Android**, use o nome do pacote
   `com.example.gerenciadorfilmes` e baixe o `google-services.json`.
4. Coloque o arquivo em `app/google-services.json`.
5. No Firebase, vá em **Build > Firestore Database > Criar banco de dados**.
   Para a atividade, escolha o modo de teste (em **Regras**, permita leitura e escrita):

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```
   (Regras abertas servem apenas para a atividade; não use em produção.)
6. Clique em **Sync Project with Gradle Files** e execute no emulador ou celular.

## Funcionalidades

| Requisito | Onde está |
|---|---|
| Cadastro (nome, tipo, gênero, ano, nota, já assistiu) | `MainActivity.salvar()` |
| Listagem (nome, tipo, gênero, ano, nota) | `FilmeAdapter` + `item_filme.xml` |
| Filtro por tipo (Spinner) | `MainActivity.aplicarFiltros()` |
| Clique curto: carregar para edição | `MainActivity.onClique()` |
| Clique longo: excluir | `MainActivity.onCliqueLongo()` / `excluir()` |
| Validações | `MainActivity.validarCampos()` |
| **Extra 1:** apenas já assistidos | CheckBox `cbFiltroAssistidos` |
| **Extra 2:** confirmação antes de excluir | `AlertDialog` em `onCliqueLongo()` |

## Estrutura

```
app/src/main/java/com/example/gerenciadorfilmes/
├── Filme.java          modelo (vira documento no Firestore)
├── FilmeAdapter.java   liga os dados à lista (RecyclerView)
└── MainActivity.java   tela, validações, filtros e CRUD no Firebase
app/src/main/res/layout/
├── activity_main.xml   formulário + filtros + lista
└── item_filme.xml      layout de cada linha da lista
```

## Uso do Firebase (CRUD)

| Operação | Código |
|---|---|
| Create | `colecao.add(filme)` |
| Read | `colecao.orderBy("nome").addSnapshotListener(...)` (atualiza a lista em tempo real) |
| Update | `colecao.document(id).set(filme)` |
| Delete | `colecao.document(id).delete()` |

## Roteiro sugerido para a apresentação

1. Explicar o objetivo do app e a tela única (formulário em cima, lista embaixo).
2. **Cadastro:** cadastrar um filme e uma série.
3. **Listagem:** mostrar os itens e conferir no Firebase Console que os documentos foram criados.
4. **Edição:** clique curto em um item, mudar a nota, clicar em Atualizar (e mostrar a mudança no Console).
5. **Exclusão:** clique longo, confirmar e mostrar que sumiu da lista e do Console.
6. **Filtros:** filtrar por tipo e marcar "apenas já assistidos".
7. **Validações:** tentar salvar com nome vazio, sem tipo/gênero, sem ano e sem nota.
8. Fechar e abrir o app para provar a persistência.
9. Mostrar o código: `Filme`, `FilmeAdapter` e os métodos de `MainActivity`.
