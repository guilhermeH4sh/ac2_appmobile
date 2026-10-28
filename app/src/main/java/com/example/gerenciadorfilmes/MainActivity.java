package com.example.gerenciadorfilmes;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Tela única do app: formulário de cadastro/edição + filtros + lista.
 * Toda a persistência é feita no Cloud Firestore (coleção "filmes").
 */
public class MainActivity extends AppCompatActivity implements FilmeAdapter.OnFilmeListener {

    private static final String COLECAO = "filmes";

    // Componentes do formulário
    private EditText etNome, etAno, etNota;
    private Spinner spTipo, spGenero;
    private CheckBox cbAssistido;
    private Button btnSalvar, btnCancelar;
    private TextView tvTituloForm;

    // Filtros e lista
    private Spinner spFiltroTipo;
    private CheckBox cbFiltroAssistidos;
    private TextView tvVazio;
    private RecyclerView rvFilmes;
    private FilmeAdapter adapter;

    // Firebase
    private CollectionReference colecao;

    // Todos os registros vindos do Firestore (a lista filtrada é derivada desta)
    private final List<Filme> todosOsFilmes = new ArrayList<>();

    // Se for null estamos cadastrando; se tiver valor, estamos editando esse documento
    private String idEmEdicao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vincularComponentes();
        configurarSpinners();
        configurarLista();
        configurarEventos();

        // Conexão com o Firestore
        colecao = FirebaseFirestore.getInstance().collection(COLECAO);
        escutarFirestore();
    }

    private void vincularComponentes() {
        tvTituloForm = findViewById(R.id.tvTituloForm);
        etNome = findViewById(R.id.etNome);
        etAno = findViewById(R.id.etAno);
        etNota = findViewById(R.id.etNota);
        spTipo = findViewById(R.id.spTipo);
        spGenero = findViewById(R.id.spGenero);
        cbAssistido = findViewById(R.id.cbAssistido);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);
        spFiltroTipo = findViewById(R.id.spFiltroTipo);
        cbFiltroAssistidos = findViewById(R.id.cbFiltroAssistidos);
        tvVazio = findViewById(R.id.tvVazio);
        rvFilmes = findViewById(R.id.rvFilmes);
    }

    private void configurarSpinners() {
        spTipo.setAdapter(criarAdapter(R.array.tipos_cadastro));
        spGenero.setAdapter(criarAdapter(R.array.generos_cadastro));
        spFiltroTipo.setAdapter(criarAdapter(R.array.tipos_filtro));
    }

    private ArrayAdapter<CharSequence> criarAdapter(int arrayRes) {
        ArrayAdapter<CharSequence> a = ArrayAdapter.createFromResource(
                this, arrayRes, android.R.layout.simple_spinner_item);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void configurarLista() {
        adapter = new FilmeAdapter(this);
        rvFilmes.setLayoutManager(new LinearLayoutManager(this));
        rvFilmes.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnSalvar.setOnClickListener(v -> salvar());
        btnCancelar.setOnClickListener(v -> limparFormulario());

        // Qualquer mudança nos filtros reaplica o filtro na lista
        spFiltroTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cbFiltroAssistidos.setOnCheckedChangeListener((b, marcado) -> aplicarFiltros());
    }

    // =====================================================================
    // FIREBASE: CONSULTA (READ)
    // O SnapshotListener é chamado ao abrir o app e SEMPRE que algo muda
    // no banco (inclusão, edição ou exclusão), mantendo a lista atualizada.
    // =====================================================================
    private void escutarFirestore() {
        colecao.orderBy("nome").addSnapshotListener(this, (snapshots, erro) -> {
            if (erro != null) {
                Toast.makeText(this, "Erro ao carregar dados: " + erro.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }
            todosOsFilmes.clear();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    todosOsFilmes.add(converter(doc));
                }
            }
            aplicarFiltros();
        });
    }

    private Filme converter(DocumentSnapshot doc) {
        Filme filme = doc.toObject(Filme.class);
        if (filme == null) {
            filme = new Filme();
        }
        filme.setId(doc.getId());
        return filme;
    }

    // Filtro por tipo (Spinner) + extra: apenas já assistidos (CheckBox)
    private void aplicarFiltros() {
        String tipoFiltro = spFiltroTipo.getSelectedItem().toString();
        boolean somenteAssistidos = cbFiltroAssistidos.isChecked();

        List<Filme> filtrados = new ArrayList<>();
        for (Filme f : todosOsFilmes) {
            boolean passaTipo = tipoFiltro.equals("Todos") || tipoFiltro.equals(f.getTipo());
            boolean passaAssistido = !somenteAssistidos || f.isAssistido();
            if (passaTipo && passaAssistido) {
                filtrados.add(f);
            }
        }

        adapter.setLista(filtrados);
        tvVazio.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // =====================================================================
    // FIREBASE: CADASTRO (CREATE) e ATUALIZAÇÃO (UPDATE)
    // =====================================================================
    private void salvar() {
        if (!validarCampos()) {
            return;
        }

        String nome = etNome.getText().toString().trim();
        String tipo = spTipo.getSelectedItem().toString();
        String genero = spGenero.getSelectedItem().toString();
        int ano = Integer.parseInt(etAno.getText().toString().trim());
        double nota = Double.parseDouble(etNota.getText().toString().trim().replace(',', '.'));
        boolean assistido = cbAssistido.isChecked();

        Filme filme = new Filme(nome, tipo, genero, ano, nota, assistido);

        if (idEmEdicao == null) {
            // CREATE: add() cria um documento novo com id gerado automaticamente
            colecao.add(filme)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        limparFormulario();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,
                            "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            // UPDATE: set() substitui os dados do documento que já existe
            colecao.document(idEmEdicao).set(filme)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        limparFormulario();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,
                            "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    // =====================================================================
    // VALIDAÇÕES (valem para cadastro e para atualização)
    // =====================================================================
    private boolean validarCampos() {
        String nome = etNome.getText().toString().trim();
        String anoTexto = etAno.getText().toString().trim();
        String notaTexto = etNota.getText().toString().trim().replace(',', '.');

        if (nome.isEmpty()) {
            etNome.setError("Informe o nome");
            etNome.requestFocus();
            return false;
        }

        // posição 0 é o texto "Selecione...", então não é uma escolha válida
        if (spTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo", Toast.LENGTH_SHORT).show();
            spTipo.requestFocus();
            return false;
        }

        if (spGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show();
            spGenero.requestFocus();
            return false;
        }

        if (anoTexto.isEmpty()) {
            etAno.setError("Informe o ano de lançamento");
            etAno.requestFocus();
            return false;
        }
        int ano = Integer.parseInt(anoTexto);
        int anoMaximo = Calendar.getInstance().get(Calendar.YEAR) + 5;
        if (ano < 1888 || ano > anoMaximo) {
            etAno.setError("Ano deve estar entre 1888 e " + anoMaximo);
            etAno.requestFocus();
            return false;
        }

        if (notaTexto.isEmpty()) {
            etNota.setError("Informe a nota pessoal");
            etNota.requestFocus();
            return false;
        }
        double nota;
        try {
            nota = Double.parseDouble(notaTexto);
        } catch (NumberFormatException e) {
            etNota.setError("Nota inválida");
            etNota.requestFocus();
            return false;
        }
        if (nota < 0 || nota > 10) {
            etNota.setError("A nota deve ficar entre 0 e 10");
            etNota.requestFocus();
            return false;
        }

        return true;
    }

    // =====================================================================
    // AÇÕES NA LISTA
    // =====================================================================

    // Clique curto: carrega os dados nos campos para edição
    @Override
    public void onClique(Filme filme) {
        idEmEdicao = filme.getId();
        tvTituloForm.setText("Editando: " + filme.getNome());
        btnSalvar.setText("Atualizar");

        etNome.setText(filme.getNome());
        etAno.setText(String.valueOf(filme.getAno()));
        etNota.setText(String.valueOf(filme.getNota()));
        cbAssistido.setChecked(filme.isAssistido());
        selecionarNoSpinner(spTipo, filme.getTipo());
        selecionarNoSpinner(spGenero, filme.getGenero());
    }

    // Clique longo: opção de excluir (extra: diálogo de confirmação)
    @Override
    public void onCliqueLongo(Filme filme) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Deseja realmente excluir \"" + filme.getNome() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> excluir(filme))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // =====================================================================
    // FIREBASE: EXCLUSÃO (DELETE)
    // =====================================================================
    private void excluir(Filme filme) {
        colecao.document(filme.getId()).delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    // Se o item excluído estava aberto para edição, limpa o formulário
                    if (filme.getId().equals(idEmEdicao)) {
                        limparFormulario();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // =====================================================================
    // AUXILIARES
    // =====================================================================
    private void selecionarNoSpinner(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private void limparFormulario() {
        idEmEdicao = null;
        tvTituloForm.setText("Novo filme ou série");
        btnSalvar.setText("Salvar");
        etNome.setText("");
        etAno.setText("");
        etNota.setText("");
        etNome.setError(null);
        etAno.setError(null);
        etNota.setError(null);
        spTipo.setSelection(0);
        spGenero.setSelection(0);
        cbAssistido.setChecked(false);
        etNome.requestFocus();
    }
}
