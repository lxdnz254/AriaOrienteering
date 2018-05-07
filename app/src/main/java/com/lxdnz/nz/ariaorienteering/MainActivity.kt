package com.lxdnz.nz.ariaorienteering

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.lxdnz.nz.ariaorienteering.fragments.HelpFragment
import com.lxdnz.nz.ariaorienteering.fragments.HomeFragment
import com.lxdnz.nz.ariaorienteering.fragments.MapFragment
import com.lxdnz.nz.ariaorienteering.model.User
import com.lxdnz.nz.ariaorienteering.services.LocationService

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener, HelpFragment.OnFragmentInteractionListener {

    private var LOGGED_IN = "Logged Out"
    lateinit private var saveState: Bundle
    lateinit var locationService: LocationService
    val mAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences: SharedPreferences
    val MY_PREFS = "MyPrefs"
    val ACTIVE = "active"


    // required for callback implementation from Fragments
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        // Login Intent
        val i = Intent(this, LoginActivity::class.java)
        // Button Listener

        fab.setOnClickListener { view ->
            startActivity(i)
        }
        sharedPreferences = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)

        // check if logged in : Launch Login Activity if not
        if (mAuth.currentUser == null){
            startActivity(i)
        } else {
            //TODO: Update the LOGGED_IN text on the UI
            LOGGED_IN = "Logged In"
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("Main", "OnStart()")


        if (mAuth.currentUser != null) {
            Log.i("Main", "Firebase User active")
            if (sharedPreferences.contains(ACTIVE)) {
                if (!sharedPreferences.getBoolean(ACTIVE, false)){
                    task { User.activate(mAuth.currentUser!!.uid) } then {
                        it -> it.addOnCompleteListener {activateLocalUser()}
                    }
                }
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {

        locationService = LocationService(this.applicationContext)
        deactivateLocalUser()

        if (locationService.isServiceRunning) {
            locationService.stop()

        }
        this.stopService(Intent(this, LocationService::class.java))
        if (mAuth.currentUser != null) {

            User.deactivate(mAuth.currentUser!!.uid)
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        Log.i("Main", "onResume()")
        locationService = LocationService(this.applicationContext)


        if (!locationService.isServiceRunning) {
            this.startService(Intent(this, LocationService::class.java))
        }

    }

    fun deactivateLocalUser() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ACTIVE, false)
        editor.apply()
    }

    fun activateLocalUser() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ACTIVE, true)
        editor.apply()
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? = when (position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1)
            0 -> HomeFragment.newInstance(position.toString(),LOGGED_IN)
            // Other Fragments Here
            1 -> MapFragment.newInstance(position.toString(), "")
            2 -> HelpFragment.newInstance(position.toString(), "")
            else -> null
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            rootView.section_label.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
