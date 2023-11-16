package com.example.sfulounge.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MatchesViewModel
import com.example.sfulounge.MatchesViewModelFactory
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.FragmentExploreBinding


// to do:
// 1) swipe left
// 3) all matchesViewModel.current_recommended_user assigning to be done in view model itself

class ExploreFragment : Fragment() {

    private lateinit var matchesViewModel: MatchesViewModel

    //    private lateinit var current_recommended_user : User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        println("hello from explore fragment")

        // Initialize the MatchesViewModel
        matchesViewModel = ViewModelProvider(
            this,
            MatchesViewModelFactory(MainRepository())
        ).get(MatchesViewModel::class.java)



        waitForInitialUser()

        // in swipe function if all users are empty fetch again

        val buttonSwipeRight = view.findViewById<Button>(R.id.buttonSwipeRight)
        buttonSwipeRight.setOnClickListener {
            // Assuming 'userThatGotSwipedOnId' is the ID of the user that got swiped on
            println(
                "this is from fragment: current user that got swiped on is: "
                        + matchesViewModel.current_recommended_user
            )
            matchesViewModel.addSwipeRight(
                matchesViewModel.current_recommended_user.userId,
                onSuccess = {
                    // Handle success, e.g., show a success message
                    Toast.makeText(context, "Swipe right successful", Toast.LENGTH_SHORT).show()
                    matchesViewModel.popAndGetNextUser { user ->
                        if (user != null) {
                            // Display the user's details
                            matchesViewModel.current_recommended_user = user
                            println("User ID: ${user.userId}, Name: ${user.firstName} ${user.lastName}")
                        } else {
                            // as all current users have been swiped on reload users to see if we have
                            // any new users to show
                            matchesViewModel.getAllUsers()
                            matchesViewModel.isInitialUserFetched = false
                            // must wait for the query to be done before updating the variable
                            waitForInitialUser()
                        }
                    }
                },
                onError = { exception ->
                    // Handle error, e.g., show an error message
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }

        val buttonSwipeLeft = view.findViewById<Button>(R.id.buttonSwipeLeft)
        buttonSwipeLeft.setOnClickListener {
            // Assuming 'current_recommended_user' holds the user object that got swiped on
            val swipedOnUser = matchesViewModel.current_recommended_user
            println("this is from fragment: current user that got swiped on is: $swipedOnUser")

            swipedOnUser.let { user ->
                matchesViewModel.addSwipeLeft(
                    user.userId,
                    onSuccess = {
                        // Handle success, e.g., show a success message
                        Toast.makeText(context, "Swipe left successful", Toast.LENGTH_SHORT).show()
                        matchesViewModel.popAndGetNextUser { nextUser ->
                            if (nextUser != null) {
                                // Display the next user's details
                                matchesViewModel.current_recommended_user = nextUser
                                println("User ID: ${nextUser.userId}, Name: ${nextUser.firstName} ${nextUser.lastName}")
                            } else {
                                // Reload users if all current users have been swiped on
                                matchesViewModel.getAllUsers()
                                matchesViewModel.isInitialUserFetched = false
                                waitForInitialUser()
                            }
                        }
                    },
                    onError = { exception ->
                        // Handle error, e.g., show an error message
                        Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
        }


        return view
    }

    private fun waitForInitialUser() {
        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty() && !matchesViewModel.isInitialUserFetched) {
                matchesViewModel.isInitialUserFetched = true
                matchesViewModel.popAndGetNextUser { user ->
                    user?.let {
                        matchesViewModel.current_recommended_user = it
                        println("User ID: ${it.userId}, Name: ${it.firstName} ${it.lastName}")
                        // Additionally, update UI here if needed
                    }
                }
            }
        }
    }
}
