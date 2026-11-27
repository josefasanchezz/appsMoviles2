package com.example.activaciones.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class Role { EJECUTIVO, SUPERVISOR }

enum class Priority(val label: String) { ALTA("Alta"), MEDIA("Media"), BAJA("Baja") }

enum class Status(val label: String) {
    IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA("En espera de respuesta"),
    CONFIRMADA("Participación confirmada"),
    DESCARTADA("Oportunidad descartada");

    val isFinal: Boolean get() = this == CONFIRMADA || this == DESCARTADA

    /** Transiciones permitidas según el diagrama de estados. */
    fun nextOptions(): List<Status> = when (this) {
        IDENTIFICADO -> listOf(CONTACTO_PENDIENTE, DESCARTADA)
        CONTACTO_PENDIENTE -> listOf(CONTACTO_REALIZADO, DESCARTADA)
        CONTACTO_REALIZADO -> listOf(PROPUESTA_ENVIADA, DESCARTADA)
        PROPUESTA_ENVIADA -> listOf(EN_ESPERA, DESCARTADA)
        EN_ESPERA -> listOf(CONFIRMADA, DESCARTADA)
        else -> emptyList()
    }
}

class Converters {
    @TypeConverter fun fromRole(v: Role): String = v.name
    @TypeConverter fun toRole(v: String): Role = Role.valueOf(v)
    @TypeConverter fun fromPriority(v: Priority): String = v.name
    @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)
    @TypeConverter fun fromStatus(v: Status): String = v.name
    @TypeConverter fun toStatus(v: String): Status = Status.valueOf(v)
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: Role
)

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val name: String
)

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String,
    val eventDate: String,
    val comuna: String,
    val organizationId: Long,
    val contactId: Long,
    val expectedAudience: Int,
    val priority: Priority,
    val status: Status,
    val responsibleId: Long,
    val closed: Boolean = false,
    val createdAt: Long
)

@Entity(tableName = "followups")
data class FollowUpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opportunityId: Long,
    val type: String,
    val notes: String,
    val proposalSent: Boolean,
    val nextAction: String,
    val authorId: Long,
    val createdAt: Long
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opportunityId: Long,
    val description: String,
    val authorId: Long,
    val createdAt: Long
)

@Entity(tableName = "activations")
data class ActivationEntity(
    @PrimaryKey val opportunityId: Long,
    val space: String,
    val date: String,
    val cost: Int,
    val requirements: String,
    val responsibleId: Long?,
    val evidenceUri: String?
)

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey val opportunityId: Long,
    val outcome: String,
    val observations: String,
    val impact: String,
    val evaluation: Int,
    val createdAt: Long
)

/** Fila de lectura con los datos ya unidos (JOIN) para listas y detalle. */
data class OpportunityRow(
    val id: Long,
    val eventName: String,
    val eventDate: String,
    val comuna: String,
    val organizationName: String,
    val contactName: String,
    val expectedAudience: Int,
    val priority: Priority,
    val status: Status,
    val responsibleId: Long,
    val responsibleName: String?,
    val closed: Boolean
)

data class HistoryRow(val description: String, val authorName: String?, val createdAt: Long)
