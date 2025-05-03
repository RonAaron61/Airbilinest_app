package com.mka.airbilinest

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PatientDB"
        private const val TABLE_PATIENTS = "Patients"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_BILIRUBIN_START = "bilirubin_start"
        private const val KEY_LENGTH_PHOTOTHERAPY = "length_phototherapy"
        private const val KEY_PHOTOTHERAPY_INTENSITY = "phototherapy_intensity"
        private const val KEY_BILIRUBIN_END = "bilirubin_end"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_PATIENTS("
                + "$KEY_ID TEXT PRIMARY KEY,"
                + "$KEY_NAME TEXT,"
                + "$KEY_WEIGHT REAL,"
                + "$KEY_BIRTH_DATE TEXT,"
                + "$KEY_BILIRUBIN_START REAL,"
                + "$KEY_LENGTH_PHOTOTHERAPY REAL,"
                + "$KEY_PHOTOTHERAPY_INTENSITY TEXT,"
                + "$KEY_BILIRUBIN_END REAL)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PATIENTS")
        onCreate(db)
    }

    fun addPatient(patient: Patient) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, patient.id)
            put(KEY_NAME, patient.name)
            put(KEY_WEIGHT, patient.weight)
            put(KEY_BIRTH_DATE, patient.birthDate)
            put(KEY_BILIRUBIN_START, patient.bilirubinStart)
            put(KEY_LENGTH_PHOTOTHERAPY, patient.lengthOfPhototherapy)
            put(KEY_PHOTOTHERAPY_INTENSITY, patient.phototherapyIntensity)
            put(KEY_BILIRUBIN_END, patient.bilirubinEnd)
        }

        db.insert(TABLE_PATIENTS, null, values)
        db.close()
    }

    fun getAllPatients(): List<Patient> {
        val patientList = ArrayList<Patient>()
        val selectQuery = "SELECT * FROM $TABLE_PATIENTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val patient = Patient(
                    id = cursor.getString(cursor.getColumnIndex(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                    weight = cursor.getString(cursor.getColumnIndex(KEY_WEIGHT)),
                    birthDate = cursor.getString(cursor.getColumnIndex(KEY_BIRTH_DATE)),
                    age = 0,
                    bilirubinStart = cursor.getString(cursor.getColumnIndex(KEY_BILIRUBIN_START)),
                    lengthOfPhototherapy = cursor.getString(cursor.getColumnIndex(KEY_LENGTH_PHOTOTHERAPY)),
                    phototherapyIntensity = cursor.getString(cursor.getColumnIndex(KEY_PHOTOTHERAPY_INTENSITY)),
                    bilirubinEnd = cursor.getString(cursor.getColumnIndex(KEY_BILIRUBIN_END)),
                )
                patientList.add(patient)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return patientList
    }

    fun updatePatient(patient: Patient) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, patient.name)
            put(KEY_WEIGHT, patient.weight)
            put(KEY_BIRTH_DATE, patient.birthDate)
            put(KEY_BILIRUBIN_START, patient.bilirubinStart)
            put(KEY_LENGTH_PHOTOTHERAPY, patient.lengthOfPhototherapy)
            put(KEY_PHOTOTHERAPY_INTENSITY, patient.phototherapyIntensity)
            put(KEY_BILIRUBIN_END, patient.bilirubinEnd)
        }

        db.update(TABLE_PATIENTS, values, "$KEY_ID = ?", arrayOf(patient.id))
        db.close()
    }

    fun deletePatient(patientId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_PATIENTS, "$KEY_ID = ?", arrayOf(patientId))
        db.close()
    }

}
