package br.edu.ads.pocketarcade;

/**
 * CLASSE DE MODELO (WordItem)
 * POR QUE ISSO FOI FEITO:
 * O Slide 11 e o Slide 13 exigem uma classe de modelo dedicada para representar
 * cada item cadastrado na RecyclerView, encapsulando a palavra e a sua categoria.
 */
public class WordItem {

    private final String palavra;
    private final String categoria;

    public WordItem(String palavra, String categoria) {
        this.palavra = palavra;
        this.categoria = categoria;
    }

    public String getPalavra() {
        return palavra;
    }

    public String getCategoria() {
        return categoria;
    }
}