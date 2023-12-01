package com.example.sfulounge.ui.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sfulounge.MatchesViewModel
import com.example.sfulounge.MatchesViewModelFactory
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User
import com.lorentzos.flingswipe.SwipeFlingAdapterView


// to do:
// 1) what if all users swiped then new users added but there are no actions
// to propagate get all users

class ExploreFragment : Fragment() {

    private lateinit var matchesViewModel: MatchesViewModel
//    private lateinit var userImage: ImageView
    private lateinit var imageFrame: FrameLayout
    private lateinit var onSwipeTouchListener: OnSwipeTouchListener
    private var initialX: Float = 0F
    private var initialY: Float = 0F
    private lateinit var frame: SwipeFlingAdapterView
    private val usersArray = ArrayList<User>()
    private lateinit var arrayOfImages: ArrayList<String>
    private lateinit var exploreUsers : List<User>
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

        exploreUsers = listOf<User>()
        frame = view.findViewById<SwipeFlingAdapterView>(R.id.frame)
        arrayOfImages = ArrayList()
        waitForUsersToPropagate()
//        setUpSwipeAction()

        matchesViewModel.printList()
//        userImage = view.findViewById<ImageView>(R.id.imageUser)
//        imageFrame = view.findViewById<FrameLayout>(R.id.imageFrame)
        //implement swipe action for userImage
        // Create an instance of OnSwipeTouchListener
//        frame = view.findViewById<SwipeFlingAdapterView>(R.id.frame)
//        arrayOfImages = ArrayList()
//        setUpSwipeAction()
        val buttonSwipeRight = view.findViewById<Button>(R.id.buttonSwipeRight)
        buttonSwipeRight.setOnClickListener {
            // Assuming 'userThatGotSwipedOnId' is the ID of the user that got swiped on
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

        val buttonSwipeLeft = view.findViewById<Button>(R.id.buttonSwipeLeft)
        buttonSwipeLeft.setOnClickListener {
            // Assuming 'current_recommended_user' holds the user object that got swiped on
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




        return view
    }


    private fun setUpSwipeAction() {
        println("SWIPE FUNCTION STARTED")
//        val currentList = matchesViewModel.currentUsers
//        currentList.observe(owner, Observer { data ->
//            // Clear the existing data in the ArrayList
//            arrayList.clear()
//
//            // Add the new data to the ArrayList
//            if (data != null) {
//                arrayList.addAll(data)
//            }
//
//            // Now, the 'arrayList' contains the updated data from the LiveData
//            // You can use 'arrayList' as needed
//        })
//        arrayOfImages.clear()
//        val usersArray = ArrayList<User>()
//        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
//            usersArray.clear()
////            arrayOfImages.clear()
//            usersArray.addAll(users)
////            users.forEach { user ->
////                if (user.photos.isNotEmpty()) {
//////                        println("ADD IMAGE")
////                    arrayOfImages.add(user.photos[0])
////                }
////            }
//            println("users Size: ${usersArray.size}")
//            println("images Size: ${arrayOfImages.size}")
//
////            val adapter = ImageAdapter(requireContext(), arrayOfImages)
////            println("users Size: ${usersArray.size}")
//
//        }
        arrayOfImages.add("php")
        arrayOfImages.add("c")
        arrayOfImages.add("python")
        arrayOfImages.add("java")
        val adapter = ArrayAdapter(requireContext(), R.layout.swipe_image_item, R.id.helloText, arrayOfImages)
        frame.adapter = adapter

        frame.setFlingListener(object: SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
//                Log.d("LIST", "removed object!")
                arrayOfImages.removeAt(0)
                adapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any?) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                Toast.makeText(requireContext(), "Left!", Toast.LENGTH_SHORT).show()
            }

            override fun onRightCardExit(dataObject: Any?) {
                Toast.makeText(requireContext(), "Right!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
//                // Ask for more data here
//                arrayOfImages.add("XML " + java.lang.String.valueOf(i))
//                arrayAdapter.notifyDataSetChanged()
//                Log.d("LIST", "notified")
//                i++
            }

            override fun onScroll(p0: Float) {

            }
        })
//        println("users Size 2: ${usersArray.size}")
//        println("name: ${usersArray[0].firstName}")
//        println("users Size: ${currentList.size}")
//        usersArray.forEach { user ->
//            if (user.photos.isNotEmpty()) {
//                println("IMAGES NOT EMPTY")
//                arrayOfImages.add(user.photos[0])
//            }
//        }
//        println("Size: ${arrayOfImages.size}")
//        for (item in arrayOfImages) println("url: $item")
//        arrayOfImages.add("php");
//        arrayOfImages.add("c");
//        arrayOfImages.add("python");
//        arrayOfImages.add("java");
//        val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.swipe_image_item, R.id.user_image, arrayOfImages)
//        val adapter = ImageAdapter(requireContext(), arrayOfImages)
//        frame.adapter = adapter


    }

//    private fun loadUserImage(imageUrl: String?) {
//        imageUrl?.let {
//            Glide.with(requireContext())
//                .load(it)
//                .error(R.drawable.baseline_close_24) // Error image if loading fails
//                .into(userImage)
//        }
//    }

    private fun loadUserInfo() {
        val user = matchesViewModel.current_recommended_user
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
//            if (user.photos.isNotEmpty()) loadUserImage(user.photos[0])
            val tvName = view?.findViewById<TextView>(R.id.tv_name)
            tvName?.setText(user.firstName)
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
                matchesViewModel.getAllUsers()
                // must wait for the query to be done before updating the variable
                waitForUsersToPropagate()
            }
        }
    }

    private fun waitForUsersToPropagate() {

        matchesViewModel.currentUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                // as there can be a case where users are fetched and already set to current users
                // this would result on infinite checks
                // only propagate if there are no other users
                exploreUsers = users
                println(exploreUsers.size)
                if(matchesViewModel.current_recommended_user == null){
                    matchesViewModel.getTheFirstUser { user ->
                        user?.let {
                            println("User ID: ${it.userId}, Name: ${it.firstName}")
                            if(matchesViewModel.mainPageCancelled){
                                val noMoreUsersView = view?.findViewById<View>(R.id.noMoreUsersLayout)
                                noMoreUsersView?.visibility = View.GONE
                                view?.findViewById<ScrollView>(R.id.mainContent)?.visibility = View.VISIBLE
                            }
                            loadUserInfo()
                            setUpSwipeAction()
                        }
                    }
                } else {
                    loadUserInfo()
                }
//                setUpSwipeAction()
            }
            else{
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
        waitForUsersToPropagate()
    }
}
