package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.arguments.argumentNullable
import kotlinx.android.synthetic.main.fragment_reading_goal_picker.*

class ReadingGoalPickerFragment : BaseFragment() {

    interface OnReadingGoalPickedListener {

        fun onGoalPicked(goal: Int, goalType: ReadingGoalType)

        fun onDelete(goalType: ReadingGoalType)
    }

    override val layoutId: Int = R.layout.fragment_reading_goal_picker

    private var type: ReadingGoalType by argument()
    private var initialValue: Int? by argumentNullable()

    private var onReadingGoalPickedListener: OnReadingGoalPickedListener? = null

    override fun setupViews() {
        setupLottieAnimation()
        setupCloseListeners()
        setupSlider()
        setupCallbackListeners()
        setupType()

        initialValue?.toFloat()?.let(slider_reading_goal::setValue)
    }

    private fun setupCallbackListeners() {
        btn_reading_goal_delete.setOnClickListener {
            onReadingGoalPickedListener?.onDelete(type)
            closeFragment()
        }

        btn_reading_goal_apply.setOnClickListener {
            onReadingGoalPickedListener?.onGoalPicked(slider_reading_goal.value.toInt(), type)
            closeFragment()
        }
    }

    private fun setupLottieAnimation() {
        lav_reading_goal.apply {
            setMinFrame(MIN_FRAME)
            setMaxFrame(MAX_FRAME)
        }
    }

    private fun setupCloseListeners() {
        btn_reading_goal_close.setOnClickListener {
            closeFragment()
        }
        layout_reading_goal_picker.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    private fun setupSlider() {
        slider_reading_goal.apply {

            valueTo = type.sliderValueTo
            valueFrom = type.sliderValueFrom
            stepSize = type.sliderStepSize

            addOnChangeListener { slider, value, _ ->
                computeLottieFrame(slider.valueFrom, slider.valueTo, value)
                updateLevel(slider.valueFrom, slider.valueTo, value)
                updateGoalLabel(value.toInt())
            }
        }
    }

    private fun computeLottieFrame(minValue: Float, maxValue: Float, value: Float) {

        val valueP = ((100f / (maxValue - minValue)) * value).div(100)
        val frame = ((MAX_FRAME - MIN_FRAME) * valueP).toInt() + MIN_FRAME

        lav_reading_goal.frame = frame
    }

    private fun updateLevel(min: Float, max: Float, value: Float) {

        val diff = max - min

        val levelRes = when {
            value > diff.times(0.8) -> R.string.reading_goal_level_3
            value > diff.times(0.6) -> R.string.reading_goal_level_2
            value > diff.times(0.4) -> R.string.reading_goal_level_1
            else -> R.string.reading_goal_level_0
        }

        tv_reading_goal_level.setText(levelRes)
    }

    private fun setupType() {
        tv_fragment_reading_goal_header.setText(type.title)
    }

    private fun updateGoalLabel(value: Int) {
        tv_reading_goal_label.text = getString(type.labelTemplate, value, getString(R.string.month))
    }

    fun setOnReadingGoalPickedListener(
        listener: OnReadingGoalPickedListener
    ): ReadingGoalPickerFragment {
        return apply {
            onReadingGoalPickedListener = listener
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    companion object {

        private const val MIN_FRAME = 18
        private const val MAX_FRAME = 28

        fun newInstance(
            initialValue: Int? = null,
            type: ReadingGoalType
        ): ReadingGoalPickerFragment {
            return ReadingGoalPickerFragment().apply {
                this.initialValue = initialValue
                this.type = type
            }
        }
    }
}