package com.example.questai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {
    private lateinit var rgTipo: RadioGroup
    private lateinit var etNome: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSenha: EditText
    private lateinit var btnCadastrar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErro: TextView
    private lateinit var tvIrParaLogin: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        rgTipo = findViewById(R.id.rgTipo)
        etNome = findViewById(R.id.etNome)
        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        progressBar = findViewById(R.id.progressBarCadastro)
        tvErro = findViewById(R.id.tvErroCadastro)
        tvIrParaLogin = findViewById(R.id.tvIrParaLogin)

        btnCadastrar.setOnClickListener { tentarCadastrar() }
        tvIrParaLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    private  fun tentarCadastrar(){
        tvErro.visibility = View.GONE

        val nome = etNome.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val senha = etSenha.text.toString()
        val tipo = if (rgTipo.checkedRadioButtonId == R.id.rbMestre) "mestre" else "jogador"

        if (nome.isEmpty()) {
            mostrarErro("Digite seu nome.")
            return
        }
        if (email.isEmpty()) {
            mostrarErro("Digite seu e-mail.")
            return
        }
        if (senha.length < 6) {
            mostrarErro("A senha precisa ter pelo menos 6 caracteres.")
            return
        }

        mostrarCarregando(true)

        auth.createUserWithEmailAndPassword(email,senha)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid
                if(uid == null){
                    mostrarCarregando(false)
                    mostrarErro("Nao foi possivel criar usuario")
                    return@addOnSuccessListener
                }
                salvarPerfil(uid,nome,email,tipo)
            }
            .addOnFailureListener { e ->
                mostrarCarregando(false)
                mostrarErro(traduzirErro(e.message))
            }
    }

    private fun salvarPerfil(uid: String, nome: String, email: String, tipo: String){
        val usuario = Usuario(uid = uid, nome = nome, email = email, tipo = tipo)

        db.collection("usuarios").document(uid).set(usuario)
            .addOnSuccessListener {
                mostrarCarregando(false)
                Toast.makeText(
                    this,
                    "Conta criada com sucesso! Bem-vindo(a), $nome!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                mostrarCarregando(false)
                mostrarErro("Conta criada, mas houve erro ao salvar o perfil: ${e.message}")
            }

    }

    private fun mostrarCarregando(carregando: Boolean) {
        progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        btnCadastrar.isEnabled = !carregando
    }

    private fun mostrarErro(mensagem: String) {
        tvErro.text = mensagem
        tvErro.visibility = View.VISIBLE
    }

    private fun traduzirErro(mensagem: String?): String {
        return when {
            mensagem == null -> "Erro ao criar conta. Tente novamente."
            mensagem.contains("email address is already in use", ignoreCase = true) ->
                "Este e-mail já está cadastrado."
            mensagem.contains("badly formatted", ignoreCase = true) -> "E-mail inválido."
            mensagem.contains("network", ignoreCase = true) -> "Sem conexão com a internet."
            mensagem.contains("password", ignoreCase = true) -> "Senha muito fraca (mín. 6 caracteres)."
            else -> "Erro ao criar conta: $mensagem"
        }
    }

}