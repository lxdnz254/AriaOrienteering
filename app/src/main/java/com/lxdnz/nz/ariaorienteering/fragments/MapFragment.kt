package com.lxdnz.nz.ariaorienteering.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.lxdnz.nz.ariaorienteering.R
import com.lxdnz.nz.ariaorienteering.services.LocationService


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

    val TAG:String = "MapFragment"
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    lateinit var locationService: LocationService
    lateinit var mContext: Context
    lateinit var activity: Activity
    lateinit var intent: Intent


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
        val rootView =  inflater.inflate(R.layout.fragment_map, container, false)

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

        return rootView
    }

    private fun setUpMap() {
        try {
            Log.i(TAG, "trying to make Map")
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    googleMap!!.isMyLocationEnabled = true
                    googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.uiSettings.isCompassEnabled = true
                } else {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION)
                }
            } else {
                googleMap!!.isMyLocationEnabled = true
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.uiSettings.isCompassEnabled = true
            }
            // Start the GPS service if it is not running
            if (!locationService.isServiceRunning) {
                Log.i(TAG, "Trying Service")
                activity.startService(intent)
                locationService = LocationService(activity)
            }
            else
            {
                locationService = LocationService(activity)
            }

            // Get current location and add initial marker
            if (locationService.hasLocation()) {
                val lat = locationService.getLocation()!!.latitude
                val lon = locationService.getLocation()!!.longitude

                googleMap.addMarker(MarkerOptions().position(LatLng(lat, lon)).title("You are here!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16.0f))
            }
            else if (locationService.hasPossiblyStaleLocation()) {
                val lat = locationService.getPossiblyStaleLocation()!!.latitude
                val lon = locationService.getPossiblyStaleLocation()!!.longitude

                googleMap.addMarker(MarkerOptions().position(LatLng(lat, lon)).title("You were last known to be here!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16.0f))
            }
            else
            {
                // can't get location
                // GPS or Network is disabled
                locationService.showSettingsAlert()
            }

            // add any onClick functions etc

        } catch (se: SecurityException) {
            // dialog for activating locations here
            Log.i(TAG, "Security Error")
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        mMapView.onResume()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
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
