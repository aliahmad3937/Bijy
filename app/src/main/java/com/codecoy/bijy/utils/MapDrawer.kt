package com.codecoy.bijy.utils

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions




class MapDrawer(val map: GoogleMap) {
    private val EARTH_RADIUS = 6371000

    private fun getPoint(center: LatLng, radius: Int, angle: Double): LatLng {
        // Get the coordinates of a circle point at the given angle
        val east = radius * Math.cos(angle)
        val north = radius * Math.sin(angle)
        val cLat: Double = center.latitude
        val cLng: Double = center.longitude
        val latRadius = EARTH_RADIUS * Math.cos(cLat / 180 * Math.PI)
        val newLat = cLat + north / EARTH_RADIUS / Math.PI * 180
        val newLng = cLng + east / latRadius / Math.PI * 180
        return LatLng(newLat, newLng)
    }

    fun drawCircle(center: LatLng, radius: Int): Polygon? {
        // Clear the map to remove the previous circle
        map.clear()
        // Generate the points
        val points: MutableList<LatLng> = ArrayList<LatLng>()
        val totalPonts = 30 // number of corners of the pseudo-circle
        for (i in 0 until totalPonts) {
            points.add(getPoint(center, radius, i * 2 * Math.PI / totalPonts))
        }
        // Create and return the polygon
        return map.addPolygon(
            PolygonOptions().addAll(points).strokeWidth(2f).strokeColor(0x700a420b)
        )
    }
}