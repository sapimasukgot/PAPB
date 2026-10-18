package com.dave.moneymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculatorDialog(
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit
) {

    var expression by remember {
        val sanitized = initialAmount.filter { it.isDigit() || it == '.' }
        mutableStateOf(if (sanitized.isNotEmpty() && sanitized != "0") sanitized else "")
    }

    val evaluatedResult = remember(expression) {
        evaluateExpression(expression)
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val formattedLiveResult = remember(evaluatedResult) {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.format(evaluatedResult)
    }

    fun appendChar(c: String) {
        if (expression.isEmpty()) {
            if (c in listOf("+", "×", "÷")) return
            if (c == "-") {
                expression = "-"
                return
            }
            if (c in listOf("0", "00", "000")) {
                expression = "0"
                return
            }
            expression = c
            return
        }

        val lastChar = expression.last().toString()
        val isLastOp = lastChar in listOf("+", "-", "×", "÷")

        if (c in listOf("+", "-", "×", "÷")) {
            if (isLastOp) {

                expression = expression.dropLast(1) + c
            } else {
                expression += c
            }
        } else if (c == ".") {
            val tokens = expression.split(Regex("[+\\-×÷]"))
            val currentToken = tokens.lastOrNull() ?: ""
            if (!currentToken.contains(".")) {
                expression += if (currentToken.isEmpty()) "0." else "."
            }
        } else if (c in listOf("0", "00", "000")) {
            val tokens = expression.split(Regex("[+\\-×÷]"))
            val currentToken = tokens.lastOrNull() ?: ""
            if (currentToken == "0") return
            expression += c
        } else {
            val tokens = expression.split(Regex("[+\\-×÷]"))
            val currentToken = tokens.lastOrNull() ?: ""
            if (currentToken == "0") {
                expression = expression.dropLast(1) + c
            } else {
                expression += c
            }
        }
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
    }

    fun clear() {
        expression = ""
    }

    fun calculateEquals() {
        val res = evaluateExpression(expression)
        expression = if (res % 1.0 == 0.0) {
            res.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", res)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = Emerald500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Kalkulator Harga",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {

                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            ),
                            maxLines = 1,
                            modifier = Modifier.horizontalScroll(scrollState)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "= Rp ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = formattedLiveResult,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald500,
                                    fontSize = 28.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val buttonSpacing = 8.dp
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalcButton(
                            text = "C",
                            modifier = Modifier.weight(1f),
                            containerColor = Rose500.copy(alpha = 0.15f),
                            textColor = Rose500,
                            onClick = { clear() }
                        )
                        CalcIconButton(
                            icon = Icons.AutoMirrored.Filled.Backspace,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { backspace() }
                        )
                        CalcButton(
                            text = "÷",
                            modifier = Modifier.weight(1f),
                            containerColor = Emerald500.copy(alpha = 0.15f),
                            textColor = Emerald500,
                            onClick = { appendChar("÷") }
                        )
                        CalcButton(
                            text = "×",
                            modifier = Modifier.weight(1f),
                            containerColor = Emerald500.copy(alpha = 0.15f),
                            textColor = Emerald500,
                            onClick = { appendChar("×") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalcButton("7", Modifier.weight(1f)) { appendChar("7") }
                        CalcButton("8", Modifier.weight(1f)) { appendChar("8") }
                        CalcButton("9", Modifier.weight(1f)) { appendChar("9") }
                        CalcButton(
                            text = "-",
                            modifier = Modifier.weight(1f),
                            containerColor = Emerald500.copy(alpha = 0.15f),
                            textColor = Emerald500,
                            onClick = { appendChar("-") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalcButton("4", Modifier.weight(1f)) { appendChar("4") }
                        CalcButton("5", Modifier.weight(1f)) { appendChar("5") }
                        CalcButton("6", Modifier.weight(1f)) { appendChar("6") }
                        CalcButton(
                            text = "+",
                            modifier = Modifier.weight(1f),
                            containerColor = Emerald500.copy(alpha = 0.15f),
                            textColor = Emerald500,
                            onClick = { appendChar("+") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalcButton("1", Modifier.weight(1f)) { appendChar("1") }
                        CalcButton("2", Modifier.weight(1f)) { appendChar("2") }
                        CalcButton("3", Modifier.weight(1f)) { appendChar("3") }
                        CalcButton(
                            text = "=",
                            modifier = Modifier.weight(1f),
                            containerColor = Emerald500,
                            textColor = Color.White,
                            onClick = { calculateEquals() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalcButton("0", Modifier.weight(1f)) { appendChar("0") }
                        CalcButton("00", Modifier.weight(1f)) { appendChar("00") }
                        CalcButton("000", Modifier.weight(1f)) { appendChar("000") }
                        CalcButton(".", Modifier.weight(1f)) { appendChar(".") }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onApply(evaluatedResult)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Gunakan Rp $formattedLiveResult",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalcIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

internal fun evaluateExpression(expr: String): Double {
    if (expr.isBlank()) return 0.0

    var clean = expr.replace("×", "*").replace("÷", "/")

    while (clean.isNotEmpty() && clean.last() in listOf('+', '-', '*', '/')) {
        clean = clean.dropLast(1)
    }
    if (clean.isBlank()) return 0.0

    return try {

        val tokens = mutableListOf<String>()
        var currentNum = StringBuilder()
        var i = 0
        while (i < clean.length) {
            val ch = clean[i]
            if (ch in listOf('+', '-', '*', '/')) {

                if (ch == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/")) && currentNum.isEmpty()) {
                    currentNum.append('-')
                } else {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum.toString())
                        currentNum = StringBuilder()
                    }
                    tokens.add(ch.toString())
                }
            } else {
                currentNum.append(ch)
            }
            i++
        }
        if (currentNum.isNotEmpty()) {
            tokens.add(currentNum.toString())
        }

        if (tokens.isEmpty()) return 0.0

        val pass1 = mutableListOf<String>()
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx]
            if (token == "*" || token == "/") {
                val left = pass1.removeAt(pass1.lastIndex).toDoubleOrNull() ?: 0.0
                idx++
                if (idx < tokens.size) {
                    val right = tokens[idx].toDoubleOrNull() ?: 1.0
                    val res = if (token == "*") left * right else {
                        if (right != 0.0) left / right else 0.0
                    }
                    pass1.add(res.toString())
                } else {
                    pass1.add(left.toString())
                }
            } else {
                pass1.add(token)
            }
            idx++
        }

        var finalResult = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
        var pIdx = 1
        while (pIdx < pass1.size) {
            val op = pass1[pIdx]
            val nextVal = pass1.getOrNull(pIdx + 1)?.toDoubleOrNull() ?: 0.0
            if (op == "+") {
                finalResult += nextVal
            } else if (op == "-") {
                finalResult -= nextVal
            }
            pIdx += 2
        }

        if (finalResult < 0.0) 0.0 else finalResult
    } catch (_: Exception) {
        0.0
    }
}
