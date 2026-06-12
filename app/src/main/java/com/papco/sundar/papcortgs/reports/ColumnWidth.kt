package com.papco.sundar.papcortgs.reports

import androidx.core.text.isDigitsOnly

//Helper class used to calculate and set the width of a column in the report
//Setting maximumWidth > 0 means this column will cap the maximum width to the provided value

class ColumnWidth(
    var minimumWidth: Int,
    var maximumWidth: Int = 0
) {

    init {
        //Stop negative numbers
        require(maximumWidth >= 0) { "Maximum Width cannot be negative" }

        if (maximumWidth > 0)
            require(maximumWidth >= minimumWidth) {
                "Maximum width cannot be less than minimum width"
            }
    }

    var recommendedWidth: Int = minimumWidth
        private set

    fun calculateRecommendedWidth(cellContent: String) {

        val length = if (cellContent.isDigitsOnly())
            cellContent.length + 2
        else
            cellContent.length + 4

        if (length <= recommendedWidth)
            return

        recommendedWidth = if (maximumWidth in 1..<length)
            maximumWidth
        else
            length
    }
}
