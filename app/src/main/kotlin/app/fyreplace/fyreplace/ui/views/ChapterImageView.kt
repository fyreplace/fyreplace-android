package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import app.fyreplace.protos.Image

class ChapterImageView : AppCompatImageView {
    private lateinit var image: Image

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setChapterImage(image: Image) {
        this.image = image
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightSpec: Int = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            val height = MeasureSpec.getSize(widthMeasureSpec) * image.height / image.width
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }

        super.onMeasure(widthMeasureSpec, newHeightSpec)
    }
}
