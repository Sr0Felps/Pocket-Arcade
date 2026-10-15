package br.edu.ads.pocketarcade;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.ads.pocketarcade.databinding.ItemWordBinding;

/**
 * ADAPTER E VIEWHOLDER DA RECYCLERVIEW (WordAdapter)
 * POR QUE ISSO FOI FEITO (Slides 11 e 13):
 * "O Adapter fornece as views dos itens; o ViewHolder as guarda para reutilização."
 * Utilizamos ItemWordBinding (View Binding) dentro do ViewHolder para cumprir a
 * proibição absoluta de findViewById() mesmo dentro de listas dinâmicas.
 */
public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    /**
     * Interface de callback para tratar o clique longo de exclusão na Activity (Slide 11).
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private final List<WordItem> listaItens;
    private final OnItemLongClickListener longClickListener;

    public WordAdapter(List<WordItem> listaItens, OnItemLongClickListener longClickListener) {
        this.listaItens = listaItens;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout item_word.xml usando View Binding
        ItemWordBinding binding = ItemWordBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordItem itemAtual = listaItens.get(position);
        holder.vincularDados(itemAtual);
    }

    @Override
    public int getItemCount() {
        return listaItens.size();
    }

    /**
     * VIEWHOLDER: Guarda a referência do ItemWordBinding para reciclagem eficiente de memória.
     */
    public class WordViewHolder extends RecyclerView.ViewHolder {

        private final ItemWordBinding itemBinding;

        public WordViewHolder(@NonNull ItemWordBinding binding) {
            super(binding.getRoot());
            this.itemBinding = binding;

            // POR QUE ISSO FOI FEITO (Slide 11):
            // "Exclua por clique longo com notifyItemRemoved()."
            // Usamos getBindingAdapterPosition() para obter a posição exata e segura do item.
            this.itemBinding.getRoot().setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(pos);
                    return true;
                }
                return false;
            });
        }

        public void vincularDados(WordItem item) {
            itemBinding.tvItemPalavra.setText(item.getPalavra());
            itemBinding.tvItemCategoria.setText(item.getCategoria());
        }
    }
}