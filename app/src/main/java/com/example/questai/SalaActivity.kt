package com.example.questai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SalaActivity : AppCompatActivity(){

    private lateinit var tvNomeSala: TextView
    private lateinit var tvCodigoSala: TextView
    private lateinit var tvMestreNome: TextView
    private lateinit var tvListaParticipantes: TextView

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var listenerRegistration: ListenerRegistration? = null
    private var salaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sala)

        tvNomeSala = findViewById(R.id.tvNomeSala)
        tvCodigoSala = findViewById(R.id.tvCodigoSala)
        tvMestreNome = findViewById(R.id.tvMestreNome)
        tvListaParticipantes = findViewById(R.id.tvListaParticipantes)

        salaId =  intent.getStringExtra("salaId") ?: ""

        if(salaId.isEmpty()){
            tvNomeSala.text = "sala invalida"
            return
        }

        listenerRegistration = db.collection("salas").document("salaId")
            .addSnapshotListener { snapshot, erro ->
                if (erro != null || snapshot == null || !snapshot.exists()){
                    tvNomeSala.text = "Erro ao carregar Sala"
                    return@addSnapshotListener
                }
                val sala = snapshot.toObject(Sala::class.java) ?: return@addSnapshotListener
                exibirSala(sala)

            }

    }

    private fun exibirSala(sala: Sala){
        tvNomeSala.text = sala.nome
        tvCodigoSala.text = sala.codigo
        tvMestreNome.text = sala.mestreNome

        tvListaParticipantes.text = if(sala.participantes.isEmpty()){
            "nenhum jogador ainda"
        } else{
            sala.participantes.values.joinToString ("\n"){ nome -> "$nome" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

}