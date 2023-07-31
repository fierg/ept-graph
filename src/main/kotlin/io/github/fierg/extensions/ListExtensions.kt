package io.github.fierg.extensions

fun MutableList<Int>.removeIfNotIncludedIn(other: List<Int>){
    if (other.isEmpty()) this.clear()
    var indexA = 0
    var indexB = 0

    while (indexA < this.size && indexB < other.size) {
        when {
            this[indexA] < other[indexB] -> {//Other list item is larger, remove
                this.removeAt(indexA)
            }
            this[indexA] > other[indexB] -> {  //this list item is larger, move index on other list
                indexB++
            }
            else -> { // Both elements are equal, keep
                indexA++
                indexB++
            }
        }
    }
}