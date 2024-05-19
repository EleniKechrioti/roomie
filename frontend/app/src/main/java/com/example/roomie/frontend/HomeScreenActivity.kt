package com.example.roomie.frontend

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.R
import com.example.roomie.backend.domain.Room
import com.example.roomie.frontend.Adapters.RoomsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class HomeScreenActivity : AppCompatActivity(), RoomsAdapter.onRoomClickListener {

    var bottomNavigationView: BottomNavigationView? = null
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        bottomNavigationView = findViewById<View>(R.id.bottomNav) as BottomNavigationView?

        bottomNavigationView?.setSelectedItemId(R.id.homeBtn)
        bottomNavigationView!!.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (item.itemId == R.id.accountbtn) {
                val intent: Intent = Intent(this, HomeScreenActivity::class.java)
                startActivity(intent)
            } else if (itemId == R.id.reservbtn) {
                val intent: Intent = Intent(this, HomeScreenActivity::class.java)
                startActivity(intent)
            } else if (itemId == R.id.searchbtn) {
                val intent: Intent = Intent(this, SearchActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.slide_in,  // Animation for the entering activity
                        R.anim.slide_out  // Animation for the exiting activity
                )
                startActivity(intent, options.toBundle())
                //startActivity(intent)
                //overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in, R.anim.slide_out, 0)
            }
            item.setChecked(true)
            true
        })

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)!!
        val grid = GridLayoutManager(this,2)
        recyclerView!!.layoutManager = grid

        val rt = RoomTester()
        val rooms = rt.getRooms()
        Log.d("HomeScreen", rooms.toString())
        val roomsAdapter = RoomsAdapter(rooms, this)
        recyclerView!!.adapter = roomsAdapter

    }

    override fun onRoomClick(room: Room, view: View) {
        var intent = Intent(this, RoomDetailsActivity::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.expand_trans,  // Animation for the entering activity
                R.anim.slide_out  // Animation for the exiting activity
        )
        startActivity(intent, options.toBundle())
        //startActivity(intent)
    }
}