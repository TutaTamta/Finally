import kotlin.math.max
import kotlin.math.min
import java.util.*

fun lz77Compress(input: String, window: Int, buffer: Int): List<Triple<Int, Int, Char>> {
    val compressed = mutableListOf<Triple<Int, Int, Char>>()
    var indexChar = 0

    while (indexChar < input.length) {
        var bestLength = 0
        var bestOffset = 0
        var nextChar = ""

        val maxOffset = min(indexChar, window)
        val maxLength: Int = min(input.length - indexChar, buffer)

        for (offset in 1..maxOffset) {
            for (length in 1..maxLength) {
                val stringWindow: String = input.substring(indexChar - offset, indexChar - offset + length)
                val stringBuffer: String = input.substring(indexChar, indexChar + length)

                if (stringWindow == stringBuffer && length > bestLength) {
                    bestLength = length
                    bestOffset = (offset - window) * (-1)
                    if (indexChar + length < input.length) {
                        nextChar = input[indexChar + length].toString()
                    }
                }
            }
        }
        if (indexChar == input.length - 1) {
            compressed.add(Triple(0, 0, input[indexChar]))
        } else {
            compressed.add(Triple(bestOffset, bestLength, if (bestLength > 0) nextChar else input[indexChar]) as Triple<Int, Int, Char>)
        }
        indexChar += bestLength + 1
    }

    return compressed
}
fun lzssCompress(input: String, max_dictionary:Int, max_buffer:Int): MutableList<Array<String>> {
    val compressed: MutableList<Array<String>> = mutableListOf()
    var dictionary = ""
    var buffer: String = input.substring(0, min(max_buffer, input.length))
    var inputUser = input.substring(buffer.length)

    println()
    while (buffer.isNotEmpty()) {
        var offset = 0
        var length = 0
        for (i in 1..buffer.length) {
            val subStr = buffer.substring(0, i)
            val position = dictionary.lastIndexOf(subStr)
            if (position != -1) {
                offset = max_dictionary - dictionary.length + position
                length = subStr.length
            } else {
                break
            }
        }
        val code: String = if (length > 0) {
            "1<$offset,$length>"
        } else {
            "0'" + buffer[0] + "'"
        }
        compressed.add(arrayOf(code))
        print("( $code ) ")

        val shiftSize = max(length.toDouble(), 1.0).toInt()
        dictionary += buffer.substring(0, shiftSize)
        if (dictionary.length > max_dictionary) {
            dictionary = dictionary.substring(dictionary.length - max_dictionary)
        }

        buffer = buffer.substring(shiftSize)
        if (buffer.length < max_buffer && inputUser.isNotEmpty()) {
            val addSize: Int = min(max_buffer - buffer.length, inputUser.length)
            buffer += inputUser.substring(0, addSize)
            inputUser = inputUser.substring(addSize)
        }
    }
    println()
    return compressed
}
fun lz78Compress(input: String): List<Pair<Int, Char>> {
    val compressed = mutableListOf<Pair<Int, Char>>()
    val dictionary = mutableMapOf<String, Int>()
    var currentIndex = 0

    while (currentIndex < input.length) {
        var length = 0
        var offset = 0

        for (i in currentIndex..<input.length) {
            val substring = input.substring(currentIndex, i + 1)

            if (!dictionary.containsKey(substring)) {
                dictionary[substring] = dictionary.size + 1
                length = substring.length
                offset = dictionary[substring.substring(0, length - 1)] ?: 0
                break
            }
        }

        if (length > 0) {
            compressed.add(Pair(offset, input[currentIndex + length - 1]))
            currentIndex += length
        } else {
            compressed.add(Pair(offset, input[currentIndex]))
            currentIndex++
        }
    }

    return compressed
}

data class Node(val symbol: Char, val probability: Double, var left: Node? = null, var right: Node? = null) : Comparable<Node> {
    override fun compareTo(other: Node): Int = probability.compareTo(other.probability)
}

fun huffman(probabilities: Map<Char, Double>): Map<Char, String> {
    val priorityQueue = PriorityQueue<Node>()
    for ((symbol, probability) in probabilities) {
        val node = Node(symbol, probability)
        priorityQueue.offer(node)
    }
    //я знаю, что ошибка у меня в приоретедней очереди, но я в упор не вижу как это исправить.

    while (priorityQueue.size > 1) {
        var node1 = priorityQueue.poll()
        var node2 = priorityQueue.poll()

        if (node1.left == null && node1.right == null &&
            node2.left == null && node2.right == null &&
            node1.probability < node2.probability
        ) {
            val temp = node1
            node1 = node2
            node2 = temp
        }

        val parent = Node('\u0000', node1.probability + node2.probability, node1, node2)
        priorityQueue.offer(parent)
    }

    val root = priorityQueue.poll()
    val huffmanCodes = mutableMapOf<Char, String>()
    buildHuffmanTable(root, "", huffmanCodes)
    return huffmanCodes
}

fun buildHuffmanTable(node: Node?, code: String, huffmanCodes: MutableMap<Char, String>) {
    if (node == null) return
    if (node.left == null && node.right == null) {
        huffmanCodes[node.symbol] = code
    } else {
        buildHuffmanTable(node.left, "${code}0", huffmanCodes)
        buildHuffmanTable(node.right, "${code}1", huffmanCodes)
    }
}

fun shannonFano(probabilities: Map<Char, Double>): Map<Char, String> {
    val sortedProbabilities = probabilities.toList().sortedByDescending { it.second }
    val shannonFanoCodes = mutableMapOf<Char, String>()
    shannonFanoHelper(sortedProbabilities, 0, sortedProbabilities.size - 1, "", shannonFanoCodes)
    return shannonFanoCodes
}

fun shannonFanoHelper(sortedProbabilities: List<Pair<Char, Double>>, start: Int, end: Int, code: String, shannonFanoCodes: MutableMap<Char, String>) {
    if (start > end) return
    if (start == end) {
        shannonFanoCodes[sortedProbabilities[start].first] = code
        return
    }

    var i = start
    var j = end
    var sum1 = sortedProbabilities[start].second
    var sum2 = sortedProbabilities[end].second
    while (i < j) {

        if (sum1 < sum2) {
            sum1 += sortedProbabilities[++i].second
        } else {
            sum2 += sortedProbabilities[--j].second
        }
    }

    shannonFanoHelper(sortedProbabilities, start, i, "${code}0", shannonFanoCodes)
    shannonFanoHelper(sortedProbabilities, i + 1, end, "${code}1", shannonFanoCodes)
}

class TreeNode(var value: Int) {
    var left: TreeNode? = null
    var right: TreeNode? = null
}

class BinaryTree {
    private var root: TreeNode? = null

    fun insert(value: Int) {
        root = insertNode(root, value)
    }

    private fun insertNode(node: TreeNode?, value: Int): TreeNode {
        if (node == null) {
            return TreeNode(value)
        }

        if (value < node.value) {
            node.left = insertNode(node.left, value)
        } else if (value > node.value) {
            node.right = insertNode(node.right, value)
        }

        return node
    }

    fun search(value: Int): Boolean {
        return searchNode(root, value) != null
    }

    private fun searchNode(node: TreeNode?, value: Int): TreeNode? {
        if (node == null || node.value == value) {
            return node
        }

        if (value < node.value) {
            return searchNode(node.left, value)
        }

        return searchNode(node.right, value)
    }

    fun remove(value: Int) {
        root = removeNode(root, value)
    }

    private fun removeNode(node: TreeNode?, value: Int): TreeNode? {
        if (node == null) {
            return null
        }

        if (value < node.value) {
            node.left = removeNode(node.left, value)
        } else if (value > node.value) {
            node.right = removeNode(node.right, value)
        } else {
            if (node.left == null && node.right == null) {
                return null
            } else if (node.left == null) {
                return node.right
            } else if (node.right == null) {
                return node.left
            } else {
                val minValue = findMinValue(node.right!!)
                node.value = minValue
                node.right = removeNode(node.right, minValue)
            }
        }

        return node
    }

    private fun findMinValue(node: TreeNode): Int {
        var current = node
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    fun breadthFirstTraversal() {
        if (root == null) {
            return
        }

        val queue = LinkedList<TreeNode>()
        queue.add(root!!)

        while (queue.isNotEmpty()) {
            val node = queue.remove()
            print("${node.value} ")

            if (node.left != null) {
                queue.add(node.left!!)
            }

            if (node.right != null) {
                queue.add(node.right!!)
            }
        }
    }

    fun depthFirstTraversal() {
        depthFirstTraversal(root)
    }

    private fun depthFirstTraversal(node: TreeNode?) {
        if (node == null) {
            return
        }

        print("${node.value} ")
        depthFirstTraversal(node.left)
        depthFirstTraversal(node.right)
    }
}


fun main() {
    var f:String = "1"
    while(f!="0"){
        println("1. Лаб. №1")
        println("2. Лаб. №2")
        println("3. Лаб. №3")
        println("4. Лаб. №4")
        println("0. Выход")
        print("Выберите лабораторную работу: ")
        val choise = readln()
        when (choise) {
            "0" -> {
                f = "0"
            }
            "1" -> {
                print("Введите кол-во рёбер: ")
                val edges: String = readln()
                print("Введите кол-во вершин: ")
                val nodes: String = readln()
                var a: String?
                var b: String?

                val grafs = mutableMapOf<Int, List<Int>>()

                for (i in 1..edges.toInt()) {
                    println("Введите пары вершин по отдельности: ")
                    println("e${i}: ")
                    a = readln()
                    b = readln()
                    grafs.put(i, listOf(a.toInt(), b.toInt()))
                }

                println("Какой граф?")
                println("1: неориентированный")
                println("2: ориентированный")


                val switch: String = readln()

                when (switch) {
                    "1" -> {
                        println("Список смежности")
                        println("-------------------------------------------")
                        for (i in 1..nodes.toInt()) {
                            val filtredGrafs = grafs.filter {it.value.contains(i)}
                            print("v${i}:\t")
                            for ((_, value) in filtredGrafs) {
                                if (value[0] == i) print("v${value[1]}\t")
                                else print("v${value[0]}\t")
                            }
                            println()
                            println()
                        }

/////////////////////////////////////////////////////////////////////////////////////////

                        println("Матрица смежности")
                        println("-------------------------------------------")
                        for(i in 1..nodes.toInt()) {
                            print("v${i}:\t")
                            for (j in 1..nodes.toInt()) {
                                run loop@ {
                                    grafs.forEach{
                                        if ((it.value == listOf(i, j)) || (it.value == listOf(j, i))) {
                                            print("1\t")
                                            return@loop
                                        }
                                    }
                                    print("0\t")
                                }
                            }

                            println()
                            println()
                        }

//////////////////////////////////////////////////////////////////////////////////////////////////
                        println("Матрица индентичности")
                        println("-------------------------------------------")
                        for (i in 1..nodes.toInt()){
                            print("v${i}:\t")
                            for ((key, value) in grafs) {
                                if (value[0] == i || value[1] == i) print("1\t")
                                else print("0\t")
                            }
                            println()
                        }
                    }
                    "2" -> {
                        println("Список смежности")
                        println("-------------------------------------------")
                        for (i in 1..nodes.toInt()) {
                            val filtredGrafs = grafs.filter {it.value.contains(i)}
                            print("v${i}:\t")
                            for ((key, value) in filtredGrafs) {
                                if (value[0] == i) print("v${value[1]}\t")
                            }
                            println()
                            println()
                        }
////////////////////////////////////////////////////////////////////////////////

                        println("Матрица смежности")
                        println("-------------------------------------------")
                        for(i in 1..nodes.toInt()) {
                            print("v${i}:\t")
                            for (j in 1..nodes.toInt()) {
                                run loop@ {
                                    grafs.forEach{
                                        if (it.value == listOf(i, j)) {
                                            print("1\t")
                                            return@loop
                                        }
                                    }
                                    print("0\t")
                                }
                            }

                            println()
                            println()
                        }
////////////////////////////////////////////////////////////

                        println("Матрица индентичности")
                        println("-------------------------------------------")
                        for (i in 1..nodes.toInt()){
                            print("v${i}:\t")
                            for ((key, value) in grafs) {
                                if ((value[0] == i) && (value[1] == i)) print("1\t")
                                if ((value[0] == i) && (value[1] != i)) print("1\t")
                                if ((value[1] == i) && (value[0] != i)) print("-1\t")
                                if ((value[0] != i) && (value[1] != i)) print("0\t")
                            }
                            println()
                        }
                    }
                }
            }
            "2" -> {
                val tree = BinaryTree()
                tree.insert(5)
                tree.insert(3)
                tree.insert(7)
                tree.insert(2)
                tree.insert(4)
                tree.insert(6)
                tree.insert(8)
                tree.breadthFirstTraversal()
                println()
                if (tree.search(5)) println("5 found") else println("5 not found")
                if (tree.search(9)) println("9 found") else println("9 not found")
                tree.depthFirstTraversal()
                println()
                tree.remove(5)
                tree.breadthFirstTraversal()
                println()
                tree.depthFirstTraversal()
                println()
                tree.remove(7)
                tree.breadthFirstTraversal()
                println()
                tree.depthFirstTraversal()
                println()
                tree.remove(3)
                tree.breadthFirstTraversal()
                println()
                tree.depthFirstTraversal()
                println()
            }
            "3" -> {
                println("Enter a word:")
                val word = readlnOrNull() ?: ""

                val probabilities = word.groupingBy { it }.eachCount().mapValues { it.value.toDouble() / word.length }

                println("Choose an algorithm:")
                println("1. Huffman")
                println("2. Shannon-Fano")

                val choice = readlnOrNull()?.toInt()

                when (choice) {
                    1 -> println("Huffman Codes: ${huffman(probabilities)}")
                    2 -> println("Shannon-Fano Codes: ${shannonFano(probabilities)}")
                    else -> println("Invalid choice")
                }
            }
            "4" -> {
                print("Введите строку: ")
                val input = readlnOrNull() ?: ""
                print("Размер словаря: ")
                val dictionary = readlnOrNull() ?: "0"
                print("Размер буфера: ")
                val buffer = readlnOrNull() ?: "0"

                val lz77Result = lz77Compress(input, dictionary.toInt(), buffer.toInt())
                println("LZ77 compressed: $lz77Result")

                val lzssResult = lzssCompress(input, dictionary.toInt(), buffer.toInt())
                println("LZSS compressed: $lzssResult")

                val lz78Result = lz78Compress(input)
                println("LZ78 compressed: $lz78Result")
            }
        }
    }
}
