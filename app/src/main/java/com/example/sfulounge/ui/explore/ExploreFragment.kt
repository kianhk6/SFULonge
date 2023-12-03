package com.example.sfulounge.ui.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.Personality
import com.example.sfulounge.data.model.User
import com.lorentzos.flingswipe.SwipeFlingAdapterView


class ExploreFragment : Fragment() {

    private lateinit var adapter: SwipeViewAdapter

    private lateinit var matchesViewModel: MatchesViewModel

    private lateinit var frame: SwipeFlingAdapterView
    private lateinit var arrayOfUsers: ArrayList<User>
    private lateinit var exploreUsers: ArrayList<User>
    val isObserverDone = MutableLiveData<Boolean>()

    private var _isInitialLoadOrRefresh = true



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
                println("set swipe is called after observer being done")
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
                        println("current user swiped right: " + matchesViewModel.current_recommended_user!!.firstName)
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
                        println("current user swiped left: " + matchesViewModel.current_recommended_user!!.firstName)
                        loadNextRecommendation()
                    }
                ) { exception ->
                    // Handle error, e.g., show an error message
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {

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
            view?.findViewById<TextView>(R.id.tv_personality)?.visibility = View.VISIBLE
            view?.findViewById<FrameLayout>(R.id.imageFrame1)?.visibility = View.VISIBLE
            view?.findViewById<FrameLayout>(R.id.imageFrame2)?.visibility = View.VISIBLE
            view?.findViewById<FrameLayout>(R.id.imageFrame3)?.visibility = View.VISIBLE

            populateImages()

            // fill info
            val tvGender = view?.findViewById<TextView>(R.id.tv_gender)
            tvGender?.setText(resources.getStringArray(R.array.gender_array)[user.gender])
            if (user.personality == -1) {
                view?.findViewById<TextView>(R.id.tv_personality)?.visibility = View.GONE
            } else {
                val colorArray = intArrayOf(
                    resources.getColor(R.color.social_butterfly),
                    resources.getColor(R.color.long_wolf),
                    resources.getColor(R.color.organizer),
                    resources.getColor(R.color.adventurer),
                    resources.getColor(R.color.analyzer)
                )
                val personality = user.personality
                view?.findViewById<TextView>(R.id.tv_personality)?.setText(
                    resources.getStringArray(R.array.personality_array)[personality!!])
                view?.findViewById<TextView>(R.id.tv_personality)?.setBackgroundColor(
                    colorArray[personality!!])
            }
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

    private fun populateImages() {
        val user = matchesViewModel.current_recommended_user

        if (user != null) {
            when (user.photos.size) {
                1 -> {
                    view?.findViewById<FrameLayout>(R.id.imageFrame1)?.visibility = View.GONE
                    view?.findViewById<FrameLayout>(R.id.imageFrame2)?.visibility = View.GONE
                    view?.findViewById<FrameLayout>(R.id.imageFrame3)?.visibility = View.GONE
                }
                2 -> {
                    view?.findViewById<FrameLayout>(R.id.imageFrame1)?.visibility = View.VISIBLE
                    view?.findViewById<FrameLayout>(R.id.imageFrame2)?.visibility = View.GONE
                    view?.findViewById<FrameLayout>(R.id.imageFrame3)?.visibility = View.GONE
                    val imageView = view?.findViewById<ImageView>(R.id.imageUser1)
                    Glide.with(requireContext())
                        .load(user.photos[1])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView!!)
                }
                3 -> {
                    view?.findViewById<FrameLayout>(R.id.imageFrame1)?.visibility = View.VISIBLE
                    view?.findViewById<FrameLayout>(R.id.imageFrame2)?.visibility = View.VISIBLE
                    view?.findViewById<FrameLayout>(R.id.imageFrame3)?.visibility = View.GONE
                    val imageView = view?.findViewById<ImageView>(R.id.imageUser1)
                    val imageView2 = view?.findViewById<ImageView>(R.id.imageUser2)
                    Glide.with(requireContext())
                        .load(user.photos[1])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView!!)
                    Glide.with(requireContext())
                        .load(user.photos[2])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView2!!)
                }
                4 -> {
                    view?.findViewById<FrameLayout>(R.id.imageFrame1)?.visibility = View.VISIBLE
                    view?.findViewById<FrameLayout>(R.id.imageFrame2)?.visibility = View.VISIBLE
                    view?.findViewById<FrameLayout>(R.id.imageFrame3)?.visibility = View.VISIBLE
                    val imageView = view?.findViewById<ImageView>(R.id.imageUser1)
                    val imageView2 = view?.findViewById<ImageView>(R.id.imageUser2)
                    val imageView3 = view?.findViewById<ImageView>(R.id.imageUser3)
                    Glide.with(requireContext())
                        .load(user.photos[1])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView!!)
                    Glide.with(requireContext())
                        .load(user.photos[2])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView2!!)
                    Glide.with(requireContext())
                        .load(user.photos[3])
                        .placeholder(R.drawable.baseline_person_24) // Replace with the ID of your placeholder drawable
                        .error(R.drawable.baseline_close_24) // Optional: Specify an error image if the loading fails
                        .into(imageView3!!)
                }
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

        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                if (!_isInitialLoadOrRefresh) {
                    return@observe
                }
                _isInitialLoadOrRefresh = false
                // as there can be a case where users are fetched and already set to current users
                // this would result on infinite checks
                // only propagate if there are no other users
                // user loaded for the first time
                exploreUsers = ArrayList(users)
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
                        }
                    }
                }
                // user was already loaded
                else {
                    loadUserInfo()
                }
                isObserverDone.value = true
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