package com.example.activaciones.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

private const val ROW_SELECT = """
SELECT o.id AS id, o.eventName AS eventName, o.eventDate AS eventDate, o.comuna AS comuna,
       org.name AS organizationName, c.name AS contactName, o.expectedAudience AS expectedAudience,
       o.priority AS priority, o.status AS status, o.responsibleId AS responsibleId,
       u.fullName AS responsibleName, o.closed AS closed
FROM opportunities o
JOIN organizations org ON org.id = o.organizationId
JOIN contacts c ON c.id = o.contactId
LEFT JOIN users u ON u.id = o.responsibleId
"""

@Dao
interface AppDao {
    @Query("SELECT * FROM users WHERE username = :u AND password = :p LIMIT 1")
    suspend fun login(u: String, p: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName")
    fun users(): Flow<List<UserEntity>>

    @Query("SELECT * FROM organizations WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun orgByName(name: String): OrganizationEntity?

    @Insert suspend fun insertOrg(o: OrganizationEntity): Long
    @Insert suspend fun insertContact(c: ContactEntity): Long
    @Insert suspend fun insertOpportunity(o: OpportunityEntity): Long
    @Insert suspend fun insertFollowUp(f: FollowUpEntity): Long
    @Insert suspend fun insertHistory(h: HistoryEntity): Long

    @Query(ROW_SELECT + " ORDER BY o.createdAt DESC")
    fun rows(): Flow<List<OpportunityRow>>

    @Query(ROW_SELECT + " WHERE o.id = :id")
    fun row(id: Long): Flow<OpportunityRow?>

    @Query("UPDATE opportunities SET status = :s WHERE id = :id")
    suspend fun updateStatus(id: Long, s: Status)

    @Query("UPDATE opportunities SET priority = :p WHERE id = :id")
    suspend fun updatePriority(id: Long, p: Priority)

    @Query("UPDATE opportunities SET responsibleId = :r WHERE id = :id")
    suspend fun updateResponsible(id: Long, r: Long)

    @Query("UPDATE opportunities SET closed = 1 WHERE id = :id")
    suspend fun close(id: Long)

    @Query("SELECT * FROM followups WHERE opportunityId = :id ORDER BY createdAt DESC")
    fun followUps(id: Long): Flow<List<FollowUpEntity>>

    @Query("""SELECT h.description AS description, h.createdAt AS createdAt, u.fullName AS authorName
              FROM history h LEFT JOIN users u ON u.id = h.authorId
              WHERE h.opportunityId = :id ORDER BY h.createdAt DESC""")
    fun history(id: Long): Flow<List<HistoryRow>>

    @Query("SELECT * FROM activations WHERE opportunityId = :id")
    fun activation(id: Long): Flow<ActivationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivation(a: ActivationEntity)

    @Query("SELECT * FROM results WHERE opportunityId = :id")
    fun result(id: Long): Flow<ResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResult(r: ResultEntity)
}

@Database(
    entities = [
        UserEntity::class, OrganizationEntity::class, ContactEntity::class,
        OpportunityEntity::class, FollowUpEntity::class, HistoryEntity::class,
        ActivationEntity::class, ResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        fun build(ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, "activaciones.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) = seed(db)
                })
                .build()

        /** Datos ficticios iniciales (usuarios, organizaciones y oportunidades de ejemplo). */
        private fun seed(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            db.execSQL("INSERT INTO users(username,password,fullName,role) VALUES " +
                "('ejecutivo','1234','Camila Rojas','EJECUTIVO')," +
                "('ejecutivo2','1234','Diego Soto','EJECUTIVO')," +
                "('supervisor','1234','Marcela Fuentes','SUPERVISOR')")
            db.execSQL("INSERT INTO organizations(name) VALUES ('Feria Pyme Santiago'),('Cámara de Comercio Ficticia')")
            db.execSQL("INSERT INTO contacts(organizationId,name) VALUES (1,'Andrea Pérez (ficticio)'),(2,'Luis Herrera (ficticio)')")
            db.execSQL("INSERT INTO opportunities(eventName,eventDate,comuna,organizationId,contactId,expectedAudience,priority,status,responsibleId,closed,createdAt) VALUES " +
                "('Expo Emprende 2026','15/12/2026','Providencia',1,1,800,'ALTA','IDENTIFICADO',1,0,$now)," +
                "('Encuentro Empresarial del Sur','20/01/2027','Puente Alto',2,2,300,'MEDIA','CONTACTO_PENDIENTE',2,0,$now)")
            db.execSQL("INSERT INTO history(opportunityId,description,authorId,createdAt) VALUES " +
                "(1,'Oportunidad registrada',1,$now),(2,'Oportunidad registrada',2,$now)")
        }
    }
}
