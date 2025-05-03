import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PatientDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_PATIENTS = "patients"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_BIRTH_DATE = "birthDate"
        private const val COLUMN_BILIRUBIN_START = "bilirubinStart"
        private const val COLUMN_LENGTH_OF_PHOTOTHERAPY = "lengthOfPhototherapy"
        private const val COLUMN_PHOTOTHERAPY_INTENSITY = "phototherapyIntensity"
        private const val COLUMN_BILIRUBIN_END = "bilirubinEnd"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_PATIENTS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USER_ID INTEGER NOT NULL,"
                + "$COLUMN_NAME TEXT NOT NULL,"
                + "$COLUMN_WEIGHT INTEGER,"
                + "$COLUMN_BIRTH_DATE TEXT,"
                + "$COLUMN_BILIRUBIN_START REAL,"
                + "$COLUMN_LENGTH_OF_PHOTOTHERAPY REAL,"
                + "$COLUMN_PHOTOTHERAPY_INTENSITY REAL,"
                + "$COLUMN_BILIRUBIN_END REAL)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PATIENTS")
        onCreate(db)
    }

    // Ambil semua data pasien berdasarkan user_id
    fun getPatientsByUserId(userId: Int): List<Patient> {
        val patients = mutableListOf<Patient>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PATIENTS,
            arrayOf(
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_WEIGHT,
                COLUMN_BIRTH_DATE,
                COLUMN_BILIRUBIN_START,
                COLUMN_LENGTH_OF_PHOTOTHERAPY,
                COLUMN_PHOTOTHERAPY_INTENSITY,
                COLUMN_BILIRUBIN_END
            ),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val patient = Patient(
                    cursor.getInt(0),
                    userId,
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getFloat(7)
                )
                patients.add(patient)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return patients
    }

    // Tambahkan data pasien
    fun addPatient(patient: Patient): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, patient.userId)
            put(COLUMN_NAME, patient.name)
            put(COLUMN_WEIGHT, patient.weight)
            put(COLUMN_BIRTH_DATE, patient.birthDate)
            put(COLUMN_BILIRUBIN_START, patient.bilirubinStart)
            put(COLUMN_LENGTH_OF_PHOTOTHERAPY, patient.lengthOfPhototherapy)
            put(COLUMN_PHOTOTHERAPY_INTENSITY, patient.phototherapyIntensity)
            put(COLUMN_BILIRUBIN_END, patient.bilirubinEnd)
        }
        val id = db.insert(TABLE_PATIENTS, null, values)
        db.close()
        return id
    }

    // Hapus data pasien
    fun deletePatient(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_PATIENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
}