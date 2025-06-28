package com.yorick.common.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PhoneNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = value.isNotEmpty() && value.length != 11
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            // 限制只能输入数字且长度不超过11位
            if (newValue.length <= 11 && newValue.all { char -> char.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = { Text("手机号码") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone
        ),
        singleLine = true,
        modifier = modifier,
        enabled = enabled,
        supportingText = {
            if (isError) {
                Text("请输入11位手机号码")
            }
        },
        isError = isError
    )
}

// 预览
@Preview(showBackground = true)
@Composable
private fun PhoneNumberInputPreview() {
    PhoneNumberInput(
        value = "1380013",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun PhoneNumberInputEmptyPreview() {
    PhoneNumberInput(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun PhoneNumberInputValidPreview() {
    PhoneNumberInput(
        value = "13800138000",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth()
    )
}