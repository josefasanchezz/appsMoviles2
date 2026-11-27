package com.example.activaciones.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

data class NewOpportunity(
    val eventName: String,
    val eventDate: String,
    val comuna: String,
    val organization: String,
    val contact: String,
    val audience: Int,
    val priority: Priority,
    val responsibleId: Long
)

class Repository(private val db: AppDatabase) {
    private val dao = db.dao()

    val opportunities: Flow<List<OpportunityRow>> = dao.rows()
    val users: Flow<List<UserEntity>> = dao.users()

    suspend fun login(user: String, pass: String) = dao.login(user.trim(), pass)
    fun opportunity(id: Long) = dao.row(id)
    fun followUps(id: Long) = dao.followUps(id)
    fun history(id: Long) = dao.history(id)
    fun activation(id: Long) = dao.activation(id)
    fun result(id: Long) = dao.result(id)

    private suspend fun log(oppId: Long, text: String, author: UserEntity) {
        dao.insertHistory(HistoryEntity(opportunityId = oppId, description = text,
            authorId = author.id, createdAt = System.currentTimeMillis()))
    }

    suspend fun create(f: NewOpportunity, author: UserEntity): Long = db.withTransaction {
        val org = dao.orgByName(f.organization.trim())
            ?: OrganizationEntity(name = f.organization.trim()).let { it.copy(id = dao.insertOrg(it)) }
        val contactId = dao.insertContact(ContactEntity(organizationId = org.id, name = f.contact.trim()))
        val id = dao.insertOpportunity(
            OpportunityEntity(
                eventName = f.eventName.trim(), eventDate = f.eventDate, comuna = f.comuna.trim(),
                organizationId = org.id, contactId = contactId, expectedAudience = f.audience,
                priority = f.priority, status = Status.IDENTIFICADO, responsibleId = f.responsibleId,
                createdAt = System.currentTimeMillis()
            )
        )
        log(id, "Oportunidad registrada (prioridad ${f.priority.label})", author)
        id
    }

    suspend fun changeStatus(row: OpportunityRow, new: Status, author: UserEntity) = db.withTransaction {
        dao.updateStatus(row.id, new)
        log(row.id, "Estado: ${row.status.label} → ${new.label}", author)
    }

    suspend fun setPriority(row: OpportunityRow, p: Priority, author: UserEntity) = db.withTransaction {
        dao.updatePriority(row.id, p)
        log(row.id, "Prioridad: ${row.priority.label} → ${p.label}", author)
    }

    suspend fun reassign(row: OpportunityRow, to: UserEntity, author: UserEntity) = db.withTransaction {
        dao.updateResponsible(row.id, to.id)
        log(row.id, "Responsable reasignado a ${to.fullName}", author)
    }

    suspend fun addFollowUp(
        row: OpportunityRow, type: String, notes: String,
        proposalSent: Boolean, nextAction: String, author: UserEntity
    ) = db.withTransaction {
        dao.insertFollowUp(FollowUpEntity(opportunityId = row.id, type = type, notes = notes,
            proposalSent = proposalSent, nextAction = nextAction, authorId = author.id,
            createdAt = System.currentTimeMillis()))
        log(row.id, "Gestión registrada: $type" + if (proposalSent) " (propuesta enviada)" else "", author)
        // El estado avanza de forma coherente con la gestión registrada.
        if (proposalSent && row.status.ordinal < Status.PROPUESTA_ENVIADA.ordinal) {
            dao.updateStatus(row.id, Status.PROPUESTA_ENVIADA)
            log(row.id, "Estado: ${row.status.label} → ${Status.PROPUESTA_ENVIADA.label}", author)
        } else if (!proposalSent && row.status.ordinal < Status.CONTACTO_REALIZADO.ordinal) {
            dao.updateStatus(row.id, Status.CONTACTO_REALIZADO)
            log(row.id, "Estado: ${row.status.label} → ${Status.CONTACTO_REALIZADO.label}", author)
        }
    }

    suspend fun saveActivation(
        row: OpportunityRow, a: ActivationEntity, confirm: Boolean, author: UserEntity
    ) = db.withTransaction {
        dao.upsertActivation(a)
        log(row.id, "Condiciones de participación registradas (${a.space})", author)
        if (confirm && row.status != Status.CONFIRMADA) {
            dao.updateStatus(row.id, Status.CONFIRMADA)
            log(row.id, "Estado: ${row.status.label} → ${Status.CONFIRMADA.label}", author)
        }
    }

    suspend fun saveResult(row: OpportunityRow, r: ResultEntity, close: Boolean, author: UserEntity) =
        db.withTransaction {
            dao.upsertResult(r)
            log(row.id, "Resultado registrado: ${r.outcome}", author)
            if (close) {
                dao.close(row.id)
                log(row.id, "Oportunidad cerrada", author)
            }
        }
}
