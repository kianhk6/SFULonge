package com.example.sfulounge.ui.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User
import com.lorentzos.flingswipe.SwipeFlingAdapterView


// to do:
// 1) what if all users swiped then new users added but there are no actions
// to propagate get all users

class ExploreFragment : Fragment() {

    private lateinit var adapter: SwipeViewAdapter

    private lateinit var matchesViewModel: MatchesViewModel

    private lateinit var frame: SwipeFlingAdapterView
    private lateinit var arrayOfUsers: ArrayList<User>
    private lateinit var exploreUsers: ArrayList<User>
    val isObserverDone = MutableLiveData<Boolean>()

    private var _isInitialLoadOrRefresh = true


    interface ObserverCompletionCallback {
        fun onObserverCompleted()
    }

    @SuppressLint("ClickableViewAccessibility")
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

        exploreUsers = ArrayList()
        arrayOfUsers = ArrayList()
        isObserverDone.value = false

        _isInitialLoadOrRefresh = true
        waitForUsersToPropagate()

        isObserverDone.observe(viewLifecycleOwner) { isDone ->
            if (isDone) {
                println("set swipe is called")
                setUpSwipeAction()
            }
        }

        return view
    }


    private fun setUpSwipeAction() {
        frame = requireView().findViewById<SwipeFlingAdapterView>(R.id.frame)

        arrayOfUsers = exploreUsers
        println("Array of users in setup swipes: ${arrayOfUsers.size}")

        val userInfoView = view?.findViewById<LinearLayout>(R.id.view_user_info)
        userInfoView!!.visibility = View.GONE
        adapter = SwipeViewAdapter(requireContext(), arrayOfUsers, userInfoView!!)
        println("adapter count: ${adapter.count}")


        frame.adapter = adapter
        adapter.notifyDataSetChanged()

        frame.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                arrayOfUsers.removeAt(0)
                adapter.notifyDataSetChanged()
                println("adapter count: ${adapter.count}")
            }

            override fun onLeftCardExit(dataObject: Any?) {
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                // Assuming 'current_recommended_user' holds the user object that got swiped on
                userInfoView.visibility = View.GONE
                val swipedOnUser = matchesViewModel.current_recommended_user
                println("this is from fragment: current user that got swiped on is: $swipedOnUser")
                matchesViewModel.addSwipeLeft(
                    matchesViewModel.current_recommended_user!!.userId,
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

            override fun onRightCardExit(dataObject: Any?) {
                userInfoView.visibility = View.GONE
                matchesViewModel.addSwipeRight(
                    matchesViewModel.current_recommended_user!!,
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

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                println("items left: $itemsInAdapter")
//                // Ask for more data here
//                waitForUsersToPropagate()
//                arrayOfImages.add("XML " + java.lang.String.valueOf(i))
//                arrayAdapter.notifyDataSetChanged()
//                Log.d("LIST", "notified")
//                i++
            }

            override fun onScroll(p0: Float) {
                val view = frame.getSelectedView();
                val rightIndicator = view.findViewById<View>(R.id.item_swipe_right_indicator)
                val leftIndicator = view.findViewById<View>(R.id.item_swipe_left_indicator)
                if (p0 < 0) {
                    rightIndicator.alpha = -p0
                    leftIndicator.alpha = 0f
                } else if (p0 > 0) {
                    rightIndicator.alpha = 0f
                    leftIndicator.alpha = p0
                } else {
                    rightIndicator.alpha = 0f
                    leftIndicator.alpha = 0f
                }
            }
        })
    }

    private fun loadUserInfo() {
        val user = matchesViewModel.current_recommended_user
        println("loading user: $user")
        if (user != null) {
            //refresh visibility
            view?.findViewById<LinearLayout>(R.id.interestsLinearLayout1)?.visibility =
                View.VISIBLE
            view?.findViewById<LinearLayout>(R.id.interestsLinearLayout2)?.visibility =
                View.VISIBLE
            view?.findViewById<TextView>(R.id.interest1)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.interest2)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.interest3)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.interest4)?.visibility = View.VISIBLE
            view?.findViewById<LinearLayout>(R.id.layout_depth_question_1)?.visibility =
                View.VISIBLE
            view?.findViewById<LinearLayout>(R.id.layout_depth_question_2)?.visibility =
                View.VISIBLE
            view?.findViewById<LinearLayout>(R.id.layout_depth_question_3)?.visibility =
                View.VISIBLE

            // fill info
            val tvGender = view?.findViewById<TextView>(R.id.tv_gender)
            tvGender?.setText(resources.getStringArray(R.array.gender_array)[user.gender])
            when (user.interests.size) {
                0 -> {
                    view?.findViewById<LinearLayout>(R.id.interestsLinearLayout2)?.visibility =
                        View.GONE
                    view?.findViewById<TextView>(R.id.interest2)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.interest1)?.setText("No Interests")
                }

                1 -> {
                    view?.findViewById<LinearLayout>(R.id.interestsLinearLayout2)?.visibility =
                        View.GONE
                    view?.findViewById<TextView>(R.id.interest2)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.interest1)?.setText(user.interests[0])
                }

                2 -> {
                    view?.findViewById<LinearLayout>(R.id.interestsLinearLayout2)?.visibility =
                        View.GONE
                    view?.findViewById<TextView>(R.id.interest1)?.setText(user.interests[0])
                    view?.findViewById<TextView>(R.id.interest2)?.setText(user.interests[1])
                }

                3 -> {
                    view?.findViewById<TextView>(R.id.interest4)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.interest1)?.setText(user.interests[0])
                    view?.findViewById<TextView>(R.id.interest2)?.setText(user.interests[1])
                    view?.findViewById<TextView>(R.id.interest3)?.setText(user.interests[2])
                }

                4 -> {
                    view?.findViewById<TextView>(R.id.interest1)?.setText(user.interests[0])
                    view?.findViewById<TextView>(R.id.interest2)?.setText(user.interests[1])
                    view?.findViewById<TextView>(R.id.interest3)?.setText(user.interests[2])
                    view?.findViewById<TextView>(R.id.interest4)?.setText(user.interests[3])
                }

                else -> println("More than 4 interests")
            }
            when (user.depthQuestions.size) {
                0 -> {
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_1)?.visibility =
                        View.GONE
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_2)?.visibility =
                        View.GONE
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_3)?.visibility =
                        View.GONE
                }

                1 -> {
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_2)?.visibility =
                        View.GONE
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_3)?.visibility =
                        View.GONE
                    view?.findViewById<TextView>(R.id.tv_depth_question_1)
                        ?.setText(user.depthQuestions[0].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_1)
                        ?.setText(user.depthQuestions[0].answer)
                }

                2 -> {
                    view?.findViewById<LinearLayout>(R.id.layout_depth_question_3)?.visibility =
                        View.GONE
                    view?.findViewById<TextView>(R.id.tv_depth_question_1)
                        ?.setText(user.depthQuestions[0].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_1)
                        ?.setText(user.depthQuestions[0].answer)
                    view?.findViewById<TextView>(R.id.tv_depth_question_2)
                        ?.setText(user.depthQuestions[1].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_2)
                        ?.setText(user.depthQuestions[1].answer)
                }

                3 -> {
                    view?.findViewById<TextView>(R.id.tv_depth_question_1)
                        ?.setText(user.depthQuestions[0].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_1)
                        ?.setText(user.depthQuestions[0].answer)
                    view?.findViewById<TextView>(R.id.tv_depth_question_2)
                        ?.setText(user.depthQuestions[1].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_2)
                        ?.setText(user.depthQuestions[1].answer)
                    view?.findViewById<TextView>(R.id.tv_depth_question_3)
                        ?.setText(user.depthQuestions[2].question)
                    view?.findViewById<TextView>(R.id.tv_depth_answer_3)
                        ?.setText(user.depthQuestions[2].answer)
                }

                else -> println("More than 3 questions")
            }
        }
    }

    private fun loadNextRecommendation() {
        matchesViewModel.popAndGetNextUser { user ->
            if (user != null) {
                // Display the user's details
                println("User ID: ${user.userId}, Name: ${user.firstName}")
                loadUserInfo()
            } else {
                println("no more users to show")
                // as all current users have been swiped on:
                // reload all users to see if we have  any new users to show
                matchesViewModel.current_recommended_user = null

                // must wait for the query to be done before updating the variable
                refreshData()
            }
        }
    }

    // this should be only called initially (onCreateView) or on refresh
    private fun waitForUsersToPropagate() {
        var isInitialLoad = true

        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                if (!_isInitialLoadOrRefresh) {
                    return@observe
                }
                _isInitialLoadOrRefresh = false
                // as there can be a case where users are fetched and already set to current users
                // this would result on infinite checks
                // only propagate if there are no other users
                exploreUsers = ArrayList(users)
                println("HI: ${exploreUsers.size}")
                // user loaded for the first time
                if (matchesViewModel.current_recommended_user == null) {
                    matchesViewModel.getTheFirstUser { user ->
                        user?.let {
                            println("User ID: ${it.userId}, Name: ${it.firstName}")
                            if (matchesViewModel.mainPageCancelled) {
                                val noMoreUsersView =
                                    view?.findViewById<View>(R.id.noMoreUsersLayout)
                                noMoreUsersView?.visibility = View.GONE
                                view?.findViewById<ScrollView>(R.id.mainContent)?.visibility =
                                    View.VISIBLE
                            }
                            loadUserInfo()
//                            setUpSwipeAction()
                        }
                    }
                }
                // user was already loaded
                else {
                    loadUserInfo()
                }
                println("waitForUsersToPropagate is done")
                if(isInitialLoad){
                    isObserverDone.value = true
                    isInitialLoad = false
                }
            } else {
                view?.findViewById<ScrollView>(R.id.mainContent)?.visibility = View.GONE
                matchesViewModel.mainPageCancelled = true
                val noMoreUsersView = view?.findViewById<View>(R.id.noMoreUsersLayout)
                noMoreUsersView?.visibility = View.VISIBLE
                Toast.makeText(context, "No new user found", Toast.LENGTH_SHORT).show()
                matchesViewModel.current_recommended_user = null
                // Set OnClickListener for the Refresh Button
                noMoreUsersView?.findViewById<Button>(R.id.button)?.setOnClickListener {
                    // Refresh logic here
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        Toast.makeText(context, "Finding new added users...", Toast.LENGTH_SHORT).show()
        matchesViewModel.getAllUsers()
        _isInitialLoadOrRefresh = true
        waitForUsersToPropagate()
    }
}