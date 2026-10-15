package br.edu.ads.pocketarcade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import br.edu.ads.pocketarcade.databinding.ActivityArenaBinding;

/**
 * TELA 4: ARENA DE PEDRA, PAPEL E TESOURA (ArenaActivity)
 * Cumpre rigorosamente os requisitos dos Slides 7, 8, 12 e 13:
 * 1. Disputa em melhor de três (vence quem alcançar 2 vitórias primeiro; empates não somam pontos).
 * 2. Sorteia a jogada do dispositivo e anima as escolhas do jogador, do dispositivo e o resultado (450ms).
 * 3. Gerencia o ciclo de vida de 2 instâncias de MediaPlayer:
 *    - Criadas uma única vez em onCreate() (nunca dentro de listeners).
 *    - Pausadas em onStop() com pause() (nunca usando stop()).
 *    - Liberadas em onDestroy() com release().
 */
public class ArenaActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida_Tela4";

    // Constantes numéricas para representar as jogadas de forma clara
    private static final int JOGADA_PEDRA = 0;
    private static final int JOGADA_PAPEL = 1;
    private static final int JOGADA_TESOURA = 2;

    private ActivityArenaBinding binding;
    private final Random randomGenerator = new Random();

    private String apelidoJogador;
    private int vitoriasJogador = 0;
    private int vitoriasDispositivo = 0;
    private boolean partidaEncerrada = false;

    // Instâncias de MediaPlayer declaradas como atributos da Activity para reutilização (Slide 8)
    private MediaPlayer playerVitoria;
    private MediaPlayer playerDerrota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Tela 4 (Arena) criada e inicializando recursos de áudio.");

        // Inicializa o View Binding (proibido findViewById - Slide 13)
        binding = ActivityArenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Valida o apelido recebido por Intent (Slide 12)
        validarExtrasNoDestino();

        // 2. REGRA 01 DO MEDIAPLAYER (Slide 8):
        // "Em onCreate(), crie e reutilize instâncias de MediaPlayer; nunca crie dentro de listeners."
        inicializarMediaPlayers();

        // 3. Configura os listeners das 3 jogadas, Nova Partida e navegação
        configurarBotoes();

        // 4. Atualiza o placar inicial (0 x 0)
        atualizarPlacarVisual();
    }

    /**
     * Valida o apelido enviado pela CentralActivity e preenche os rótulos da Arena.
     */
    private void validarExtrasNoDestino() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_NICKNAME)) {
            apelidoJogador = intent.getStringExtra(MainActivity.EXTRA_NICKNAME);
        }
        if (apelidoJogador == null || apelidoJogador.trim().isEmpty()) {
            apelidoJogador = getString(R.string.t2_default_player);
        }
        binding.tvLabelEscolhaJogador.setText(apelidoJogador);
    }

    /**
     * Cria as duas instâncias de efeitos sonoros a partir de res/raw/ uma única vez no onCreate().
     */
    private void inicializarMediaPlayers() {
        playerVitoria = MediaPlayer.create(this, R.raw.som_vitoria);
        playerDerrota = MediaPlayer.create(this, R.raw.som_derrota);
    }

    /**
     * Configura os cliques das três opções de jogada (Pedra, Papel e Tesoura) e controles da tela.
     */
    private void configurarBotoes() {
        binding.btnJogadaPedra.setOnClickListener(v -> processarRodada(JOGADA_PEDRA));
        binding.btnJogadaPapel.setOnClickListener(v -> processarRodada(JOGADA_PAPEL));
        binding.btnJogadaTesoura.setOnClickListener(v -> processarRodada(JOGADA_TESOURA));

        // Botão Nova Partida (Slide 7: reinicia o placar e reabilita as jogadas)
        binding.btnNovaPartidaArena.setOnClickListener(v -> reiniciarPartida());

        // Botão Voltar à Central usando exclusivamente finish() (Slide 3 e 12)
        binding.btnVoltarCentralArena.setOnClickListener(v -> finish());

        // Botão Encerrar Sessão retornando à Tela 1 com CLEAR_TOP | SINGLE_TOP (Slide 12)
        binding.btnEncerrarSessaoArena.setOnClickListener(v -> encerrarSessaoParaTela1());
    }

    /**
     * Executa uma rodada da Arena: sorteia a jogada da CPU, verifica o vencedor da rodada,
     * atualiza o placar da melhor de três, anima as escolhas e toca o som correspondente.
     */
    private void processarRodada(int escolhaJogador) {
        if (partidaEncerrada) {
            return;
        }

        // Sorteia a jogada do dispositivo (0 = Pedra, 1 = Papel, 2 = Tesoura)
        int escolhaDispositivo = randomGenerator.nextInt(3);

        // Exibe os nomes das escolhas nos cartões individuais
        binding.tvValorEscolhaJogador.setText(obterNomeJogada(escolhaJogador));
        binding.tvValorEscolhaDispositivo.setText(obterNomeJogada(escolhaDispositivo));

        // ANIMAÇÃO DAS ESCOLHAS (Slide 8: 300 a 700 ms):
        // Usamos ViewPropertyAnimator com duração de 450ms para animar os cartões de escolha.
        animarCartaoEscolha(binding.cardEscolhaJogador);
        animarCartaoEscolha(binding.cardEscolhaDispositivo);

        // Avalia o resultado da rodada (Empates não somam pontos - Slide 7)
        if (escolhaJogador == escolhaDispositivo) {
            binding.tvMensagemResultadoArena.setText(R.string.t4_round_tie);
            binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.accent_champagne_gold));
            reproduzirSomSeguro(playerDerrota);
        } else if ((escolhaJogador == JOGADA_PEDRA && escolhaDispositivo == JOGADA_TESOURA) ||
                (escolhaJogador == JOGADA_PAPEL && escolhaDispositivo == JOGADA_PEDRA) ||
                (escolhaJogador == JOGADA_TESOURA && escolhaDispositivo == JOGADA_PAPEL)) {
            // Vitória do Jogador na rodada
            vitoriasJogador++;
            binding.tvMensagemResultadoArena.setText(R.string.t4_round_win);
            binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.accent_jade_teal));
            reproduzirSomSeguro(playerVitoria);
        } else {
            // Vitória do Dispositivo na rodada
            vitoriasDispositivo++;
            binding.tvMensagemResultadoArena.setText(R.string.t4_round_loss);
            binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.accent_terracotta));
            reproduzirSomSeguro(playerDerrota);
        }

        atualizarPlacarVisual();

        // Verifica se alguém alcançou 2 vitórias primeiro (Melhor de Três - Slide 7)
        verificarFimDaMelhorDeTres();

        // ANIMAÇÃO DA MENSAGEM DE RESULTADO (Slide 8: animação XML de 450ms)
        Animation animacaoResultado = AnimationUtils.loadAnimation(this, R.anim.anim_feedback_resultado);
        binding.tvMensagemResultadoArena.startAnimation(animacaoResultado);
    }

    /**
     * Anima uma view de escolha usando ViewPropertyAnimator com duração de 450ms
     * (estritamente dentro do intervalo de 300 a 700 ms exigido no Slide 8 e sem usar Runnable).
     */
    private void animarCartaoEscolha(View cardView) {
        cardView.setScaleX(0.80f);
        cardView.setScaleY(0.80f);
        cardView.setAlpha(0.4f);
        cardView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(450)
                .start();
    }

    /**
     * Verifica se o jogador ou o dispositivo atingiu 2 vitórias, encerrando a partida atual.
     */
    private void verificarFimDaMelhorDeTres() {
        if (vitoriasJogador >= 2) {
            partidaEncerrada = true;
            binding.tvMensagemResultadoArena.setText(R.string.t4_match_win);
            binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.accent_jade_teal));
            definirEstadoBotoesJogada(false);
        } else if (vitoriasDispositivo >= 2) {
            partidaEncerrada = true;
            binding.tvMensagemResultadoArena.setText(R.string.t4_match_loss);
            binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.accent_terracotta));
            definirEstadoBotoesJogada(false);
        }
    }

    /**
     * Reinicia o placar e restaura o estado inicial para uma nova disputa melhor de três (Slide 7).
     */
    private void reiniciarPartida() {
        vitoriasJogador = 0;
        vitoriasDispositivo = 0;
        partidaEncerrada = false;

        binding.tvValorEscolhaJogador.setText("—");
        binding.tvValorEscolhaDispositivo.setText("—");
        binding.tvMensagemResultadoArena.setText(R.string.t4_waiting_move);
        binding.tvMensagemResultadoArena.setTextColor(getColor(R.color.text_warm_ivory));

        atualizarPlacarVisual();
        definirEstadoBotoesJogada(true);
    }

    /**
     * Habilita ou desabilita os três ImageButtons de jogada.
     */
    private void definirEstadoBotoesJogada(boolean habilitado) {
        binding.btnJogadaPedra.setEnabled(habilitado);
        binding.btnJogadaPapel.setEnabled(habilitado);
        binding.btnJogadaTesoura.setEnabled(habilitado);
        float opacidade = habilitado ? 1.0f : 0.45f;
        binding.btnJogadaPedra.setAlpha(opacidade);
        binding.btnJogadaPapel.setAlpha(opacidade);
        binding.btnJogadaTesoura.setAlpha(opacidade);
    }

    /**
     * Atualiza o TextView do placar com o nome do jogador e a pontuação atual.
     */
    private void atualizarPlacarVisual() {
        String placar = getString(R.string.t4_score_format, apelidoJogador, vitoriasJogador, vitoriasDispositivo);
        binding.tvPlacarArena.setText(placar);
    }

    /**
     * Converte o código numérico da jogada para o texto correspondente do strings.xml.
     */
    private String obterNomeJogada(int codigoJogada) {
        if (codigoJogada == JOGADA_PEDRA) return getString(R.string.t4_rock);
        if (codigoJogada == JOGADA_PAPEL) return getString(R.string.t4_paper);
        return getString(R.string.t4_scissors);
    }

    /**
     * Reproduz uma instância existente de MediaPlayer reposicionando o áudio no início (seekTo(0)).
     */
    private void reproduzirSomSeguro(MediaPlayer player) {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            }
            player.seekTo(0);
            player.start();
        }
    }

    /**
     * Retorna diretamente à Tela 1 informando que "Arena" foi o último módulo usado (Slide 12).
     */
    private void encerrarSessaoParaTela1() {
        Intent intent = new Intent(ArenaActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LAST_MODULE, getString(R.string.t4_title));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ================= CICLO DE VIDA E GESTÃO DO MEDIAPLAYER (Slides 8 e 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 4 (Arena) em primeiro plano.");
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 8 - Regra 02 Pausar):
     * "Ao sair da tela, pause o áudio; não use stop()."
     * No método onStop(), verificamos se algum MediaPlayer está tocando e chamamos pause().
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 4 (Arena) oculta. Pausando MediaPlayers ativos com pause().");

        if (playerVitoria != null && playerVitoria.isPlaying()) {
            playerVitoria.pause();
        }
        if (playerDerrota != null && playerDerrota.isPlaying()) {
            playerDerrota.pause();
        }
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 8 - Regra 03 Liberar):
     * "Em onDestroy(), chame release(): libera recursos e leva o player ao estado End."
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 4 (Arena) destruída. Liberando MediaPlayers com release().");

        if (playerVitoria != null) {
            playerVitoria.release();
            playerVitoria = null;
        }
        if (playerDerrota != null) {
            playerDerrota.release();
            playerDerrota = null;
        }
    }
}