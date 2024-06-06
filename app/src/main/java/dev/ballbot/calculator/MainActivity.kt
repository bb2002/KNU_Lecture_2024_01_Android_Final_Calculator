package dev.ballbot.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.ballbot.calculator.ui.theme.CalculatorTheme
import java.util.Stack

class MainActivity : ComponentActivity() {
    lateinit var calculateView: TextView
    lateinit var acButton: Button
    lateinit var delButton: Button
    lateinit var equalButton: Button
    lateinit var plusButton: Button
    lateinit var minusButton: Button
    lateinit var divideButton: Button
    lateinit var multiButton: Button
    lateinit var numPads: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.calculateView = findViewById(R.id.calcView)
        this.acButton = findViewById(R.id.btnAC)
        this.delButton = findViewById(R.id.btnDEL)
        this.equalButton = findViewById(R.id.btnEquals)
        this.plusButton = findViewById(R.id.btnAdd)
        this.minusButton = findViewById(R.id.btnSubtract)
        this.multiButton = findViewById(R.id.btnMultiply)
        this.divideButton = findViewById(R.id.btnDivide)
        this.numPads = arrayListOf(
            R.id.btn0, R.id.btn1, R.id.btn2,
            R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8,
            R.id.btn9, R.id.btnDot
        ).map { id -> findViewById(id) }

        this.acButton.setOnClickListener {
            this.calculateView.text = ""
        }

        this.delButton.setOnClickListener {
            val currentText = this.calculateView.text
            this.calculateView.text = currentText.subSequence(0, currentText.length - 1)
        }

        this.equalButton.setOnClickListener {
            try {
                this.calculateView.text = eval(this.calculateView.text.toString()).toString()
            } catch(ex: Exception) {
                this.calculateView.text = "ERROR"
            }
        }

        this.plusButton.setOnClickListener(onOperationClickListener)
        this.minusButton.setOnClickListener(onOperationClickListener)
        this.multiButton.setOnClickListener(onOperationClickListener)
        this.divideButton.setOnClickListener(onOperationClickListener)
        this.numPads.forEach { btn -> btn.setOnClickListener(onNumPadClickListener) }
    }

    private val onOperationClickListener = View.OnClickListener {it ->
        val op = when (it.id) {
            R.id.btnAdd -> '+'
            R.id.btnSubtract -> '-'
            R.id.btnMultiply -> '*'
            R.id.btnDivide -> '/'
            else -> null
        }
        val currentText = this.calculateView.text.toString()
        this.calculateView.text = currentText + op
    }

    private val onNumPadClickListener = View.OnClickListener { it ->
        val num = when (it.id) {
            R.id.btn0 -> 0
            R.id.btn1 -> 1
            R.id.btn2 -> 2
            R.id.btn3 -> 3
            R.id.btn4 -> 4
            R.id.btn5 -> 5
            R.id.btn6 -> 6
            R.id.btn7 -> 7
            R.id.btn8 -> 8
            R.id.btn9 -> 9
            R.id.btnDot -> '.'
            else -> 0
        }

        val currentText = this.calculateView.text.toString()
        this.calculateView.text = currentText + num
    }


    private fun isOperator(c: Char): Boolean {
        return when (c) {
            '+', '-', '*', '/' -> true
            else -> false
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = StringBuilder()

        for (char in expression) {
            when {
                char.isDigit() || char == '.' -> {
                    currentToken.append(char)
                }
                isOperator(char) || char == '(' || char == ')' -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                    tokens.add(char.toString())
                }
                char.isWhitespace() -> continue
                else -> throw IllegalArgumentException("Invalid character in expression: $char")
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }

        return tokens
    }

    private fun applyOperator(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            else -> throw IllegalArgumentException("Invalid operator: $op")
        }
    }

    private fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> -1
        }
    }

    private fun eval(expression: String): Double {
        val tokens = tokenize(expression)
        val values = Stack<Double>()
        val operators = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            when {
                (token.toDoubleOrNull() != null) -> {
                    values.push(token.toDouble())
                }
                token[0] == '(' -> {
                    operators.push(token[0])
                }
                token[0] == ')' -> {
                    while (operators.peek() != '(') {
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
                    }
                    operators.pop()
                }
                isOperator(token[0]) -> {
                    while (operators.isNotEmpty() && precedence(operators.peek()) >= precedence(token[0])) {
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
                    }
                    operators.push(token[0])
                }
            }
            i++
        }

        while (operators.isNotEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
        }

        return values.pop()
    }
}
