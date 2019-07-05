package at.shockbytes.dante.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import androidx.annotation.ColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import at.shockbytes.dante.signin.DanteUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.leinardi.android.speeddial.SpeedDialView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
fun String.colored(@ColorInt color: Int): SpannableString {
    val spannable = SpannableString(this)
    spannable.setSpan(ForegroundColorSpan(color),
            0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
}

fun String.removeBrackets(): String {
    return this.replace("(", "")
            .replace(")", "")
}

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

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

fun View.setVisible(isVisible: Boolean) {
    val visibility = if (isVisible) View.VISIBLE else View.GONE
    this.visibility = visibility
}

fun Double.roundDouble(digits: Int): Double {

    if (this == 0.0 || digits < 0 || this == Double.POSITIVE_INFINITY || this.isNaN() || this == Double.NEGATIVE_INFINITY) {
        return 0.00
    }

    return BigDecimal(this).setScale(digits, RoundingMode.HALF_UP).toDouble()
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun Fragment.isPortrait(): Boolean {
    return context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Activity.isPortrait(): Boolean {
    return resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Drawable.toBitmap(): Bitmap {

    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}
