package com.example.questai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail : EditText
    private lateinit var etSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErro: TextView
    private lateinit var tvIrParaCadastro: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        btnEntrar = findViewById(R.id.btnEntrar)
        progressBar = findViewById(R.id.progressBarLogin)
        tvErro = findViewById(R.id.tvErroLogin)
        tvIrParaCadastro = findViewById(R.id.tvIrParaCadastro)

        btnEntrar.setOnClickListener { tentarLogin() }

        tvIrParaCadastro.setOnClickListener {
            startActivity(Intent (this, CadastroActivity::class.java))
        }

    }

    private fun tentarLogin(){
        tvErro.visibility = View.GONE

        val email = etEmail.text.toString().trim();
        val senha = etSenha.text.toString()

        if (email.isEmpty() || senha.isEmpty()){
            mostrarErro("Preencha email e senha")
            return
        }

        mostrarCarregando(true)

        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid
                if (uid == null){
                    mostrarCarregando(false)
                    mostrarErro("Não foi possivel identificar o usuario")
                    return@addOnSuccessListener
                }
                buscarTipoUsuario(uid)
            }
            .addOnFailureListener { e ->
                mostrarCarregando(false)
                mostrarErro(traduzirErro(e.message))
            }


    }

    private fun buscarTipoUsuario(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                mostrarCarregando(false)

                val usuario = doc.toObject(Usuario::class.java)
                if (usuario == null) {
                    mostrarErro("Perfil nao encontrado. Tente se cadastrar novamente")
                    return@addOnSuccessListener
                }

                Toast.makeText(
                    this,
                    "BemVindo(a), ${usuario.nome}!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()

            }
            .addOnFailureListener { e ->
                mostrarCarregando(false)
                mostrarErro("Erro ao buscar perfil: ${e.message}")
            }
    }

    private fun mostrarCarregando(carregando: Boolean) {
        progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        btnEntrar.isEnabled = !carregando
    }

    private fun mostrarErro(mensagem: String) {
        tvErro.text = mensagem
        tvErro.visibility = View.VISIBLE
    }

    private fun traduzirErro(mensagem: String?): String {
        return when {
            mensagem == null -> "Erro ao entrar. Tente novamente."
            mensagem.contains("no user record", ignoreCase = true) -> "E-mail não cadastrado."
            mensagem.contains("password is invalid", ignoreCase = true) -> "Senha incorreta."
            mensagem.contains("badly formatted", ignoreCase = true) -> "E-mail inválido."
            mensagem.contains("network", ignoreCase = true) -> "Sem conexão com a internet."
            else -> "Erro ao entrar: $mensagem"
        }
    }
}