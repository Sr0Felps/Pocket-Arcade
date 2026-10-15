package br.edu.ads.pocketarcade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.edu.ads.pocketarcade.databinding.ActivityRandomizerBinding;

/**
 * TELA 6: RANDOMIZADOR DE PALAVRAS (RandomizerActivity)
 * Cumpre rigorosamente os requisitos dos Slides 8, 11, 12 e 13:
 * 1. Usa RecyclerView, WordAdapter, WordViewHolder e a classe de modelo WordItem.
 * 2. Cadastra novos itens com notifyItemInserted() e rola até o novo item (scrollToPosition).
 * 3. Exclui itens por clique longo atualizando a lista com notifyItemRemoved().
 * 4. Alterna dinamicamente entre LinearLayoutManager (Lista) e GridLayoutManager (Grade).
 * 5. Sorteia apenas na categoria selecionada no Spinner, com animação XML e som (MediaPlayer).
 */
public class RandomizerActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida_Tela6";

    private ActivityRandomizerBinding binding;
    private String apelidoJogador;

    private final List<WordItem> listaPalavras = new ArrayList<>();
    private WordAdapter adapter;
    private boolean modoGradeAtivo = false;
    private final Random randomGenerator = new Random();

    // Instância de MediaPlayer criada em onCreate, pausada em onStop e liberada em onDestroy (Slides 8 e 11)
    private MediaPlayer playerSorteio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Tela 6 (Randomizador) criada.");

        // Inicializa o View Binding (Slide 13)
        binding = ActivityRandomizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Valida o apelido recebido por Intent (Slide 12)
        validarExtrasNoDestino();

        // 2. Cria a instância de MediaPlayer no onCreate (Regra do Slide 8 aplicada ao som do Slide 11)
        playerSorteio = MediaPlayer.create(this, R.raw.som_vitoria);

        // 3. Carrega itens iniciais nas 4 categorias obrigatórias para facilitar testes imediatos
        carregarItensIniciais();

        // 4. Configura a RecyclerView com Adapter e LinearLayoutManager inicial
        configurarRecyclerView();

        // 5. Configura os botões de Adicionar, Sortear, Lista/Grade e Navegação
        configurarBotoes();
    }

    /**
     * Valida o extra recebido na Intent no destino (Slide 12).
     */
    private void validarExtrasNoDestino() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_NICKNAME)) {
            apelidoJogador = intent.getStringExtra(MainActivity.EXTRA_NICKNAME);
        }
        if (apelidoJogador == null || apelidoJogador.trim().isEmpty()) {
            apelidoJogador = getString(R.string.t2_default_player);
        }
    }

    /**
     * Popula a lista inicial com exemplos nas categorias Filmes, Comidas, Lugares e Desafios (Slide 11).
     */
    private void carregarItensIniciais() {
        listaPalavras.add(new WordItem("Interestelar", "Filmes"));
        listaPalavras.add(new WordItem("O Poderoso Chefão", "Filmes"));
        listaPalavras.add(new WordItem("Ramen Tradicional", "Comidas"));
        listaPalavras.add(new WordItem("Risoto de Funghi", "Comidas"));
        listaPalavras.add(new WordItem("Tóquio", "Lugares"));
        listaPalavras.add(new WordItem("Chapada dos Veadeiros", "Lugares"));
        listaPalavras.add(new WordItem("Zerar sem perder vida", "Desafios"));
        listaPalavras.add(new WordItem("Programar sem bugs", "Desafios"));
    }

    /**
     * Inicializa a RecyclerView com LinearLayoutManager e define o callback de clique longo
     * para exclusão com notifyItemRemoved() (Slide 11).
     */
    private void configurarRecyclerView() {
        adapter = new WordAdapter(listaPalavras, position -> {
            // POR QUE ISSO FOI FEITO (Slide 11):
            // "Exclua por clique longo com notifyItemRemoved()."
            if (position >= 0 && position < listaPalavras.size()) {
                listaPalavras.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(this, R.string.t6_item_deleted, Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvPalavras.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPalavras.setAdapter(adapter);
    }

    /**
     * Configura os cliques dos botões Adicionar, Sortear, Alternar Lista/Grade e Navegação.
     */
    private void configurarBotoes() {
        // Botão ADICIONAR (Slide 11)
        binding.btnAdicionarPalavra.setOnClickListener(v -> adicionarNovoItem());

        // Botão SORTEAR (Slide 11)
        binding.btnSortearPalavra.setOnClickListener(v -> sortearPorCategoria());

        // Botão LISTA / GRADE (Slide 11: alterna LinearLayoutManager e GridLayoutManager)
        binding.btnAlternarLayout.setOnClickListener(v -> alternarLayoutManager());

        // Botão VOLTAR À CENTRAL com finish() (Slides 3 e 12)
        binding.btnVoltarCentralRandom.setOnClickListener(v -> finish());

        // Botão ENCERRAR SESSÃO retornando à Tela 1 com CLEAR_TOP | SINGLE_TOP (Slide 12)
        binding.btnEncerrarSessaoRandom.setOnClickListener(v -> encerrarSessaoParaTela1());
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 11):
     * Valida o campo de texto, insere o novo WordItem na lista, notifica o Adapter com
     * notifyItemInserted() e rola suavemente a RecyclerView até o novo item cadastrado.
     */
    private void adicionarNovoItem() {
        String palavraDigitada = binding.etPalavraRandom.getText().toString().trim();

        if (palavraDigitada.length() < 2) {
            binding.etPalavraRandom.setError(getString(R.string.t6_err_empty_word));
            binding.etPalavraRandom.requestFocus();
            Toast.makeText(this, R.string.t6_err_empty_word, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.etPalavraRandom.setError(null);
        String categoriaSelecionada = binding.spinnerCategorias.getSelectedItem().toString();

        // Adiciona ao final da lista e obtém o índice recém-inserido
        WordItem novoItem = new WordItem(palavraDigitada, categoriaSelecionada);
        listaPalavras.add(novoItem);
        int novaPosicao = listaPalavras.size() - 1;

        // Atualiza a RecyclerView com notifyItemInserted() e rola até o novo item (Slide 11)
        adapter.notifyItemInserted(novaPosicao);
        binding.rvPalavras.scrollToPosition(novaPosicao);

        // Limpa o campo para o próximo cadastro
        binding.etPalavraRandom.setText("");
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 11):
     * "Alterne LinearLayoutManager e GridLayoutManager."
     */
    private void alternarLayoutManager() {
        modoGradeAtivo = !modoGradeAtivo;
        if (modoGradeAtivo) {
            binding.rvPalavras.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            binding.rvPalavras.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 11):
     * "Sorteie apenas na categoria selecionada, com animação e som."
     */
    private void sortearPorCategoria() {
        String categoriaAlvo = binding.spinnerCategorias.getSelectedItem().toString();

        // Filtra apenas os itens pertencentes à categoria atualmente selecionada no Spinner
        List<WordItem> itensDaCategoria = new ArrayList<>();
        for (WordItem item : listaPalavras) {
            if (item.getCategoria().equalsIgnoreCase(categoriaAlvo)) {
                itensDaCategoria.add(item);
            }
        }

        if (itensDaCategoria.isEmpty()) {
            String msgErro = getString(R.string.t6_err_empty_category, categoriaAlvo);
            Toast.makeText(this, msgErro, Toast.LENGTH_SHORT).show();
            return;
        }

        // Sorteia um item aleatório dentro da categoria filtrada
        int indiceSorteado = randomGenerator.nextInt(itensDaCategoria.size());
        WordItem itemSorteado = itensDaCategoria.get(indiceSorteado);

        // Exibe o resultado formatado
        String textoResultado = getString(
                R.string.t6_draw_result_format,
                itemSorteado.getCategoria(),
                itemSorteado.getPalavra()
        );
        binding.tvResultadoSorteio.setText(textoResultado);
        binding.tvResultadoSorteio.setVisibility(View.VISIBLE);

        // Aplica animação XML ao resultado do sorteio (Slide 11)
        Animation animacaoSorteio = AnimationUtils.loadAnimation(this, R.anim.anim_feedback_resultado);
        binding.tvResultadoSorteio.startAnimation(animacaoSorteio);

        // Reproduz o efeito sonoro reutilizando a instância criada no onCreate (Slides 8 e 11)
        if (playerSorteio != null) {
            if (playerSorteio.isPlaying()) {
                playerSorteio.pause();
            }
            playerSorteio.seekTo(0);
            playerSorteio.start();
        }
    }

    /**
     * Retorna diretamente à Tela 1 limpando a pilha com CLEAR_TOP | SINGLE_TOP (Slide 12).
     */
    private void encerrarSessaoParaTela1() {
        Intent intent = new Intent(RandomizerActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LAST_MODULE, getString(R.string.t6_title));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ================= CICLO DE VIDA E GESTÃO DO MEDIAPLAYER (Slides 8 e 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 6 (Randomizador) em primeiro plano.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 6 (Randomizador) oculta. Pausando áudio se estiver ativo.");

        // Regra do Slide 8: Ao sair da tela, usar pause() e nunca stop()
        if (playerSorteio != null && playerSorteio.isPlaying()) {
            playerSorteio.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 6 (Randomizador) destruída. Libertando MediaPlayer com release().");

        // Regra do Slide 8: Em onDestroy(), chamar release() para libertar recursos
        if (playerSorteio != null) {
            playerSorteio.release();
            playerSorteio = null;
        }
    }
}