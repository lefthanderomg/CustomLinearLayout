package andrey.murzin.com.customlinearlayout

import andrey.murzin.com.customlinearlayout.case.FifthCaseFragment
import andrey.murzin.com.customlinearlayout.case.FirstCaseFragment
import andrey.murzin.com.customlinearlayout.case.FourthCaseFragment
import andrey.murzin.com.customlinearlayout.case.SecondCaseFragment
import andrey.murzin.com.customlinearlayout.case.ThirdCaseFragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            findViewById<Button>(R.id.case1_button).setOnClickListener { navigate(FirstCaseFragment()) }
            findViewById<Button>(R.id.case2_button).setOnClickListener { navigate(SecondCaseFragment()) }
            findViewById<Button>(R.id.case3_button).setOnClickListener { navigate(ThirdCaseFragment()) }
            findViewById<Button>(R.id.case4_button).setOnClickListener { navigate(FourthCaseFragment()) }
            findViewById<Button>(R.id.case5_button).setOnClickListener { navigate(FifthCaseFragment()) }
        }
    }

    private fun navigate(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
