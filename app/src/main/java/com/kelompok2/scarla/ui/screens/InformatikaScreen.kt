package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*

data class InformatikaItem(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val route: String? = null
)

@Composable
fun InformatikaScreen(navController: NavController) {

    val materiList = listOf(
        InformatikaItem(
            "HTML",
            "HTML dasar",
            R.drawable.ic_html,
            "html_screen"
        ),
        InformatikaItem(
            "CSS",
            "CSS dasar",
            R.drawable.ic_css
        ),
        InformatikaItem(
            "Javascript",
            "Javascript dasar",
            R.drawable.ic_javascript
        ),
        InformatikaItem(
            "Java",
            "Java dasar",
            R.drawable.ic_java
        ),
        InformatikaItem(
            "Python",
            "Python dasar",
            R.drawable.ic_python
        ),
        InformatikaItem(
            "C#",
            "C# dasar",
            R.drawable.ic_csharp
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // HEADER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Neutral50
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .background(
                            Primary500,
                            CircleShape
                        )
                        .size(38.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Neutral900
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Informatika",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            items(materiList) { item ->

                InformatikaCard(
                    item = item,
                    onClick = {
                        item.route?.let {
                            navController.navigate(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InformatikaCard(
    item: InformatikaItem,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .height(190.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Neutral50
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.title,
                    modifier = Modifier.size(55.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral700
                )
            }

            AppButton(
                text = "Mulai",
                onClick = onClick,

                modifier = Modifier.fillMaxWidth(),

                buttonType = ButtonType.PRIMARY
            )
        }
    }
}