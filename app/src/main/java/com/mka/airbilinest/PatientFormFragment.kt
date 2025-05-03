package com.mka.airbilinest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PatientFormFragment : BaseFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private var currentPatient: Patient? = null

    companion object {
        fun newInstance(patient: Patient?): PatientFormFragment {
            val fragment = PatientFormFragment()
            val args = Bundle()
            args.putParcelable("patient", patient)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_patient_form, container, false)
        val birthdateview = view.findViewById<TextView>(R.id.birthdateshow)
        val birthDatePicker = view.findViewById<DatePicker>(R.id.birthDate)

        dbHelper = DatabaseHelper(requireContext())
        currentPatient = arguments?.getParcelable("patient")

        Log.d("PatientFormFragment", "Current patient: $currentPatient")

        val saveButton = view.findViewById<Button>(R.id.saveButton)

        birthDatePicker.init(birthDatePicker.year, birthDatePicker.month, birthDatePicker.dayOfMonth) { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            birthdateview.text = "Selected Date: $selectedDate"
        }

        if (currentPatient != null) {
            populateForm(view)
        }

        saveButton.setOnClickListener {
            savePatientData(view)
        }

        return view
    }

    private fun populateForm(view: View) {
        view.findViewById<EditText>(R.id.patientId).setText(currentPatient?.id)
        view.findViewById<EditText>(R.id.patientName).setText(currentPatient?.name)
        view.findViewById<EditText>(R.id.weight).setText(currentPatient?.weight.toString())
        view.findViewById<EditText>(R.id.bilirubinStart).setText(currentPatient?.bilirubinStart.toString())
        view.findViewById<EditText>(R.id.lengthOfPhototherapy).setText(currentPatient?.lengthOfPhototherapy.toString())
        view.findViewById<EditText>(R.id.phototherapyIntensity).setText(currentPatient?.phototherapyIntensity)
        view.findViewById<EditText>(R.id.bilirubinEnd).setText(currentPatient?.bilirubinEnd.toString())

        val birthDate = currentPatient?.birthDate
        if (!birthDate.isNullOrEmpty()) {
            try {
                // Assuming birthDate is stored as a String in format "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(birthDate)

                val calendar = Calendar.getInstance()
                calendar.time = date!!

                val birthDatePicker = view.findViewById<DatePicker>(R.id.birthDate)
                val selectedDateTextView = view.findViewById<TextView>(R.id.birthdateshow)

                // Set the DatePicker to the saved birth date
                birthDatePicker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                // Update the TextView to show the selected date
                selectedDateTextView.text = "Selected Date: $birthDate"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun savePatientData(view: View) {
        val id = view.findViewById<EditText>(R.id.patientId).text.toString()
        val name = view.findViewById<EditText>(R.id.patientName).text.toString()
        val weight = view.findViewById<EditText>(R.id.weight).text.toString()
        val birthDate = getSelectedDate(view.findViewById<DatePicker>(R.id.birthDate))
        val bilirubinStart = view.findViewById<EditText>(R.id.bilirubinStart).text.toString()
        val lengthOfPhototherapy = view.findViewById<EditText>(R.id.lengthOfPhototherapy).text.toString()
        val phototherapyIntensity = view.findViewById<EditText>(R.id.phototherapyIntensity).text.toString()
        val bilirubinEnd = view.findViewById<EditText>(R.id.bilirubinEnd).text.toString()

        val patient = Patient(id, name, weight, birthDate, 0, bilirubinStart, lengthOfPhototherapy, phototherapyIntensity, bilirubinEnd)

        if (currentPatient == null) {
            dbHelper.addPatient(patient)
        } else {
            dbHelper.updatePatient(patient)
        }

        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun getSelectedDate(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1
        val year = datePicker.year
        return "$day/$month/$year"
    }
}
