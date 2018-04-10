package liang.lollipop.lqr.encode

import com.google.zxing.common.BitMatrix

/**
 * Created by lollipop on 2018/3/16.
 * @author Lollipop
 * 一个包装的矩阵，因为在二维码缩放模式下，
 * 需要标记清除允许透明的部分，黑色部分，白色部分
 */
class LBitMatrix(val width: Int,val height: Int) {

    constructor(dimension: Int): this(dimension,dimension)

    private val nullableMatrix = BitMatrix(width,height)
    private val blackMatrix = BitMatrix(width,height)

    companion object {

        fun copyOf(bitMatrix: BitMatrix): LBitMatrix {
            val lqrMatrix = LBitMatrix(bitMatrix.width, bitMatrix.height)
            for(width in 0 until bitMatrix.width){
                for(height in 0 until bitMatrix.height){

                    if(bitMatrix.get(width,height)){
                        lqrMatrix.set(width,height)
                    }

                }
            }
            return lqrMatrix
        }

    }

    fun isBlack(x: Int, y: Int): Boolean {
        return blackMatrix.get(x, y)
    }

    fun isNotNull(x: Int, y: Int): Boolean{
        return nullableMatrix.get(x, y)
    }

    fun setRegion(left: Int, top: Int, width: Int, height: Int,type: TYPE = TYPE.BLACK){
        when(type){

            TYPE.BLACK -> {
                nullableMatrix.setRegion(left, top, width, height)
                blackMatrix.setRegion(left, top, width, height)
            }

            TYPE.WHITE -> {
                nullableMatrix.setRegion(left, top, width, height)
            }

            else -> {}

        }
    }

    fun setRegionOneInNine(left: Int, top: Int, width: Int, height: Int,type: TYPE){

        val widthMini = width / 3.0
        val heightMini = height / 3.0

        val x: Int = (left + ((width - widthMini) / 2)).toInt().min()
        val y: Int = (top + ((height - heightMini) / 2)).toInt().min()
        var w: Int = (width - (heightMini * 2)).toInt().min()
        val h: Int = (height - (heightMini * 2)).toInt().min()

        setRegion(x, y, w, h,type)

    }

    private fun Int.min(): Int{
        return if(this < 1){1}else{this}
    }

    fun setNullable(x: Int,y: Int){
        nullableMatrix.set(x, y)
    }

    fun setBlack(x: Int,y: Int){
        blackMatrix.set(x, y)
    }

    fun set(x: Int,y: Int){
        setNullable(x, y)
        setBlack(x, y)
    }

    fun clear(){
        nullableMatrix.clear()
        blackMatrix.clear()
    }

    fun bitMatrix(): BitMatrix{
        return blackMatrix
    }

    enum class TYPE{
        NULL,BLACK,WHITE
    }

}