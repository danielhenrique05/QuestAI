package com.example.questai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var rgModo: RadioGroup
    private lateinit var rbAleatoria: RadioButton
    private lateinit var rbPersonalizada: RadioButton

    private lateinit var grupoAleatorio: LinearLayout
    private lateinit var spAmbientacao: Spinner
    private lateinit var spDificuldade: Spinner

    private lateinit var grupoPersonalizado: LinearLayout
    private lateinit var etContexto: EditText
    private lateinit var etElementos: EditText

    private lateinit var btnGerar: Button
    private lateinit var tvErro: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        rgModo = findViewById(R.id.rgModo)
        rbAleatoria = findViewById(R.id.rbAleatoria)
        rbPersonalizada = findViewById(R.id.rbPersonalizada)

        grupoAleatorio = findViewById(R.id.grupoAleatorio)
        spAmbientacao = findViewById(R.id.spAmbientacao)
        spDificuldade = findViewById(R.id.spDificuldade)

        grupoPersonalizado = findViewById(R.id.grupoPersonalizado)
        etContexto = findViewById(R.id.etContexto)
        etElementos = findViewById(R.id.etElementos)

        btnGerar = findViewById(R.id.btnGerar)
        tvErro = findViewById(R.id.tvErro)


        //SPINNERS
        val ambientacoes = arrayOf("Fantasia Medieval", "Cyberpunk", "Terror", "Faroeste", "Sci-Fi")
        val adapterAmbientacao = ArrayAdapter(this, android.R.layout.simple_spinner_item, ambientacoes)
        adapterAmbientacao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAmbientacao.adapter = adapterAmbientacao

        val dificuldades = arrayOf("facil","media","dificil","pesadelo")
        val adapterDificuldade = ArrayAdapter(this, android.R.layout.simple_spinner_item, dificuldades)
        adapterDificuldade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDificuldade.adapter = adapterDificuldade


        rgModo.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbAleatoria){
                grupoAleatorio.visibility = View.VISIBLE
                grupoPersonalizado.visibility = View.GONE
            }else{
                grupoAleatorio.visibility = View.GONE
                grupoPersonalizado.visibility = View.VISIBLE
            }
            tvErro.visibility = View.GONE
        }


        btnGerar.setOnClickListener {
            val modoAleatorio = rbAleatoria.isChecked

            if(modoAleatorio){
                val ambientacao = spAmbientacao.selectedItem.toString()
                val dificuldade = spDificuldade.selectedItem.toString()

                val intent = Intent(this, ResultadoActivity::class.java)
                intent.putExtra("Modo","aleatorio")
                intent.putExtra("Ambientacao", ambientacao)
                intent.putExtra("Dificuldade",dificuldade)
                startActivity(intent)

            }else{
                val contexto = etContexto.text.toString().trim()
                val elementos = etElementos.text.toString().trim()

                if (contexto.isEmpty() || elementos.isEmpty()){
                    tvErro.text="Porfavor informe o contexto e os elementos da sua historia para forjar a quest!"
                    tvErro.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                tvErro.visibility = View.GONE

                val intent = Intent(this, ResultadoActivity::class.java)
                intent.putExtra("Modo", "Personalizado")
                intent.putExtra("Contexto", contexto)
                intent.putExtra("Elementos", elementos)
                startActivity(intent)


            }

        }
      

    }
}