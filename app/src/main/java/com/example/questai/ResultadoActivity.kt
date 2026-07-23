package com.example.questai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

class ResultadoActivity: AppCompatActivity() {
    private lateinit var grupoCarregando: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var grupoResultado: LinearLayout
    private lateinit var tvQuest: TextView

    private lateinit var grupoErro: LinearLayout
    private lateinit var tvMensagemErro: TextView

    private lateinit var btnCopiar: Button
    private lateinit var btnNovaQuest: Button

    private var textoQuestGerada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        grupoCarregando = findViewById(R.id.grupoCarregando)
        progressBar = findViewById(R.id.progressBar)
        grupoResultado = findViewById(R.id.grupoResultado)
        tvQuest = findViewById(R.id.tvQuest)
        grupoErro = findViewById(R.id.grupoErro)
        tvMensagemErro = findViewById(R.id.tvMensagemErro)
        btnCopiar = findViewById(R.id.btnCopiar)
        btnNovaQuest = findViewById(R.id.btnNovaQuest)


        val modo = intent.getStringExtra("modo")?: "aleatorio"

        val prompt = if (modo == "aleatorio"){
            val ambientacao = intent.getStringExtra("ambientacao") ?: ""
            val dificuldade = intent.getStringExtra("dificuldade") ?: ""
            montarPromptAleatorio(ambientacao, dificuldade)
        } else{
            val contexto = intent.getStringExtra("contexto") ?: ""
            val elementos = intent.getStringExtra("elementos") ?: ""
            montarPromptPersonalizado(contexto, elementos)
        }

        gerarQuest(prompt)

        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Quest", textoQuestGerada)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this,"Quest copiada!", Toast.LENGTH_SHORT).show()

        }

        btnNovaQuest.setOnClickListener {
            val intentNovo = Intent(this, MainActivity:: class.java)
            intentNovo.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intentNovo)
            finish()
        }


    }

    private fun montarPromptAleatorio(ambientacao:String, dificuldade: String ): String{
        return """
    Você é um mestre de RPG altamente criativo e inovador.
    
    O usuário definiu os seguintes parâmetros para esta quest:
    - Ambientação / Estilo: $ambientacao
    - Dificuldade desejada: $dificuldade
    
    Contexto base: Uma cidade mística oculta através de um véu sobre um mar cósmico, habitada por astrônomos, estudiosos e criaturas de horror cósmico.
    
    INSTRUÇÕES OBRIGATÓRIAS:
    1. Crie uma história TOTALMENTE focada na ambientação "$ambientacao" e na dificuldade "$dificuldade" fornecidas.
    2. NÃO gere respostas padronizadas nem repita histórias anteriores.
    3. Adapte os desafios, monstros e NPCs para combinar estritamente com esses parâmetros.
    
    Estruture sua resposta EXATAMENTE nestas seções, cada uma com um título em negrito:
    Título:
    Gancho narrativo:
    Objetivo principal:
    Complicação:
    NPCs envolvidos:
    Recompensa sugerida:
    Dificuldade estimada:
    """.trimIndent()
    }

    private fun montarPromptPersonalizado(contexto:String, elementos:String): String{
        return """
             Você é um mestre de RPG experiente e criativo.
        
        CONTEXTO DA CAMPANHA :
        $contexto
        
        ELEMENTOS OBRIGATÓRIOS (devem estar presentes na quest):
        $elementos
        
        Com base EXATAMENTE nesse contexto e INCLUINDO OBRIGATORIAMENTE todos esses elementos,
        crie uma quest que faz sentido DENTRO desse mundo e dessa história.
        
        NÃO ignore o contexto. NÃO crie uma aventura genérica.
        A quest DEVE usar os elementos fornecidos e estar ALINHADA com o contexto descrito.
        
        Estruture sua resposta EXATAMENTE nestas seções, cada uma com um título em negrito:
        Título:
        Gancho narrativo:
        Objetivo principal:
        Complicação:
        NPCs envolvidos:
        Recompensa sugerida:
        Dificuldade estimada:
        """.trimIndent()
    }

    private fun gerarQuest(prompt:String){
        mostrarCarregando()

        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.1-flash-lite")

        lifecycleScope.launch{
            try {
                val response = model.generateContent(prompt)
                val texto = response.text

                if(texto.isNullOrBlank()){
                    mostrarErro("A IA não conseguiu gerar uma quest.Tente Novamente")
                }else{
                    textoQuestGerada = texto
                    mostrarResultado(texto)
                }
            } catch (e: Exception){
                mostrarErro("Erro: ${e.message}")
            }
        }
    }

    private fun mostrarCarregando() {
        grupoCarregando.visibility = View.VISIBLE
        grupoResultado.visibility = View.GONE
        grupoErro.visibility = View.GONE
    }

    private fun mostrarResultado(texto: String) {
        grupoCarregando.visibility = View.GONE
        grupoResultado.visibility = View.VISIBLE
        grupoErro.visibility = View.GONE
        tvQuest.text = texto
    }

    private fun mostrarErro(mensagem: String) {
        grupoCarregando.visibility = View.GONE
        grupoResultado.visibility = View.GONE
        grupoErro.visibility = View.VISIBLE
        tvMensagemErro.text = mensagem
    }

}