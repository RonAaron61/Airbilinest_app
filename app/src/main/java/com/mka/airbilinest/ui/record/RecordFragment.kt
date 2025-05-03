package com.mka.airbilinest.ui.record

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mka.airbilinest.R
import com.mka.airbilinest.databinding.FragmentRecordBinding
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mka.airbilinest.DatabaseHelper
import com.mka.airbilinest.Patient
import com.mka.airbilinest.PatientAdapter
import com.mka.airbilinest.PatientFormFragment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi


class RecordFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PatientAdapter
    private lateinit var exportButton: Button
    private lateinit var addButton: Button
    private var _binding: FragmentRecordBinding? = null

    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dbHelper = DatabaseHelper(requireContext())

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        exportButton = root.findViewById(R.id.exportButton)
        addButton = root.findViewById(R.id.addButton)

        loadPatients()

        addButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, PatientFormFragment(), "PatientFormFragment")
                .addToBackStack(null)
                .commit()
            logFragmentStack() // Check fragment stack
        }

        exportButton.setOnClickListener {
            checkPermissionsAndExportToExcel()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPatients() {
        val patientsList = dbHelper.getAllPatients()
        adapter = PatientAdapter(patientsList, { patient ->
            // Edit patient
            val fragment = PatientFormFragment.newInstance(patient)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, fragment)
                .addToBackStack(null)
                .commit()

        }, { patient ->
            // Delete patient
            deletePatient(patient)
        })
        recyclerView.adapter = adapter
    }

    private fun deletePatient(patient: Patient) {
        // Confirm the deletion with the user
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete ${patient.name}?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete from database
                dbHelper.deletePatient(patient.id)
                // Refresh the list
                loadPatients()
                Toast.makeText(requireContext(), "${patient.name} deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun checkPermissionsAndExportToExcel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped Storage (Android 10 and above)
            exportToExcelScopedStorage()
        } else {
            // Request WRITE_EXTERNAL_STORAGE permission (Android 9 and below)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                exportToExcelLegacyStorage()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportToExcelScopedStorage() {
        val patientsList = dbHelper.getAllPatients()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Patient Data")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ID")
        header.createCell(1).setCellValue("Name")
        header.createCell(2).setCellValue("Weight (gram)")
        header.createCell(3).setCellValue("Birth Date")
        header.createCell(4).setCellValue("Bilirubin Start (mg/dL)")
        header.createCell(5).setCellValue("Length of Phototherapy (hour)")
        header.createCell(6).setCellValue("Phototherapy Intensity (uW/cm^2/nm)")
        header.createCell(7).setCellValue("Bilirubin End (mg/dL)")

        for ((index, patient) in patientsList.withIndex()) {
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(patient.id)
            row.createCell(1).setCellValue(patient.name)
            row.createCell(2).setCellValue(patient.weight)
            row.createCell(3).setCellValue(patient.birthDate)
            row.createCell(4).setCellValue(patient.bilirubinStart)
            row.createCell(5).setCellValue(patient.lengthOfPhototherapy)
            row.createCell(6).setCellValue(patient.phototherapyIntensity)
            row.createCell(7).setCellValue(patient.bilirubinEnd)
        }

        // Save the file to the external storage (Downloads folder)
        val contentResolver = requireContext().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "PatientData.xlsx")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
                workbook.close()
                Toast.makeText(requireContext(), "Exported to ${uri.path}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Failed to export file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToExcelLegacyStorage() {
        val patientsList = dbHelper.getAllPatients()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Patient Data")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ID")
        header.createCell(1).setCellValue("Name")
        header.createCell(2).setCellValue("Weight (gram)")
        header.createCell(3).setCellValue("Birth Date")
        header.createCell(4).setCellValue("Bilirubin Start (mg/dL)")
        header.createCell(5).setCellValue("Length of Phototherapy (hour)")
        header.createCell(6).setCellValue("Phototherapy Intensity (uW/cm^2/nm)")
        header.createCell(7).setCellValue("Bilirubin End (mg/dL)")

        for ((index, patient) in patientsList.withIndex()) {
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(patient.id)
            row.createCell(1).setCellValue(patient.name)
            row.createCell(2).setCellValue(patient.weight)
            row.createCell(3).setCellValue(patient.birthDate)
            row.createCell(4).setCellValue(patient.bilirubinStart)
            row.createCell(5).setCellValue(patient.lengthOfPhototherapy)
            row.createCell(6).setCellValue(patient.phototherapyIntensity)
            row.createCell(7).setCellValue(patient.bilirubinEnd)
        }

        // Save the file to the external storage (Downloads folder)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "PatientData.xlsx")

        val fileOut = FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()
        workbook.close()

        Toast.makeText(requireContext(), "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }



    private fun logFragmentStack() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            Log.d("FragmentStack", "Fragment: ${fragment.javaClass.simpleName}, Visible: ${fragment.isVisible}")
        }
    }

}
