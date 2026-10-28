package com.example.gerenciadorfilmes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter do RecyclerView: transforma cada objeto Filme em uma linha da lista
 * e repassa clique curto / clique longo para a MainActivity.
 */
public class FilmeAdapter extends RecyclerView.Adapter<FilmeAdapter.FilmeViewHolder> {

    public interface OnFilmeListener {
        void onClique(Filme filme);      // clique curto -> editar
        void onCliqueLongo(Filme filme); // clique longo -> excluir
    }

    private final List<Filme> lista = new ArrayList<>();
    private final OnFilmeListener listener;

    public FilmeAdapter(OnFilmeListener listener) {
        this.listener = listener;
    }

    public void setLista(List<Filme> novaLista) {
        lista.clear();
        lista.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filme, parent, false);
        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmeViewHolder holder, int position) {
        Filme filme = lista.get(position);

        holder.tvNome.setText(filme.getNome());
        holder.tvDetalhes.setText(filme.getTipo() + " • " + filme.getGenero()
                + " • " + filme.getAno());
        holder.tvNota.setText(String.format(Locale.getDefault(), "Nota: %.1f", filme.getNota()));
        holder.tvAssistido.setText(filme.isAssistido() ? "✔ Já assistiu" : "Quero assistir");

        holder.itemView.setOnClickListener(v -> listener.onClique(filme));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onCliqueLongo(filme);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class FilmeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNome, tvDetalhes, tvNota, tvAssistido;

        FilmeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhes);
            tvNota = itemView.findViewById(R.id.tvNota);
            tvAssistido = itemView.findViewById(R.id.tvAssistido);
        }
    }
}
