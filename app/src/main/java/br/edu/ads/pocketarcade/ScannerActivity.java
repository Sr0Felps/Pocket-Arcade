package br.edu.ads.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Normalizer;
import java.util.Locale;

import br.edu.ads.pocketarcade.databinding.ActivityScannerBinding;

/**
 * TELA 3: SCANNER DE MENSAGENS (ScannerActivity)
 * Cumpre os requisitos dos Slides 6, 12 e 13:
 * 1. Valida entrada exigindo ao menos 3 caracteres válidos.
 * 2. Calcula as 5 medidas: caracteres sem espaços, palavras, vogais, texto invertido e palíndromo.
 * 3. Detecta palíndromos ignorando espaços e diferenças entre maiúsculas e minúsculas.
 * 4. Anima a exibição do painel de resultados com recurso XML.
 * 5. Gerencia corretamente a pilha de Activities (finish() e flags CLEAR_TOP | SINGLE_TOP).
 */
public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida_Tela3";

    private ActivityScannerBinding binding;
    private String apelidoJogador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Tela 3 (Scanner de Mensagens) criada.");

        // Inicializa o View Binding para substituir o proibido findViewById() (Slide 13)
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Valida os Extras recebidos da CentralActivity (Slide 12)
        validarExtrasNoDestino();

        // Configura os botões de ação e navegação via código Java
        configurarBotoes();
    }

    /**
     * POR QUE ISSO FOI FEITO:
     * O Slide 12 exige validar os extras de Intent no destino para garantir integridade do estado.
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
     * Configura os listeners dos botões Analisar, Limpar, Voltar à Central e Encerrar Sessão.
     */
    private void configurarBotoes() {
        // Botão ANALISAR (Slide 6)
        binding.btnAnalisarMensagem.setOnClickListener(v -> analisarTexto());

        // Botão LIMPAR (Slide 6): limpa o campo, remove erros e oculta o painel de resultados
        binding.btnLimparMensagem.setOnClickListener(v -> {
            binding.etMensagemScanner.setText("");
            binding.etMensagemScanner.setError(null);
            binding.panelResultadoScanner.clearAnimation();
            binding.panelResultadoScanner.setVisibility(View.GONE);
            binding.etMensagemScanner.requestFocus();
        });

        // Botão VOLTAR À CENTRAL (Slide 3 e 12):
        // Usa exclusivamente finish() para fechar a tela atual e revelar a Central já existente na pilha.
        binding.btnVoltarCentralScanner.setOnClickListener(v -> finish());

        // Botão ENCERRAR SESSÃO (Slide 12):
        // Retorna direto para a Tela 1 limpando o topo da pilha sem duplicar a MainActivity.
        binding.btnEncerrarSessaoScanner.setOnClickListener(v -> encerrarSessaoParaTela1());
    }

    /**
     * Executa a validação de mínimo de 3 caracteres e processa as 5 medidas do texto (Slide 6).
     */
    private void analisarTexto() {
        String textoOriginal = binding.etMensagemScanner.getText().toString().trim();

        // Remove todos os espaços em branco para medir o conteúdo real
        String textoSemEspacos = textoOriginal.replaceAll("\\s+", "");

        // VALIDAÇÃO OBRIGATÓRIA (Slide 6): pelo menos 3 caracteres válidos
        if (textoOriginal.length() < 3 || textoSemEspacos.length() < 3) {
            binding.etMensagemScanner.setError(getString(R.string.t3_err_short_text));
            binding.etMensagemScanner.requestFocus();
            Toast.makeText(this, R.string.t3_err_short_text, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.etMensagemScanner.setError(null);

        // 1. Medida: Caracteres sem espaços
        int qtdCaracteresSemEspaco = textoSemEspacos.length();

        // 2. Medida: Quantidade de palavras (divide por um ou mais espaços consecutivos)
        String[] palavras = textoOriginal.split("\\s+");
        int qtdPalavras = palavras.length;

        // 3. Medida: Quantidade de vogais (normaliza acentos para contar 'á', 'ã', 'ê', etc. como vogais)
        int qtdVogais = contarVogais(textoOriginal);

        // 4. Medida: Texto invertido (mantendo a estrutura original invertida caractere a caractere)
        String textoInvertido = new StringBuilder(textoOriginal).reverse().toString();

        // 5. Medida: Verificação de Palíndromo (Slide 6):
        // Ignora espaços e diferenças entre maiúsculas/minúsculas (ex: "Ana" e "A sacada da casa").
        boolean ehPalindromo = verificarPalindromo(textoSemEspacos);

        // Preenche os resultados na interface via View Binding
        binding.tvResCharsSemEspaco.setText(String.valueOf(qtdCaracteresSemEspaco));
        binding.tvResPalavras.setText(String.valueOf(qtdPalavras));
        binding.tvResVogais.setText(String.valueOf(qtdVogais));
        binding.tvResTextoInvertido.setText(textoInvertido);
        binding.tvResPalindromo.setText(ehPalindromo ? R.string.t3_yes : R.string.t3_no);

        // Destaca visualmente a resposta do palíndromo com a paleta do app
        binding.tvResPalindromo.setTextColor(getColor(
                ehPalindromo ? R.color.accent_jade_teal : R.color.accent_terracotta
        ));

        // ANIMAÇÃO NO PAINEL DE RESULTADO (Slide 6):
        binding.panelResultadoScanner.setVisibility(View.VISIBLE);
        Animation animacaoResultado = AnimationUtils.loadAnimation(this, R.anim.anim_feedback_resultado);
        binding.panelResultadoScanner.startAnimation(animacaoResultado);
    }

    /**
     * Conta todas as vogais do texto. Remove acentuação gráfica temporariamente usando Normalizer
     * para que vogais acentuadas em português (como 'á', 'é', 'õ') sejam contabilizadas corretamente.
     */
    private int contarVogais(String texto) {
        String textoSemAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);

        int contador = 0;
        for (int i = 0; i < textoSemAcentos.length(); i++) {
            char c = textoSemAcentos.charAt(i);
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                contador++;
            }
        }
        return contador;
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 6):
     * Para detectar palíndromos, o slide determina ignorar espaços e diferenças entre
     * maiúsculas e minúsculas. Também normalizamos acentos para máxima precisão.
     */
    private boolean verificarPalindromo(String textoSemEspacos) {
        String textoNormalizado = Normalizer.normalize(textoSemEspacos, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);

        String invertidoNormalizado = new StringBuilder(textoNormalizado).reverse().toString();
        return textoNormalizado.equals(invertidoNormalizado);
    }

    /**
     * POR QUE ISSO FOI FEITO (Slide 12):
     * Retorna à Tela 1 usando as flags FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP.
     * Isso remove a Central e o Scanner da pilha e entrega o nome deste módulo no onNewIntent() da Tela 1.
     */
    private void encerrarSessaoParaTela1() {
        Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LAST_MODULE, getString(R.string.t3_title));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ================= REGISTROS DE CICLO DE VIDA OBRIGATÓRIOS (Slide 12) =================

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Tela 3 (Scanner) visível e ativa.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Tela 3 (Scanner) oculta.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Tela 3 (Scanner) destruída.");
    }
}