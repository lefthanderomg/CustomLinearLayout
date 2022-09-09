package andrey.murzin.com.customlinearlayout.view.custom

import andrey.murzin.com.customlinearlayout.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isGone
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.min
import kotlin.properties.Delegates

internal class CustomLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    @ColorInt
    private var borderColor: Int = 0
    private var orientation: Int = HORIZONTAL
    private var radius = 0f
    private var borderSize: Float = 0f
    private val rectF = RectF()
    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.CustomLinearLayout) {
            orientation = getInt(R.styleable.CustomLinearLayout_orientation, HORIZONTAL)
            radius = getDimension(R.styleable.CustomLinearLayout_radius, 0f)
            borderColor = getColor(R.styleable.CustomLinearLayout_borderColor, 0)
            borderSize = getDimension(R.styleable.CustomLinearLayout_borderSize, 0f)
            paint.color = borderColor
            paint.strokeWidth = borderSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        val visibleChildren = children.filter { !it.isGone }
        visibleChildren.forEach { child ->
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
        }
        val childParameters = findChildParameters(visibleChildren)

        if (orientation == HORIZONTAL) {
            measureHorizontal(
                childParameters = childParameters,
                widthMode = widthMode,
                heightMode = heightMode,
                widthSize = widthSize,
                heightSize = heightSize,
                heightMeasureSpec = heightMeasureSpec,
                visibleChildren = visibleChildren
            )
        } else {
            measureVertical(
                childParameters = childParameters,
                widthMode = widthMode,
                heightMode = heightMode,
                widthSize = widthSize,
                heightSize = heightSize,
                widthMeasureSpec = widthMeasureSpec,
                visibleChildren = visibleChildren
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val visibleChildren = children.filter { !it.isGone }
        if (orientation == HORIZONTAL) {
            layoutHorizontal(visibleChildren)
        } else {
            layoutVertical(visibleChildren)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet) = MarginLayoutParams(context, attrs)

    override fun dispatchDraw(canvas: Canvas) {
        if (radius > 0) {
            path.rewind()
            children.forEach { child ->
                if (radius > 0) {
                    rectF.set(child.x, child.y, child.x + child.measuredWidth, child.y + child.measuredHeight)
                    path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
                }
            }
            canvas.clipPath(path)
        }

        super.dispatchDraw(canvas)

        if (borderColor != 0 && borderSize != 0f) {
            children.forEach { child ->
                rectF.set(
                    child.x,
                    child.y,
                    child.x + child.measuredWidth,
                    child.y + child.measuredHeight
                )

                canvas.drawRoundRect(rectF, radius, radius, paint)
            }
            canvas.clipPath(path)
        }
    }

    private fun layoutVertical(visibleChildren: Sequence<View>) {
        var offset = paddingTop

        visibleChildren.forEach { child ->
            val layoutParams = child.layoutParams as MarginLayoutParams

            child.layout(
                layoutParams.leftMargin + paddingLeft,
                offset + layoutParams.topMargin,
                child.measuredWidth + layoutParams.leftMargin + paddingLeft,
                child.measuredHeight + offset + layoutParams.topMargin
            )
            offset += child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
        }
    }

    private fun layoutHorizontal(visibleChildren: Sequence<View>) {
        var offset = paddingStart

        visibleChildren.forEach { child ->
            val layoutParams = child.layoutParams as MarginLayoutParams

            child.layout(
                offset + layoutParams.leftMargin,
                layoutParams.topMargin + paddingTop,
                child.measuredWidth + offset + layoutParams.leftMargin,
                child.measuredHeight + layoutParams.topMargin + paddingTop
            )
            offset += child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
        }
    }

    private fun measureVertical(
        widthMode: Int,
        heightMode: Int,
        widthSize: Int,
        heightSize: Int,
        widthMeasureSpec: Int,
        visibleChildren: Sequence<View>,
        childParameters: ChildParamsWithMargin
    ) {
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> min(childParameters.maxChildrenWidth, widthSize)
            else -> error("Unreachable")
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> min(childParameters.sumChildrenHeight, heightSize)
            else -> error("Unreachable")
        }
        var delta = height - childParameters.sumChildrenHeight
        var weightSum = childCount

        visibleChildren.forEach { child ->
            val layoutParams = child.layoutParams as MarginLayoutParams
            val shape = delta / weightSum
            val childrenHeight = child.measuredHeight + shape
            val childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight + layoutParams.leftMargin + layoutParams.rightMargin,
                layoutParams.width
            )

            child.measure(
                childWidthMeasureSpec,
                MeasureSpec.makeMeasureSpec(childrenHeight, MeasureSpec.EXACTLY),
            )

            delta -= shape
            weightSum--
        }

        setMeasuredDimension(
            width + paddingLeft + paddingRight,
            height + paddingTop + paddingBottom
        )
    }

    private fun measureHorizontal(
        childParameters: ChildParamsWithMargin,
        widthMode: Int,
        heightMode: Int,
        widthSize: Int,
        heightSize: Int,
        heightMeasureSpec: Int,
        visibleChildren: Sequence<View>,
    ) {
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> min(childParameters.sumChildrenWidth, widthSize)
            else -> error("Unreachable")
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> min(childParameters.maxChildHeight, heightSize)
            else -> error("Unreachable")
        }
        var delta = width - childParameters.sumChildrenWidth
        var weightSum = childCount

        visibleChildren.forEach { child ->
            val layoutParams = child.layoutParams as MarginLayoutParams
            val shape = delta / weightSum
            val childrenWidth = child.measuredWidth + shape
            val childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingBottom + paddingTop + layoutParams.topMargin + layoutParams.bottomMargin,
                layoutParams.height
            )

            child.measure(
                MeasureSpec.makeMeasureSpec(childrenWidth, MeasureSpec.EXACTLY),
                childHeightMeasureSpec
            )

            delta -= shape
            weightSum--
        }

        setMeasuredDimension(
            width + paddingLeft + paddingRight,
            height + paddingTop + paddingBottom
        )
    }

    private fun findChildParameters(visibleChildren: Sequence<View>): ChildParamsWithMargin {
        var sumChildrenWidth = 0
        var sumChildrenHeight = 0
        var maxChildrenWidth = 0
        var maxChildHeight = 0

        visibleChildren.forEach { child ->
            val layoutParam = child.layoutParams as MarginLayoutParams
            val horizontalMargin = layoutParam.leftMargin + layoutParam.rightMargin
            val verticalMargin = layoutParam.topMargin + layoutParam.bottomMargin

            sumChildrenWidth += child.measuredWidth + horizontalMargin
            sumChildrenHeight += child.measuredHeight + verticalMargin

            if (maxChildrenWidth < child.measuredWidth) {
                maxChildrenWidth = child.measuredWidth + horizontalMargin
            }
            if (maxChildHeight < child.measuredHeight) {
                maxChildHeight = child.measuredHeight + verticalMargin
            }
        }

        return ChildParamsWithMargin(
            sumChildrenWidth,
            sumChildrenHeight,
            maxChildrenWidth,
            maxChildHeight,
        )
    }

    companion object {
        private const val HORIZONTAL = 0
    }

    private class ChildParamsWithMargin(
        val sumChildrenWidth: Int,
        val sumChildrenHeight: Int,
        val maxChildrenWidth: Int,
        val maxChildHeight: Int,
    )
}
