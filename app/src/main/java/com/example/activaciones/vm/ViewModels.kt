package com.example.activaciones.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.activaciones.App
import com.example.activaciones.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val app: App) : ViewModel() {
    var error by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(false)
        private set

    fun login(user: String, pass: String, onOk: () -> Unit) {
        if (user.isBlank() || pass.isBlank()) { error = "Ingresa usuario y contraseña"; return }
        viewModelScope.launch {
            loading = true
            val u = app.repository.login(user, pass)
            loading = false
            if (u == null) error = "Credenciales incorrectas" else {
                error = null
                app.session.user = u
                onOk()
            }
        }
    }
}

class ListViewModel(private val app: App) : ViewModel() {
    val user = app.session.user
    val query = MutableStateFlow("")
    val status = MutableStateFlow<Status?>(null)
    val priority = MutableStateFlow<Priority?>(null)

    val items: StateFlow<List<OpportunityRow>> =
        combine(app.repository.opportunities, query, status, priority) { all, q, s, p ->
            val needle = q.trim().lowercase()
            all.filter { r ->
                (user?.role == Role.SUPERVISOR || r.responsibleId == user?.id) &&
                    (s == null || r.status == s) &&
                    (p == null || r.priority == p) &&
                    (needle.isEmpty() || listOf(r.eventName, r.organizationName, r.comuna,
                        r.contactName, r.responsibleName ?: "").any { it.lowercase().contains(needle) })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logout() { app.session.user = null }
}

class FormViewModel(private val app: App) : ViewModel() {
    val user = app.session.user!!
    val executives: StateFlow<List<UserEntity>> =
        app.repository.users.map { l -> l.filter { it.role == Role.EJECUTIVO } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(f: NewOpportunity, onDone: (Long) -> Unit) = viewModelScope.launch {
        onDone(app.repository.create(f, user))
    }
}

class DetailViewModel(private val app: App, val id: Long) : ViewModel() {
    private val repo = app.repository
    val user = app.session.user!!

    private fun <T> Flow<T>.state(init: T) =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), init)

    val row = repo.opportunity(id).state(null)
    val followUps = repo.followUps(id).state(emptyList())
    val history = repo.history(id).state(emptyList())
    val activation = repo.activation(id).state(null)
    val result = repo.result(id).state(null)
    val executives = repo.users.map { l -> l.filter { it.role == Role.EJECUTIVO } }.state(emptyList())

    suspend fun activationOnce() = repo.activation(id).first()
    suspend fun resultOnce() = repo.result(id).first()

    fun changeStatus(s: Status) = viewModelScope.launch { row.value?.let { repo.changeStatus(it, s, user) } }
    fun setPriority(p: Priority) = viewModelScope.launch { row.value?.let { repo.setPriority(it, p, user) } }
    fun reassign(u: UserEntity) = viewModelScope.launch { row.value?.let { repo.reassign(it, u, user) } }

    fun addFollowUp(type: String, notes: String, proposal: Boolean, next: String) = viewModelScope.launch {
        row.value?.let { repo.addFollowUp(it, type, notes, proposal, next, user) }
    }

    fun saveActivation(a: ActivationEntity, confirm: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        row.value?.let { repo.saveActivation(it, a, confirm, user); onDone() }
    }

    fun saveResult(r: ResultEntity, close: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        row.value?.let { repo.saveResult(it, r, close, user); onDone() }
    }

}

class CatalogViewModel : ViewModel() {
    var events by mutableStateOf<List<ExternalEvent>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var offline by mutableStateOf(false)
        private set

    init { load() }

    fun load() = viewModelScope.launch {
        loading = true
        try {
            events = Network.fetchEvents(); offline = false
        } catch (e: Exception) {
            events = Network.offlineEvents(); offline = true
        }
        loading = false
    }
}
