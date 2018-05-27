package com.lxdnz.nz.ariaorienteering.fragments

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.maps.android.clustering.ClusterManager

import com.lxdnz.nz.ariaorienteering.R
import com.lxdnz.nz.ariaorienteering.customisers.CustomClusterRenderer
import com.lxdnz.nz.ariaorienteering.customisers.StringClusterItem
import com.lxdnz.nz.ariaorienteering.dialogs.AddMarkerDialog
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.User
import com.lxdnz.nz.ariaorienteering.model.types.MarkerStatus
import com.lxdnz.nz.ariaorienteering.services.LocationService
import com.lxdnz.nz.ariaorienteering.tasks.AdminTask
import com.lxdnz.nz.ariaorienteering.viewmodel.UserViewModel

import kotlinx.android.synthetic.main.fragment_map.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    val TAG: String = "MapFragment"
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    val RADIUS = 5.0f
    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    lateinit var locationService: LocationService
    lateinit var mContext: Context
    lateinit var activity: Activity
    lateinit var intent: Intent
    lateinit var mClusterManager1: ClusterManager<StringClusterItem>
    lateinit var mClusterManager2: ClusterManager<StringClusterItem>
    lateinit var mClusterManager3: ClusterManager<StringClusterItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
        activity = this.requireActivity()
        mContext = this.requireContext()
        locationService = LocationService(mContext)
        intent = Intent(activity, LocationService::class.java)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        val sMapFragment = SupportMapFragment.newInstance()
        mMapView = rootView.findViewById(R.id.mapView) as MapView
        mMapView.getMapAsync(this)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(getActivity()!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        sMapFragment.getMapAsync(this)

        /**
         * Keep track of the current user with the View Model and update UI accordingly
         */
        val userViewModel: UserViewModel = ViewModelProviders
                .of(this)
                .get(UserViewModel::class.java)
        val userLiveData = userViewModel.getLiveUserData()
        userLiveData.observe(this, Observer { user: User? ->
            if (user != null) {
                adminUpdateUI(user)
            }
        })

        return rootView
    }

    /**
     * Updates the UI, if ADMIN then set add marker option, else move to refresh/add course markers to map
     */
    private fun adminUpdateUI(user: User) {
        adminMarker.visibility = View.GONE

        AdminTask.checkAdmin(user.firstName).addOnCompleteListener { task ->
            if (task.result) {
                Toast.makeText(mContext, "Logged as admin", Toast.LENGTH_SHORT).show()
                // show add marker button and set on click
                adminMarker.visibility = View.VISIBLE
                adminMarker.setOnClickListener(View.OnClickListener { it -> addMarker() })
                // add adminOnMarkerClick to remove marker

            } else {
                // do this if not admin - add onMarkerClick - Target Marker
                if (user.courseObject != null)
                    refreshMarkers(user)
            }
        }

    }

    /**
     * Admin Adds a marker to the map
     */
    private fun addMarker() {
        val markerDialog = AddMarkerDialog()
        markerDialog.show(fragmentManager, "AddMarkerDialog")
    }

    private fun setUpMap() {
        try {
            Log.i(TAG, "trying to make Map")
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    googleMap.isMyLocationEnabled = true
                    googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.uiSettings.isCompassEnabled = true
                } else {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION)
                }
            } else {
                googleMap.isMyLocationEnabled = true
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.uiSettings.isCompassEnabled = true
            }
            // Start the GPS service if it is not running
            if (!locationService.isServiceRunning) {
                Log.i(TAG, "Trying Service")
                activity.startService(intent)
                locationService = LocationService(activity)
            } else {
                locationService = LocationService(activity)
            }

            // Get current location and add initial marker
            if (locationService.hasLocation()) {
                val lat = locationService.getLocation()!!.latitude
                val lon = locationService.getLocation()!!.longitude

                googleMap.addMarker(MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title("You are here!")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16.0f))
            } else if (locationService.hasPossiblyStaleLocation()) {
                val lat = locationService.getPossiblyStaleLocation()!!.latitude
                val lon = locationService.getPossiblyStaleLocation()!!.longitude

                googleMap.addMarker(MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title("You were last known to be here!")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16.0f))
            } else {
                // can't get location
                // GPS or Network is disabled
                locationService.showSettingsAlert()
            }
            mClusterManager1 = ClusterManager(mContext, googleMap)
            mClusterManager2 = ClusterManager(mContext, googleMap)
            mClusterManager3 = ClusterManager(mContext, googleMap)
            setClusterManger(mClusterManager1, MarkerStatus.NOT_FOUND)
            setClusterManger(mClusterManager2, MarkerStatus.FOUND)
            setClusterManger(mClusterManager3, MarkerStatus.TARGET)

        } catch (se: SecurityException) {
            // dialog for activating locations here
            Log.i(TAG, "Security Error")
        }

    }

    /**
     * Sets up Cluster Managers
     * Note - we could add functionality here with extra CustomisedCluster Renderers
     */
    private fun setClusterManger(clusterManager: ClusterManager<StringClusterItem>,
                                 status: MarkerStatus) {
        googleMap.setOnCameraChangeListener(clusterManager)
        val renderer = CustomClusterRenderer(mContext, googleMap, clusterManager, status)
        clusterManager.setRenderer(renderer)
    }

    /**
     * Standard User has course markers displayed (if they are on a current course)
     */
    private fun refreshMarkers(user: User) {

        Log.i(TAG, "refreshing Markers")
        // TODO: add onClick function to target Marker

        val courseMarkers = user.courseObject!!.markers

        val notFoundMarkers = mutableListOf<Marker>()
        val foundMarkers = mutableListOf<Marker>()
        val targetMarker = mutableListOf<Marker>()

        courseMarkers.forEach({ marker ->

            if (marker.status.equals(MarkerStatus.NOT_FOUND)) {
                notFoundMarkers.add(marker)
            } else if (marker.status.equals(MarkerStatus.FOUND)) {
                foundMarkers.add(marker)
            } else if (marker.status.equals(MarkerStatus.TARGET)) {
                targetMarker.add(marker)
            }
        })

        clusterManagement(mClusterManager1, notFoundMarkers)
        clusterManagement(mClusterManager2, foundMarkers)
        clusterManagement(mClusterManager3, targetMarker)

        // get current Location and test the markers
        val currentLocation = locationService.getLocation()
        if (currentLocation != null) {
            testProximity(targetMarker, currentLocation)
            testProximity(notFoundMarkers, currentLocation)
        }

    }

    /**
     * Check the distance to all un-found markers
     */
    private fun testProximity(markers: MutableList<Marker>, currentLocation: Location) {

        val updateMarker = mutableListOf<Marker>()
        markers.forEach { marker ->

            Log.d(TAG, "Location test:" + currentLocation.latitude + " -> " + marker.lat)
            val target = Location("target")
            target.longitude = marker.lon
            target.latitude = marker.lat
            val distance = currentLocation.distanceTo(target)
            Log.d(TAG, "distance: " + distance)
            if(distance < RADIUS) {
                Toast.makeText(mContext, "You've found a marker", Toast.LENGTH_SHORT).show()
                updateMarker.add(marker)
            }
        }
        // update markers within the distance to found
        if (updateMarker.size > 0) {
            updateMarker.forEach { marker ->
                User.findMarker(marker)
            }
        }
    }

    /**
     * private function to manage many clusters
     */
    private fun clusterManagement(mClusterManager: ClusterManager<StringClusterItem>,
                                  markerList: MutableList<Marker>) {
        //remove the markers
        mClusterManager.markerCollection.markers.forEach({marker -> marker.remove()})
        mClusterManager.clearItems()
        // add to cluster if markers exist in list
        if (markerList.isNotEmpty()) {
            markerList.forEach({ marker ->
                mClusterManager
                        .addItem(StringClusterItem(marker.id.toString(),
                                LatLng(marker.lat, marker.lon)))
            })
        }
        // cluster to map
        mClusterManager.cluster()
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            Log.i(TAG, "Map onAttach")
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        mMapView.onResume()
        Log.i(TAG, "Map onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onMapReady(p0: GoogleMap?) {
        Log.i(TAG, "Reached OnMapReady")
        googleMap = p0 as GoogleMap
        setUpMap()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MapFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

}
