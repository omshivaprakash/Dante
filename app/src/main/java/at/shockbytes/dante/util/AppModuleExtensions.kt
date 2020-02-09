package at.shockbytes.dante.util

import android.os.Handler
import at.shockbytes.dante.signin.DanteUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.leinardi.android.speeddial.SpeedDialView

fun FloatingActionButton.toggle(millis: Long = 300) {
    this.hide()
    Handler().postDelayed({ this.show() }, millis)
}

fun SpeedDialView.toggleVisibility(millis: Long = 300) {
    this.hide()
    Handler().postDelayed({ this.show() }, millis)
}

fun GoogleSignInAccount.toDanteUser(): DanteUser {
    return DanteUser(
        this.givenName,
        this.displayName,
        this.email,
        this.photoUrl,
        "google",
        this.idToken
    )
}