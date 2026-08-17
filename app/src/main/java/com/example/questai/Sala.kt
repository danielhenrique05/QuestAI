package com.example.questai

data class Sala (
    val id: String = "",
    val nome: String = "",
    val codigo: String = "",
    val mestreId: String = "",
    val mestreNome: String = "",
    val participantes: Map<String, String> = emptyMap(),
    val criadaEm: Long = 0L
)