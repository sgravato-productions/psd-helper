package com.psd_helper.backend

import com.psd_helper.drawable.Division
import com.psd_helper.spaceFill
import com.psd_helper.superScript
import com.psd_helper.zeroFill
import dev.federicocapece.drawzone.Group
import dev.federicocapece.drawzone.drawables.Text
import kotlin.math.pow

object Conversions {
    enum class Base{
        B2,
        B8,
        B10,
        B16,
        CA2;

        override fun toString(): String = name.removePrefix("B").toLowerCase().capitalize()

        private val toInt: Int = name.substring(name.indexOfFirst { it.isDigit() }).toInt()
        fun toInt():Int = toInt
    }


    fun convert(inputNum:String, inputBase:Base, outputBase:Base, canvas: Group, inputCa2:Int=0, outputCa2:Int=0) : String{
        /*quick convert to check results later : TODO: TURN THIS INTO A UNIT TEST!!!
        try{
            outputTextField.style = "-fx-text-fill: black;";
            outputTextField.text = inputTextField.text.toInt(inputBase).toString(outputBase).toUpperCase()
        }catch (ex:Exception){
            outputTextField.style = "-fx-text-fill: red;";
            outputTextField.text = "error"
        }
        */

        var canvas = canvas
        var inputNum = inputNum
        var inputBase = inputBase

        //if the base is the same there's nothing to convert
        if(inputBase == outputBase)
            return inputNum

        //if the output is in B10 i use the sum of positional numbers and that's it (this takes care of the CA2 too)
        if(outputBase == Base.B10){
            return toB10(inputNum, inputBase, canvas, inputCa2)
        }

        //if the input is a CA2 then i need to fix it by complementing before doing the conversion
        if(inputBase == Base.CA2){
            inputNum = complement(inputNum, inputCa2, canvas)
            inputBase = Base.B2
            canvas = canvas.group(canvas.y, canvas.bottom)
        }

        //doing the first conversion
        var output:String = when(inputBase){
            Base.B10 -> consecutiveDivisions(inputNum, outputBase, canvas)
            else -> quickB2xToB2x(inputNum, inputBase, outputBase, canvas)
        }

        //eventually converting to Ca2
        if(outputBase == Base.CA2){
            canvas = canvas.group(canvas.y, canvas.bottom)
            output = complement(output, outputCa2, canvas)
        }

        return output
    }

    private fun complement(inputB2: String, bitCount: Int, canvas:Group): String {
        TODO("Not yet implemented")
    }

    private fun consecutiveDivisions(inputNum: String, outputBase: Base, canvas: Group): String {
        val outputBase = outputBase.toInt()
        //write number
        var toDivide:Int = inputNum.toInt()
        val firstDivisionText = Text(toDivide.toString())
        canvas.add(firstDivisionText)

        var lastDivision: Division? = null
        val remainders = arrayListOf<Int>()
        while(toDivide != 0){
            //effective calculations for the return result
            remainders.add(toDivide % outputBase)
            toDivide /= outputBase

            val divisionStartingNumber: Text = lastDivision?.resultText ?: firstDivisionText
            lastDivision = Division(divisionStartingNumber, outputBase.toInt(), lastDivision ?: canvas)
        }


        return remainders.map { it.toString(outputBase.toInt()) }.reversed().joinToString("").toUpperCase()
    }

    private fun quickB2xToB2x(inputNum: String, inputBase: Base, outputBase: Base, canvas: Group): String {
        TODO("Not yet implemented")
    }

    //sum of positional digits
    private fun toB10(inputNum: String, inputBase: Base, canvas: Group, inputCa2:Int=0): String {
        var baseNum = inputBase.toInt()

        //i swear this is the first time in my life that i need an inner class
        class SumPiece(var number:String, var exponent:Int?){
            constructor(number:Char, exponent:Int) : this(number.toString(), exponent)

            override fun toString(): String {
                if(exponent != null){
                    return number + " ⋅ " + baseNum + exponent!!.superScript()
                }
                return number
            }
        }

        //preparing the data for CA2
        var inputNum = inputNum
        var ca2Sub = ""
        if(inputBase == Base.CA2){
            inputNum = inputNum.zeroFill(inputCa2)
            ca2Sub = if(inputNum[0] == '1') "-" else ""
        }

        //region number(base) =
        var output = "$inputNum($inputBase) = "
        val endLine = " =\n" + " = ".spaceFill(output.length)
        //endregion

        //region character*base^position + ...
        val lastIndex = inputNum.length - 1
        val sums = inputNum
                .mapIndexed {index, digit -> SumPiece(digit, lastIndex - index)}
                .filter {  it.number != "0" }

        sums[0].number = ca2Sub + sums[0].number

        output += sums.joinToString(" + ") + endLine
        //endregion

        //extra step: HEX TO DEC
        if (inputBase == Base.B16){
            // A*16^2 -> 10*16^2
            sums.forEach { it.number = it.number.toInt(16).toString()  }
            output += sums.joinToString(" + ") + endLine
        }

        //creating number sum
        sums.forEach {
            it.number = (it.number.toInt(baseNum) * (baseNum.toDouble().pow(it.exponent!!).toInt())).toString()
            it.exponent = null
        }
        output += sums.joinToString(" + ") + endLine

        //calculating the actual result
        val returnValue = (sums.map{it.number.toInt()}.sum()).toString()
        output += returnValue

        //showing it in the output
        canvas.add(Text(output))

        return returnValue
    }

}