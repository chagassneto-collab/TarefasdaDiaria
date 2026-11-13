package com.example.tarefasdadiaria

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun tituloVazioEhInvalido() {
        assertFalse(tituloValido(""))
    }

    @Test
    fun tituloComApenasEspacosEhInvalido() {
        assertFalse(tituloValido("   "))
    }

    @Test
    fun tituloPreenchidoEhValido() {
        assertTrue(tituloValido("Estudar Kotlin"))
    }
}