package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import at.shockbytes.dante.core.DanteApp
import at.shockbytes.dante.dagger.AppComponent

/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 */

abstract class BaseDialogFragment: DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    abstract fun injectToGraph(appComponent: AppComponent)

}