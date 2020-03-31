package com.mcarving.swipemenu2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayersDataAdapter (
        val players : MutableList<Player>
) : RecyclerView.Adapter<PlayersDataAdapter.PlayerViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    inner class PlayerViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var name : TextView? = view.findViewById(R.id.name)
        val nationality : TextView? = view.findViewById(R.id.nationality)
        val club : TextView? = view.findViewById(R.id.club)
        val rating : TextView? = view.findViewById(R.id.rating)
        val age : TextView? = view.findViewById(R.id.age)

        fun bind(player : Player){
            name?.text = player.name
            nationality?.text = player.nationality
            club?.text = player.club
            rating?.text = player.rating.toString()
            age?.text = player.age.toString()
        }

    }
}