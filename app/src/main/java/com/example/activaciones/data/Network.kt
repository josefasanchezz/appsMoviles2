package com.example.activaciones.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/** Servicio de prueba (JSONPlaceholder). Se transforma en eventos ficticios; es opcional. */
data class PostDto(val id: Int, val title: String)

interface EventsApi {
    @GET("posts")
    suspend fun posts(): List<PostDto>
}

data class ExternalEvent(
    val id: Int, val name: String, val date: String, val comuna: String, val organization: String
)

private val comunas = listOf("Santiago", "Providencia", "Las Condes", "Ñuñoa", "Puente Alto", "Maipú", "La Florida")
private val orgs = listOf("Cámara Ficticia A", "Fundación Demo B", "Corporación Prueba C", "Asociación Muestra D")

object Network {
    private val api: EventsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventsApi::class.java)
    }

    suspend fun fetchEvents(): List<ExternalEvent> = api.posts().take(15).map { p ->
        ExternalEvent(
            id = p.id,
            name = "Evento " + p.title.split(" ").take(3).joinToString(" "),
            date = "%02d/%02d/2027".format(1 + p.id % 28, 1 + p.id % 12),
            comuna = comunas[p.id % comunas.size],
            organization = orgs[p.id % orgs.size]
        )
    }

    /** Datos locales ficticios usados si no hay conexión. */
    fun offlineEvents(): List<ExternalEvent> = (1..8).map { i ->
        ExternalEvent(i, "Evento ficticio #$i", "%02d/%02d/2027".format(3 * i, 1 + i), comunas[i % comunas.size], orgs[i % orgs.size])
    }
}
