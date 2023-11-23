package com.example.sfulounge.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MatchesViewModel
import com.example.sfulounge.MatchesViewModelFactory
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository


// to do:
// 1) what if all users swiped then new users added but there are no actions
// to propagate get all users

class ExploreFragment : Fragment() {

    private lateinit var matchesViewModel: MatchesViewModel
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

        waitForUsersToPropagate()

        val buttonSwipeRight = view.findViewById<ImageView>(R.id.buttonSwipeRight)
        buttonSwipeRight.setOnClickListener {
            // Assuming 'userThatGotSwipedOnId' is the ID of the user that got swiped on
            println(
                "this is from fragment: current user that got swiped on is: "
                        + matchesViewModel.current_recommended_user.firstName
            )
            matchesViewModel.addSwipeRight(
                matchesViewModel.current_recommended_user,
                onSuccess = {
                    // Handle success, e.g., show a success message
                    Toast.makeText(context, "Swipe right successful", Toast.LENGTH_SHORT).show()
                    loadNextRecommendation()
                }
            ) { exception ->
                // Handle error, e.g., show an error message
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val buttonSwipeLeft = view.findViewById<ImageView>(R.id.buttonSwipeLeft)
        buttonSwipeLeft.setOnClickListener {
            // Assuming 'current_recommended_user' holds the user object that got swiped on
            val swipedOnUser = matchesViewModel.current_recommended_user
            println("this is from fragment: current user that got swiped on is: $swipedOnUser")
            matchesViewModel.addSwipeLeft(
                matchesViewModel.current_recommended_user.userId,
                onSuccess = {
                    // Handle success, e.g., show a success message
                    Toast.makeText(context, "Swipe left successful", Toast.LENGTH_SHORT).show()
                    loadNextRecommendation()
                },
                onError = { exception ->
                    // Handle error, e.g., show an error message
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }


        return view
    }

    private fun loadNextRecommendation() {
        matchesViewModel.popAndGetNextUser { user ->
            if (user != null) {
                // Display the user's details
                println("User ID: ${user.userId}, Name: ${user.firstName} ${user.lastName}")
            } else {
                // as all current users have been swiped on:
                // reload all users to see if we have  any new users to show
                matchesViewModel.getAllUsers()
                matchesViewModel.isInitialUserFetched = false
                // must wait for the query to be done before updating the variable
                waitForUsersToPropagate()
            }
        }
    }

    private fun waitForUsersToPropagate() {
        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty() && !matchesViewModel.isInitialUserFetched) {
                matchesViewModel.getTheFirstUser { user ->
                    user?.let {
                        println("User ID: ${it.userId}, Name: ${it.firstName} ${it.lastName}")
                    }
                }
            }
        }
    }
}
