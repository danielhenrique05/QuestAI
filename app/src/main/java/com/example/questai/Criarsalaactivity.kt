package com.example.questai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class Criarsalaactivity : AppCompatActivity() {
    private lateinit var etNomeSala: TextView
    private lateinit var btnCriarSala: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var  tvError: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criarsala)

        etNomeSala = findViewById(R.id.etNomeSala)
        btnCriarSala = findViewById(R.id.btnCriarSala)
        progressBar = findViewById(R.id.progressBarCriarSala)
        tvError = findViewById(R.id.tvErroCriarSala)

       btnCriarSala.setOnClickListener{
           tentarCriarSala()
       }
    }

    private fun tentarCriarSala(){
        tvError.visibility = View.GONE

        val nomeSala = etNomeSala.text.toString().trim()

        if(nomeSala.isEmpty()){
            mostrarErro("Nao foi possivel criar sala")
            return
        }

        val uid = auth.currentUser?.uid
        if(uid == null){
            mostrarErro("Sessão expirada. Faça login novamente")
            return
        }

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                val mestreNome = usuario?.nome ?: "Mestre"
                //chama o metodo para criar sala com o id
            }

    }

    private fun criarSalaComCodigoUnico(mestreId: String, mestreNome:String, nomeSala: String, tentativa: Int = 0){
        if (tentativa >= 5){
            mostrarCarregando(false)
            mostrarErro("Não foi possivel gerar um codigo unico. Tente Novamente")
            return
        }

        val codigo = gerarCodigo()


        //percorre na coleção das salas e procura o codigo
        db.collection("salas").whereEqualTo("codigo", codigo).get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    criarSalaComCodigoUnico(mestreId, mestreNome, nomeSala, tentativa + 1)
                    return@addOnSuccessListener
                }

                val novaSalaRef = db.collection("salas").document()
                val sala = Sala(
                    id = novaSalaRef.id,
                    nome = nomeSala,
                    codigo = codigo,
                    mestreId = mestreId,
                    mestreNome = mestreNome,
                    participantes = emptyMap(),
                    criadaEm = System.currentTimeMillis()
                )

                novaSalaRef.set(sala)
                    .addOnSuccessListener {
                        mostrarCarregando(false)
                        abrirSala(sala.id)
                    }
                    .addOnFailureListener { e ->
                        mostrarCarregando(false)
                        mostrarErro("Erro ao criar Sala: ${e.message}")
                    }
            }.addOnFailureListener { e->
                mostrarCarregando(false)
                mostrarErro("Erro ao verificar codigo:  ${e.message}")
            }
    }


    private fun gerarCodigo(): String{
        val caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { caracteres.random() }.joinToString { "" }
    }

    private fun abrirSala(salaId: String){
        val intent = Intent(this, SalaActivity::class.java)
        intent.putExtra("salaId" salaId)
        startActivity(intent)
        finish()
    }

    private fun mostrarCarregando(carregando: Boolean) {
        progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        btnCriarSala.isEnabled = !carregando
    }

    private fun mostrarErro(mensagem: String) {
        tvError.text = mensagem
        tvError.visibility = View.VISIBLE
    }

}