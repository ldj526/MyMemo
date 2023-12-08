package eu.tutorials.mymemo.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var drawPath: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    var brushSize: Float = 3.0.toFloat()         // 캔버스에 그릴 브러쉬 크기
    private var color = Color.BLACK     // 획의 색상 설정
    private var isDrawingEnabled = false

    private var canvas: Canvas? = null
    private val paths = ArrayList<CustomPath>()

    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.style = Paint.Style.STROKE         // 스트로크 스타일
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    // 그릴 때 호출되는 메서드
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        for (path in paths) {      // 그린 내용들을 저장하는 기능
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }
        if (!drawPath!!.isEmpty) {
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    // 터치가 발생했을 때 호출되는 메서드
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isDrawingEnabled) {
            return false
        }

        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = color
                drawPath!!.brushThickness = brushSize

                drawPath!!.reset()
                if (touchX != null) {       // 시작점을 x, y 로 설정한다.
                    if (touchY != null) {
                        drawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {    // 마지막 지점까지 선 추가
                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)     // 경로 추가
                drawPath = CustomPath(color, brushSize)
            }

            else -> return false
        }
        invalidate()

        return true
    }

    // 그리기 활성화
    fun enableDrawing() {
        isDrawingEnabled = true
    }

    // 그리기 비활성화
    fun disableDrawing() {
        isDrawingEnabled = false
    }

    // 브러쉬의 크기변경하는 메서드
    fun setSizeForBrush(newSize: Float) {
        brushSize = convertDpToPx(newSize)  // 해상도 별로 px 값이 다르므로 px로 변환
        drawPaint!!.strokeWidth = brushSize
        brushSize = convertPxToDp(brushSize)    // SeekBar의 progress 값을 위해 변환
    }

    // dp 값을 px로 변환
    private fun convertDpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp, resources.displayMetrics
        )
    }

    // px 값을 dp로 변환
    private fun convertPxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    // 색상 변경
    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        drawPaint!!.color = color
    }

    // 실행취소
    fun onClickUndo() {
        if (paths.size > 0) {
            mUndoPaths.add(paths.removeAt(paths.size - 1))
            invalidate()    //  // Invalidate the whole view. If the view is visible
        }
    }

    // 색상, 획 크기를 위한 inner class
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}