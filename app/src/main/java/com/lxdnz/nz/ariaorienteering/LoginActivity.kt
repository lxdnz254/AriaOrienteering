package com.lxdnz.nz.ariaorienteering

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import android.os.PersistableBundle
import android.support.design.widget.TextInputLayout
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.lxdnz.nz.ariaorienteering.model.User



/**
 * A login screen that offers login via email/password to Firebase.
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "FirebaseGoogleSignIn"
    private val LOGGED_IN = "LOGGED_IN"
    private val REQUEST_CODE_SIGN_IN = 1234
    private val WEB_CLIENT_ID = BuildConfig.FirebaseWebClientKey

    private var saveState: Bundle? = null
    private var firstName = ""

    private var mAuth: FirebaseAuth? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setFinishOnTouchOutside(false)
        val firstNameWrapper: TextInputLayout = findViewById(R.id.firstNameWrapper)
        firstNameWrapper.hint = "First Name"
        saveState = savedInstanceState
        btn_sign_in.setOnClickListener(this)
        btn_sign_out.setOnClickListener(this)
        btn_disconnect.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth!!.currentUser
        Log.i(TAG, "Current User:" + currentUser?.uid )

        User.retrieve(currentUser?.uid).addOnCompleteListener { task -> updateUI(task.result) }


    }

    override fun onClick(view: View?) {
        val i = view!!.id

        when(i) {
            R.id.btn_sign_in -> signIn()
            R.id.btn_sign_out -> signOut()
            R.id.btn_disconnect -> revokeAccess()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent();
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // successful -> authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                // failed -> update UI
                updateUI(null)
                Toast.makeText(applicationContext, "SignIn: failed!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e(TAG, "firebaseAuthWithGoogle():" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.e(TAG, "signInWithCredential: Success!")
                        val user = mAuth!!.currentUser
                        // insert a user object here
                        val activeUser = User.create(user?.uid, user?.email, firstName, 0.0, 0.0 ,true)
                        updateUI(activeUser)
                        // return to main
                        saveState?.putBoolean(LOGGED_IN, true)
                        finish()
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential: Failed!", task.exception)
                        Toast.makeText(applicationContext, "Authentication failed!",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed():" + connectionResult);
        Toast.makeText(applicationContext, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private fun signIn() {
        if(firstNameWrapper.hasFocus()) {
            hideKeyboard()
        }
        firstName = firstNameWrapper.editText?.text.toString()
        if (firstName.isNotEmpty()) {
            doSignIn()
        } else {
            firstNameWrapper.error = "Enter First Name"
        }
    }

    private fun doSignIn() {
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }

    private fun signOut() {
        // sign out Firebase
        mAuth!!.signOut()
        // sign out Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback { updateUI(null) }
        saveState?.putBoolean(LOGGED_IN, false)
    }

    private fun revokeAccess() {
        // sign out Firebase
        mAuth!!.signOut()

        // revoke access Google
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback { updateUI(null) }
        saveState?.putBoolean(LOGGED_IN, false)
    }


    private fun updateUI(user: User?) {
        Log.i(TAG, "Retrieved user:" + user?.firstName)
        if (user != null) {
            tvStatus.text = "User name: " + user.firstName
            tvDetail.text = "User Logged In: "  + user.active

            firstNameWrapper.visibility = View.GONE
            btn_sign_in.visibility = View.GONE
            layout_sign_out_and_disconnect.visibility = View.VISIBLE
        } else {
            tvStatus.text = getString(R.string.signed_out)
            tvDetail.text = null

            firstNameWrapper.visibility = View.VISIBLE
            firstNameWrapper.isFocused
            btn_sign_in.visibility = View.VISIBLE
            layout_sign_out_and_disconnect.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val view: View = currentFocus
        view?.let {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromInputMethod(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
