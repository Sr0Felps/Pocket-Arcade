package br.edu.ads.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import br.edu.ads.pocketarcade.databinding.ActivityCentralBinding;

/**
 * TELA 2: CENTRAL DE DESAFIOS (CentralActivity)
 * Cumpre os requisitos dos Slides 3, 5, 12 e 13:
 * 1. Recebe e valida no destino o apelido vindo da Tela 1 por Intent.
 * 2. Exibe saudação personalizada e aplica animação XML (translate + fade) ao carregar.
 * 3. Navega para os 4 desafios usando Intents explícitas.
 * 4. Retorna à Tela 1 usando exclusivamente finish(), sem duplicar Activities na pilha.
 */
public class CentralActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida_Tela2";

    private ActivityCentralBinding binding;
    private String apelidoJogador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Tela 2 (Central de Desafios) criada.");

        // Inicializa o View Binding para acesso seguro às views do layout XML
        binding = ActivityCentralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Valida os Extras recebidos da Tela 1 (exigência do Slide 12)
        validarExtrasNoDestino();

        // 2. Executa a animação XML de entrada nos painéis (exigência do Slide 5)
        iniciarAnimacaoEntradaXml();

        // 3. Configura os cliques de navegação para as Telas 3, 4, 5 e 6
        configurarBotoesDosPainel();
    }

    /**
     * POR QUE ISSO FOI FEITO:
     * O Slide 12 exige validar os dados da Intent no destino.
     * Se por algum motivo o extra vier nulo ou menor que 3 letras, definimos um fallback seguro.
     */
    private void validarExtrasNoDestino() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_NICKNAME)) {
            apelidoJogador = intent.getStringExtra(MainActivity.EXTRA_NICKNAME);
        }

        if (apelidoJogador == null || apelidoJogador.trim().length() < 3) {
            apelidoJogador = getString(R.string.t2_default_player);
        }

        // Preenche a saudação no topo do menu rolável (Slide 5)
        binding.tvSaudacaoJogador.setText(getString(R.string.t2_greeting_format, apelidoJogador));
    }

    /**
     * Carrega o arquivo res/anim/anim_entrada_painel.xml e anima o LinearLayout interno.
     */
    private void iniciarAnimacaoEntradaXml() {
        Animation animacaoXml = AnimationUtils.loadAnimation(this, R.anim.anim_entrada_painel);
        binding.llContainerDesafios.startAnimation(animacaoXml);
    }

    /**
     * Configura os botões de cada painel usando Intents explícitas e repassando o apelido do jogador.
     */
    private void configurarBotoesDosPainel() {
        // Navega para a Tela 3: Scanner de Mensagens
        binding.btnAbrirScanner.setOnClickListener(v -> {
            Intent intent = new Intent(CentralActivity.this, ScannerActivity.class);
            intent.putExtra(MainActivity.EXTRA_NICKNAME, apelidoJogador);
            startActivity(intent);
        });

        // Navega para a Tela 4: Arena Pedra, Papel e Tesoura
        binding.btnAbrirArena.setOnClickListener(v -> {
            Intent intent = new Intent(CentralActivity.this, ArenaActivity.class);
            intent.putExtra(MainActivity.EXTRA_NICKNAME, apelidoJogador);
            startActivity(intent);
        });

        // Navega para a Tela 5: Cofre do Código Secreto
        binding.btnAbrirCodigoSecreto.setOnClickListener(v -> {
            Intent intent = new Intent(CentralActivity.this, SecretCodeActivity.class);
            intent.putExtra(MainActivity.EXTRA_NICKNAME, apelidoJogador);
            startActivity(intent);
        });

        // Navega para a Tela 6: Randomizador de Palavras
        binding.btnAbrirRandomizador.setOnClickListener(v -> {
            Intent intent = new Intent(CentralActivity.this, RandomizerActivity.class);
            intent.putExtra(MainActivity.EXTRA_NICKNAME, apelidoJogador);
            startActivity(intent);
        });

        // POR QUE ISSO FOI FEITO (Slide 3 e Slide 12):
        // O método finish() encerra apenas a CentralActivity e revela a MainActivity que
        // já estava aguardando na pilha. Nunca se deve usar startActivity() para simular o retorno!
        binding.btnVoltarApresentacao.setOnClickListener(v -> finish());
    }

    // ================= REGISTROS DE CICLO DE VIDA OBRIGATÓRIOS (Slide 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 2 (Central de Desafios) visível em primeiro plano.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 2 (Central de Desafios) oculta.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 2 (Central de Desafios) destruída.");
    }
}