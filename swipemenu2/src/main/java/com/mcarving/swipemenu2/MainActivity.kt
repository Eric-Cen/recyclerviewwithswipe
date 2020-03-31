package com.mcarving.swipemenu2

import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var swipeController: SwipeController
    lateinit var playersDataAdapter: PlayersDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking {
            setPlayersDataAdapter()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )

        recyclerView.adapter = playersDataAdapter

        swipeController = SwipeController(object : SwipeControllerActions {
            override fun onLeftClicked(position: Int) {
                Toast.makeText(
                        applicationContext,
                        "clicked on left button",
                        Toast.LENGTH_SHORT
                ).show()
            }

            override fun onRightClicked(position: Int) {
                playersDataAdapter.players.removeAt(position)
                playersDataAdapter.notifyItemRemoved(position)
                playersDataAdapter.notifyItemRangeChanged(position, playersDataAdapter.itemCount)

            }
        })

        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
                //super.onDraw(c, parent, state)
            }
        })

    }

    private fun setPlayersDataAdapter() {
        var players = mutableListOf<Player>()

        try {
            val inputStream = InputStreamReader(assets.open("players_20.csv"))
            val reader = BufferedReader(inputStream)
            reader.readLine()
            var line : String? = null
            while({line = reader.readLine(); line}() != null){
                val st = line!!.split(",")
                val newName = st[3]
                val newNationality = st[8]
                val newClub = st[9]
                val newRating = st[10].toInt()

                val newAge = st[4].toInt()

                val player = Player(newName, newNationality, newClub, newRating, newAge)
                players.add(player)
            }

            Log.d("MainActivity", "players size =${players.size}")

        } catch (e : IOException){
            Log.d("MainActivity", e.toString())
        }

        playersDataAdapter = PlayersDataAdapter(players)
    }
}
