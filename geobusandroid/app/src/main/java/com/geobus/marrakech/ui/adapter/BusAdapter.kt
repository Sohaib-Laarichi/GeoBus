package com.geobus.marrakech.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.geobus.marrakech.R
import com.geobus.marrakech.model.BusPosition

/**
 * Adaptateur pour la liste des bus
 */
class BusAdapter(
    private val onBusClick: (BusPosition) -> Unit
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    private var buses = listOf<BusPosition>()

    class BusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBusLine: TextView = itemView.findViewById(R.id.tvBusLine)
        val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tvArrivalTime)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]

        holder.tvBusLine.text = bus.ligne
        holder.tvDestination.text = bus.getDisplayDestination() // Utiliser la nouvelle méthode

        // Affichage du temps d'arrivée avec vos couleurs existantes
        when {
            bus.minutesUntilArrival == null -> {
                holder.tvArrivalTime.text = "Temps indisponible"
                holder.tvArrivalTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.secondary_text)
                )
            }
            bus.minutesUntilArrival!! <= 1 -> {
                holder.tvArrivalTime.text = "Maintenant"
                holder.tvArrivalTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.success)
                )
            }
            bus.minutesUntilArrival!! <= 5 -> {
                holder.tvArrivalTime.text = "${bus.minutesUntilArrival} min"
                holder.tvArrivalTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.warning)
                )
            }
            else -> {
                holder.tvArrivalTime.text = "${bus.minutesUntilArrival} min"
                holder.tvArrivalTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.primary_text)
                )
            }
        }

        // Affichage de la distance
        bus.distanceToStop?.let { distance ->
            val distanceText = when {
                distance < 1000 -> "${distance.toInt()} m"
                else -> "${String.format("%.1f", distance / 1000)} km"
            }
            holder.tvDistance.text = distanceText
            holder.tvDistance.visibility = View.VISIBLE
        } ?: run {
            holder.tvDistance.visibility = View.GONE
        }

        // Clic sur l'élément
        holder.itemView.setOnClickListener {
            onBusClick(bus)
        }
    }

    override fun getItemCount(): Int = buses.size

    /**
     * Met à jour la liste des bus
     */
    fun updateBuses(newBuses: List<BusPosition>) {
        buses = newBuses
        notifyDataSetChanged()
    }
}