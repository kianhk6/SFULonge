package com.example.sfulounge.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MatchesViewModel
import com.example.sfulounge.MatchesViewModelFactory
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private lateinit var matchesViewModel: MatchesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        // Initialize the MatchesViewModel
        matchesViewModel = ViewModelProvider(
            this,
            MatchesViewModelFactory(MainRepository())
        ).get(MatchesViewModel::class.java)

//        // Observe the LiveData from MatchesViewModel
//        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
//            // Print out the user details to the console
//            users.forEach { user ->
//                println("User ID: ${user.userId}, Name: ${user.firstName} ${user.lastName} at time: ${user.timestamp}, current time: ${System.currentTimeMillis()}")
//            }
//        }
//
        matchesViewModel.popAndGetNextUser { user ->
            if (user != null) {
                println("User ID: ${user.userId}, Name: ${user.firstName} ${user.lastName}")
            }
        }

        return view
    }
}
