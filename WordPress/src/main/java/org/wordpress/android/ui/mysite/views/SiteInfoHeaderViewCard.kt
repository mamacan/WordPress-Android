package org.wordpress.android.ui.mysite.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.wordpress.android.R
import org.wordpress.android.ui.mysite.MySiteCardAndItem.SiteInfoHeaderCard

class SiteInfoHeaderViewCard {
    @Composable
    fun SiteInfoHeaderView(siteInfoHeaderCard: SiteInfoHeaderCard?) {
        val siteInfoIcon = painterResource(id = R.drawable.ic_globe_white_24dp)
        val siteInfoIconContentDescription = stringResource(id = R.string.my_site_icon_content_description)

        val switchSiteIcon = painterResource(id = R.drawable.ic_chevron_down_white_24dp)
        val switchSiteIconContentDescription = stringResource(id = R.string.my_site_btn_switch_site)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Image(painter = siteInfoIcon, contentDescription = siteInfoIconContentDescription)
//                CircularProgressIndicator()
            }
            Column {
                Text(siteInfoHeaderCard?.title ?: "Site title")
                Text(siteInfoHeaderCard?.url ?: "Site url")
            }
            IconButton(onClick = { siteInfoHeaderCard?.onSwitchSiteClick }) {
                Icon(painter = switchSiteIcon, contentDescription = switchSiteIconContentDescription)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        SiteInfoHeaderView(null)
    }
}
