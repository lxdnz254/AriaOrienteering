package com.lxdnz.nz.ariaorienteering.customisers

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.lxdnz.nz.ariaorienteering.model.types.MarkerStatus

class CustomClusterRenderer(context: Context,
                            map: GoogleMap,
                            clusterManager: ClusterManager<StringClusterItem>,
                            var found: MarkerStatus) :
        DefaultClusterRenderer<StringClusterItem>(context, map, clusterManager) {

    var mClusterIconGenerator: IconGenerator
    var customContext: Context

    init {
        customContext = context
        mClusterIconGenerator = IconGenerator(customContext.getApplicationContext())
    }

    override fun onBeforeClusterItemRendered(item: StringClusterItem,
                                             markerOptions: MarkerOptions) {
        if (found == MarkerStatus.NOT_FOUND) {
            val markerDescriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            markerOptions.icon(markerDescriptor).snippet(item.title)
        } else if (found == MarkerStatus.FOUND) {
            val markerDescriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            markerOptions.icon(markerDescriptor).snippet(item.title + " Located")
        } else if (found == MarkerStatus.TARGET) {
            val markerDescriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            markerOptions.icon(markerDescriptor).snippet(item.title + " Targeting")
        }
    }

    //TODO: possibly add A Cluster Icon


}