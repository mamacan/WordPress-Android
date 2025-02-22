package org.wordpress.android.ui.main.jetpack.migration.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wordpress.android.R.color
import org.wordpress.android.R.dimen

@Composable
fun Message(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
            text = text,
            fontSize = 17.sp,
            style = TextStyle(letterSpacing = (-0.01).sp),
            color = colorResource(color.gray_50),
            modifier = modifier
                    .padding(horizontal = dimensionResource(dimen.jp_migration_padding_horizontal))
                    .padding(top = 20.dp, bottom = 30.dp)
    )
}
