package com.example.gerenciadorfilmes;

import com.google.firebase.firestore.Exclude;

/**
 * Modelo de dados. Cada objeto Filme vira um documento na coleção "filmes"
 * do Cloud Firestore. O Firestore exige um construtor vazio e getters/setters
 * para converter documento <-> objeto automaticamente.
 */
public class Filme {

    // O id é o identificador do documento no Firestore, não é salvo como campo.
    private String id;
    private String nome;
    private String tipo;
    private String genero;
    private int ano;
    private double nota;
    private boolean assistido;

    public Filme() {
        // Obrigatório para o Firestore
    }

    public Filme(String nome, String tipo, String genero, int ano, double nota, boolean assistido) {
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.ano = ano;
        this.nota = nota;
        this.assistido = assistido;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public boolean isAssistido() {
        return assistido;
    }

    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }
}
