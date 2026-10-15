package br.edu.ads.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.edu.ads.pocketarcade.databinding.ActivitySecretCodeBinding;

/**
 * TELA 5: COFRE DO CÓDIGO SECRETO (SecretCodeActivity)
 * Cumpre rigorosamente os requisitos dos Slides 9, 10, 12 e 13:
 * 1. Gera um código aleatório de 4 dígitos sem repetição.
 * 2. Limita o jogador a no máximo 8 tentativas.
 * 3. Valida campo não vazio, exatamente 4 números e ausência de dígitos repetidos.
 * 4. Registra o histórico textual colorido por dígito (Verde, Amarelo e Cinza - lógica TERMO).
 * 5. Calcula o tempo decorrido usando System.currentTimeMillis() no início e no fim,
 *    sem utilizar Handler, Thread ou Runnable.
 */
public class SecretCodeActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida_Tela5";
    private static final int MAX_TENTATIVAS = 8;

    private ActivitySecretCodeBinding binding;
    private String apelidoJogador;

    private String codigoSecreto = "";
    private int tentativasRealizadas = 0;
    private long tempoInicioMs = 0L;
    private boolean jogoAtivo = true;

    // Construtor de texto rico para colorir cada dígito no histórico textual (Slide 9)
    private final SpannableStringBuilder historicoColorido = new SpannableStringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Tela 5 (Código Secreto) criada.");

        // Inicializa o View Binding (proibido findViewById - Slide 13)
        binding = ActivitySecretCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Valida o apelido recebido por Intent (Slide 12)
        validarExtrasNoDestino();

        // 2. Configura os botões da tela
        configurarBotoes();

        // 3. Inicia a primeira partida gerando o código secreto e marcando o tempo inicial
        iniciarNovaPartida();
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
     * Configura os listeners de Tentar, Nova Partida, Voltar à Central e Encerrar Sessão.
     */
    private void configurarBotoes() {
        binding.btnTentarCodigo.setOnClickListener(v -> processarTentativa());
        binding.btnNovaPartidaCofre.setOnClickListener(v -> iniciarNovaPartida());

        // Retorno à Central usando apenas finish() (Slides 3 e 12)
        binding.btnVoltarCentralCofre.setOnClickListener(v -> finish());

        // Encerrar sessão retornando à Tela 1 com CLEAR_TOP | SINGLE_TOP (Slide 12)
        binding.btnEncerrarSessaoCofre.setOnClickListener(v -> encerrarSessaoParaTela1());
    }

    /**
     * Gera um novo código de 4 dígitos sem repetição, zera as tentativas e captura o
     * instante inicial com System.currentTimeMillis() (Slides 9 e 10).
     */
    private void iniciarNovaPartida() {
        codigoSecreto = gerarCodigoQuatroDigitosUnicos();
        tentativasRealizadas = 0;
        jogoAtivo = true;
        historicoColorido.clear();

        // POR QUE ISSO FOI FEITO (Slide 10 - Tempo sem atualização contínua):
        // "Use System.currentTimeMillis() no início e no fim. Calcule a diferença,
        // sem Handler, Thread ou Runnable."
        tempoInicioMs = System.currentTimeMillis();

        // Restaura o estado visual dos componentes
        binding.etPalpiteCodigo.setText("");
        binding.etPalpiteCodigo.setError(null);
        binding.etPalpiteCodigo.setEnabled(true);
        binding.btnTentarCodigo.setEnabled(true);
        binding.tvConclusaoCofre.clearAnimation();
        binding.tvConclusaoCofre.setVisibility(View.GONE);
        binding.tvHistoricoTentativas.setText(R.string.t5_history_empty);

        atualizarStatusTentativas();
    }

    /**
     * Embaralha uma lista com os dígitos de 0 a 9 e seleciona os 4 primeiros.
     * Isso garante matematicamente 4 dígitos aleatórios sem qualquer repetição (Slide 9).
     */
    private String gerarCodigoQuatroDigitosUnicos() {
        List<Integer> digitos = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            digitos.add(i);
        }
        Collections.shuffle(digitos);

        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(digitos.get(i));
        }
        Log.d(TAG, "Novo código secreto gerado (para testes/demonstração): " + sb);
        return sb.toString();
    }

    /**
     * Valida a tentativa do jogador e, se for válida, avalia as pistas por dígito (Slides 9 e 10).
     */
    private void processarTentativa() {
        if (!jogoAtivo) {
            return;
        }

        String palpite = binding.etPalpiteCodigo.getText().toString().trim();

        // VALIDAÇÃO 1 (Slide 9): Campo não vazio
        if (palpite.isEmpty()) {
            binding.etPalpiteCodigo.setError(getString(R.string.t5_err_empty));
            binding.etPalpiteCodigo.requestFocus();
            Toast.makeText(this, R.string.t5_err_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDAÇÃO 2 (Slide 9): Exatamente quatro números
        if (!palpite.matches("\\d{4}")) {
            binding.etPalpiteCodigo.setError(getString(R.string.t5_err_length));
            binding.etPalpiteCodigo.requestFocus();
            Toast.makeText(this, R.string.t5_err_length, Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDAÇÃO 3 (Slide 9): Ausência de dígitos repetidos
        if (possuiDigitosRepetidos(palpite)) {
            binding.etPalpiteCodigo.setError(getString(R.string.t5_err_repeated));
            binding.etPalpiteCodigo.requestFocus();
            Toast.makeText(this, R.string.t5_err_repeated, Toast.LENGTH_SHORT).show();
            return;
        }

        // Entrada válida: limpa erros e contabiliza a tentativa
        binding.etPalpiteCodigo.setError(null);
        tentativasRealizadas++;
        atualizarStatusTentativas();

        // Registra a tentativa no histórico textual com as cores do TERMO (Slides 9 e 10)
        adicionarTentativaAoHistorico(tentativasRealizadas, palpite);

        // Limpa o campo para facilitar a próxima tentativa
        binding.etPalpiteCodigo.setText("");

        // Verifica condição de vitória (acertou os 4 dígitos) ou derrota (atingiu 8 tentativas)
        if (palpite.equals(codigoSecreto)) {
            concluirPartida(true);
        } else if (tentativasRealizadas >= MAX_TENTATIVAS) {
            concluirPartida(false);
        }
    }

    /**
     * Verifica se há números repetidos na tentativa usando um conjunto (Set).
     */
    private boolean possuiDigitosRepetidos(String texto) {
        Set<Character> caracteresUnicos = new HashSet<>();
        for (int i = 0; i < texto.length(); i++) {
            if (!caracteresUnicos.add(texto.charAt(i))) {
                return true; // Já existia no Set, logo é repetido
            }
        }
        return false;
    }

    /**
     * POR QUE ISSO FOI FEITO (Slides 9 e 10):
     * Avalia cada dígito da tentativa segundo a regra do jogo TERMO:
     * - Verde (R.color.termo_green): dígito certo na posição certa.
     * - Amarelo (R.color.termo_yellow): dígito existe no código, mas na posição errada.
     * - Cinza (R.color.termo_gray): dígito não existe no código.
     * Exemplo do Slide 10: Código 4271, tentativa 4219 -> 4 e 2 verdes, 1 amarelo, 9 cinza.
     */
    private void adicionarTentativaAoHistorico(int numeroTentativa, String palpite) {
        if (historicoColorido.length() > 0) {
            historicoColorido.append("\n\n");
        }

        historicoColorido.append("#").append(String.valueOf(numeroTentativa)).append("   ");

        StringBuilder resumoTextual = new StringBuilder("   (");

        for (int i = 0; i < 4; i++) {
            char digitoTentado = palpite.charAt(i);
            int corFundo;
            String descricaoPista;

            if (codigoSecreto.charAt(i) == digitoTentado) {
                // Posição certa -> Verde
                corFundo = getColor(R.color.termo_green);
                descricaoPista = "Verde";
            } else if (codigoSecreto.indexOf(digitoTentado) != -1) {
                // Existe no código, mas em outra posição -> Amarelo
                corFundo = getColor(R.color.termo_yellow);
                descricaoPista = "Amarelo";
            } else {
                // Não existe no código -> Cinza
                corFundo = getColor(R.color.termo_gray);
                descricaoPista = "Cinza";
            }

            // Cria o bloco colorido para o dígito atual usando Spannable
            int inicioSpan = historicoColorido.length();
            historicoColorido.append(" ").append(digitoTentado).append(" ");
            int fimSpan = historicoColorido.length();

            historicoColorido.setSpan(
                    new BackgroundColorSpan(corFundo),
                    inicioSpan,
                    fimSpan,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            historicoColorido.setSpan(
                    new ForegroundColorSpan(getColor(R.color.text_warm_ivory)),
                    inicioSpan,
                    fimSpan,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            historicoColorido.append(" ");

            resumoTextual.append(digitoTentado).append(":").append(descricaoPista);
            if (i < 3) {
                resumoTextual.append(", ");
            }
        }

        resumoTextual.append(")");
        historicoColorido.append(resumoTextual.toString());

        binding.tvHistoricoTentativas.setText(historicoColorido);
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 10):
     * Calcula o tempo decorrido subtraindo o System.currentTimeMillis() inicial do final,
     * bloqueia novas tentativas e anima o painel de sucesso ou fracasso na mesma tela.
     */
    private void concluirPartida(boolean venceu) {
        jogoAtivo = false;
        binding.etPalpiteCodigo.setEnabled(false);
        binding.btnTentarCodigo.setEnabled(false);

        long tempoFimMs = System.currentTimeMillis();
        long segundosDecorridos = Math.max(1L, (tempoFimMs - tempoInicioMs) / 1000L);

        String mensagemFinal;
        if (venceu) {
            mensagemFinal = getString(R.string.t5_win_msg, codigoSecreto, tentativasRealizadas, segundosDecorridos);
            binding.tvConclusaoCofre.setTextColor(getColor(R.color.accent_jade_teal));
        } else {
            mensagemFinal = getString(R.string.t5_loss_msg, codigoSecreto, segundosDecorridos);
            binding.tvConclusaoCofre.setTextColor(getColor(R.color.accent_terracotta));
        }

        binding.tvConclusaoCofre.setText(mensagemFinal);
        binding.tvConclusaoCofre.setVisibility(View.VISIBLE);

        // Anima o resultado de sucesso ou fracasso com recurso XML (Slide 10)
        Animation animacaoConclusao = AnimationUtils.loadAnimation(this, R.anim.anim_feedback_resultado);
        binding.tvConclusaoCofre.startAnimation(animacaoConclusao);
    }

    /**
     * Atualiza o contador visual de tentativas (ex: "Tentativas utilizadas: 3 / 8").
     */
    private void atualizarStatusTentativas() {
        binding.tvContadorTentativas.setText(getString(R.string.t5_attempts_status, tentativasRealizadas));
    }

    /**
     * Retorna à Tela 1 com as flags CLEAR_TOP | SINGLE_TOP informando o último módulo (Slide 12).
     */
    private void encerrarSessaoParaTela1() {
        Intent intent = new Intent(SecretCodeActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LAST_MODULE, getString(R.string.t5_title));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ================= REGISTROS DE CICLO DE VIDA OBRIGATÓRIOS (Slide 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 5 (Código Secreto) em primeiro plano.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 5 (Código Secreto) oculta.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 5 (Código Secreto) destruída.");
    }
}