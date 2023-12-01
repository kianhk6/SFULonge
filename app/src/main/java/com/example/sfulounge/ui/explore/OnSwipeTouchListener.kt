package com.example.sfulounge.ui.explore

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    open override fun onTouch(view: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 50
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val distanceX = e2.x - e1!!.x
            val distanceY = e2.y - e1.y

            if (Math.abs(distanceX) > Math.abs(distanceY) &&
                Math.abs(distanceX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (distanceX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
                return true
            }

            return false
        }
    }

    open fun onSwipeRight() {
        // Override this method in activity
    }

    open fun onSwipeLeft() {
        // Override this method in activity
    }
}

// your previous code:
//val onSwipeTouchListener = object : OnSwipeTouchListener(requireContext()) {
    //            override fun onTouch(view: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        // Save initial touch position
//                        initialX = event.x
//                        initialY = event.y
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        // Calculate the distance moved by the user's finger
//                        val offsetX = event.x - initialX
//                        val offsetY = event.y - initialY
//
//                        // Update the position of the image based on the finger movement
//                        imageFrame.translationX = offsetX
//                        imageFrame.translationY = offsetY
//
////                        // Check if the horizontal movement exceeds the threshold for swipe
////                        if (Math.abs(offsetX) > 50) {
////                            // If it does, treat it as a swipe and reset initialX to avoid rapid accumulation
////                            initialX = event.x
////                        }
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        // Reset initialX and initialY to avoid accumulation when not touching
//                        initialX = 0F
//                        initialY = 0F
//                    }
//                }
//                return true
////                return super.onTouch(view, motionEvent)
//            }
//    override fun onSwipeLeft() {
//        // Handle swipe left action (e.g., load the next recommendation)
//        // Assuming 'current_recommended_user' holds the user object that got swiped on
//        val swipedOnUser = matchesViewModel.current_recommended_user
//        println("this is from fragment: current user that got swiped on is: $swipedOnUser")
//        matchesViewModel.addSwipeLeft(
//            matchesViewModel.current_recommended_user.userId,
//            onSuccess = {
//                // Handle success, e.g., show a success message
//                Toast.makeText(context, "Swipe left successful", Toast.LENGTH_SHORT).show()
//                loadNextRecommendation()
//                loadUserInfo()
//            },
//            onError = { exception ->
//                // Handle error, e.g., show an error message
//                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        )
//    }
//
//    override fun onSwipeRight() {
//        // Handle swipe right action (e.g., load the next recommendation)
//        // Assuming 'userThatGotSwipedOnId' is the ID of the user that got swiped on
//        matchesViewModel.addSwipeRight(
//            matchesViewModel.current_recommended_user,
//            onSuccess = {
//                // Handle success, e.g., show a success message
//                Toast.makeText(context, "Swipe right successful", Toast.LENGTH_SHORT).show()
//                loadNextRecommendation()
//                loadUserInfo()
//            }
//        ) { exception ->
//            // Handle error, e.g., show an error message
//            Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//}
//
//// Set OnTouchListener on userImage
//userImage.setOnTouchListener(onSwipeTouchListener)