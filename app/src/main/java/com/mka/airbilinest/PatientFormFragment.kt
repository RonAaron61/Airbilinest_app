import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mka.airbilinest.Patient
import com.mka.airbilinest.R


class PatientFormFragment : Fragment() {

    private var userId: Int = -1
    private var patient: Patient? = null

    companion object {
        fun newInstance(userId: Int): PatientFormFragment {
            val fragment = PatientFormFragment()
            val args = Bundle()
            args.putInt("user_id", userId)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(patient: Patient, userId: Int): PatientFormFragment {
            val fragment = PatientFormFragment()
            val args = Bundle()
            args.putParcelable("patient", patient)
            args.putInt("user_id", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt("user_id", -1) ?: -1
        patient = arguments?.getParcelable("patient")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout dan tambahkan logika untuk form pasien
        return inflater.inflate(R.layout.fragment_patient_form, container, false)
    }

    private fun savePatient(patient: Patient) {
        val dbHelper = DatabaseHelper(requireContext())
        if (patient.id == 0) {
            // Tambahkan pasien baru
            dbHelper.addPatient(patient)
        } else {
            // Update pasien yang sudah ada
            // Implementasikan metode update di DatabaseHelper jika diperlukan
        }
    }
}