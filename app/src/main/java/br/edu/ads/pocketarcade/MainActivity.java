package br.edu.ads.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.edu.ads.pocketarcade.databinding.ActivityMainBinding;

/**
 * TELA 1: APRESENTAÇÃO (MainActivity)
 * Cumpre os requisitos dos Slides 4, 12 e 13:
 * 1. Exibe os dados dos dois integrantes da dupla, curso e logotipo.
 * 2. Valida o apelido garantindo no mínimo 3 caracteres.
 * 3. Anima o botão com ViewPropertyAnimator sem utilizar Runnable (usando AnimatorListenerAdapter).
 * 4. Envia o apelido para a CentralActivity via Intent explícita usando constante de chave.
 * 5. Trata o retorno de "Encerrar Sessão" no método onNewIntent() sem duplicar instâncias.
 */
public class MainActivity extends AppCompatActivity {

    // Constante de TAG para demonstrar o ciclo de vida no Logcat durante a apresentação (Slide 12 e 15)
    private static final String TAG = "CicloDeVida_Tela1";

    // POR QUE ISSO FOI FEITO:
    // O Slide 12 exige o uso de constantes para chaves de Extras de Intent.
    // Isso evita erros de digitação entre a Activity que envia e a Activity que recebe os dados.
    public static final String EXTRA_NICKNAME = "br.edu.ads.pocketarcade.EXTRA_NICKNAME";
    public static final String EXTRA_LAST_MODULE = "br.edu.ads.pocketarcade.EXTRA_LAST_MODULE";

    // Objeto de View Binding gerado automaticamente a partir do arquivo activity_main.xml
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registro obrigatório no Logcat para comprovar a execução do onCreate (Slide 12)
        Log.d(TAG, "onCreate: Tela 1 (Apresentação) inicializada.");

        // POR QUE ISSO FOI FEITO:
        // Inflamos o layout usando View Binding em vez de findViewById() (proibido no Slide 13).
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura o evento de clique do botão via código Java (proibido android:onClick no XML)
        configurarBotaoEntrada();

        // Verifica se a Intent inicial trouxe informação de encerramento de sessão
        processarRetornoDeSessao(getIntent());
    }

    /**
     * Valida o apelido inserido e executa a microanimação no botão antes de navegar para a Tela 2.
     */
    private void configurarBotaoEntrada() {
        binding.btnEntrarArcade.setOnClickListener(v -> {
            // Captura o texto digitado e remove espaços excedentes no início e no fim (.trim())
            String apelido = binding.etNickname.getText().toString().trim();

            // POR QUE ISSO FOI FEITO:
            // O Slide 4 exige validar um apelido com ao menos 3 caracteres antes de prosseguir.
            if (apelido.length() < 3) {
                binding.etNickname.setError(getString(R.string.t1_err_invalid_nick));
                binding.etNickname.requestFocus();
                Toast.makeText(this, R.string.t1_err_invalid_nick, Toast.LENGTH_SHORT).show();
                return;
            }

            // Limpa mensagens de erro e bloqueia múltiplos cliques durante a animação
            binding.etNickname.setError(null);
            binding.btnEntrarArcade.setEnabled(false);

            // POR QUE ISSO FOI FEITO:
            // O Slide 4 exige animar o botão com ViewPropertyAnimator (.animate()).
            // ATENÇÃO TÉCNICA: Não usamos .withEndAction() porque ele recebe um Runnable,
            // e o Slide 13 proíbe expressamente o uso de Runnable!
            // Usando AnimatorListenerAdapter, ouvimos o fim da animação sem usar Runnable.
            binding.btnEntrarArcade.animate()
                    .scaleX(0.93f)
                    .scaleY(0.93f)
                    .alpha(0.8f)
                    .setDuration(140)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Retorna o botão ao tamanho original e abre a Central de Desafios
                            binding.btnEntrarArcade.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .setDuration(140)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator anim) {
                                            binding.btnEntrarArcade.setEnabled(true);
                                            navegarParaCentral(apelido);
                                        }
                                    });
                        }
                    });
        });
    }

    /**
     * Cria e dispara uma Intent explícita enviando o apelido validado para a Tela 2 (CentralActivity).
     */
    private void navegarParaCentral(String apelido) {
        Intent intentExplicita = new Intent(MainActivity.this, CentralActivity.class);
        intentExplicita.putExtra(EXTRA_NICKNAME, apelido);
        startActivity(intentExplicita);
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 12):
     * Quando o jogador clica em "Encerrar sessão" nas Telas 3, 4, 5 ou 6, a Intent é disparada
     * com as flags FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP.
     * Como a MainActivity já está na base da pilha de Activities, ela não é recriada:
     * o Android limpa as telas acima dela e entrega a Intent diretamente neste método onNewIntent().
     */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: Retorno de sessão recebido na instância única da Tela 1.");

        // Atualiza a referência da Intent atual da Activity
        setIntent(intent);
        processarRetornoDeSessao(intent);
    }

    /**
     * Lê o Extra EXTRA_LAST_MODULE (caso exista) e mostra na interface qual foi o último desafio visitado.
     */
    private void processarRetornoDeSessao(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_LAST_MODULE)) {
            String ultimoModulo = intent.getStringExtra(EXTRA_LAST_MODULE);
            if (ultimoModulo != null && !ultimoModulo.trim().isEmpty()) {
                String textoFormatado = getString(R.string.t1_last_module_format, ultimoModulo);
                binding.tvUltimoModulo.setText(textoFormatado);
                binding.tvUltimoModulo.setVisibility(View.VISIBLE);
            }
        }
    }

    // ================= REGISTROS DE CICLO DE VIDA OBRIGATÓRIOS (Slide 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 1 visível e ativa para o jogador.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 1 saiu de foco (parcial ou totalmente oculta).");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 1 encerrada e removida da pilha.");
    }
}