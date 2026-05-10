package com.kelompok2.scarla.ui.theme

import com.kelompok2.scarla.R
import androidx.compose.material3.Typography as M3Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val BubblegumSansFontFamily = FontFamily(
    Font(R.font.bubblegum_sans_regular, FontWeight.Normal),
)

private fun appTextStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: Int,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    letterSpacing = 0.sp,
)

val PoppinsH1Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 64)
val PoppinsH1Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 64)
val PoppinsH1Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 64)
val PoppinsH1Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 64)

val PoppinsH2Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 48)
val PoppinsH2Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 48)
val PoppinsH2Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 48)
val PoppinsH2Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 48)

val PoppinsH3Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 40)
val PoppinsH3Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 40)
val PoppinsH3Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 40)
val PoppinsH3Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 40)

val PoppinsH4Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 32)
val PoppinsH4Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 32)
val PoppinsH4Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 32)
val PoppinsH4Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 32)

val PoppinsH5Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 24)
val PoppinsH5Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 24)
val PoppinsH5Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 24)
val PoppinsH5Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 24)

val PoppinsH6Bold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 20)
val PoppinsH6Semibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 20)
val PoppinsH6Medium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 20)
val PoppinsH6Regular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 20)

val PoppinsLargeBold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 20)
val PoppinsLargeSemibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 20)
val PoppinsLargeMedium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 20)
val PoppinsLargeRegular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 20)

val PoppinsMediumBold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 18)
val PoppinsMediumSemibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 18)
val PoppinsMediumMedium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 18)
val PoppinsMediumRegular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 18)

val PoppinsRegularBold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 16)
val PoppinsRegularSemibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 16)
val PoppinsRegularMedium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 16)
val PoppinsRegularRegular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 16)

val PoppinsSmallBold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 14)
val PoppinsSmallSemibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 14)
val PoppinsSmallMedium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 14)
val PoppinsSmallRegular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 14)

val PoppinsTinyBold = appTextStyle(PoppinsFontFamily, FontWeight.Bold, 12)
val PoppinsTinySemibold = appTextStyle(PoppinsFontFamily, FontWeight.SemiBold, 12)
val PoppinsTinyMedium = appTextStyle(PoppinsFontFamily, FontWeight.Medium, 12)
val PoppinsTinyRegular = appTextStyle(PoppinsFontFamily, FontWeight.Normal, 12)

val BubblegumH1 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 64)
val BubblegumH2 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 48)
val BubblegumH3 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 40)
val BubblegumH4 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 32)
val BubblegumH5 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 24)
val BubblegumH6 = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 20)
val BubblegumLarge = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 20)
val BubblegumMedium = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 18)
val BubblegumRegular = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 16)
val BubblegumSmall = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 14)
val BubblegumTiny = appTextStyle(BubblegumSansFontFamily, FontWeight.Normal, 12)

val Typography = M3Typography(
    displayLarge = PoppinsH1Bold,
    displayMedium = PoppinsH2Bold,
    displaySmall = PoppinsH3Bold,
    headlineLarge = PoppinsH4Bold,
    headlineMedium = PoppinsH5Bold,
    headlineSmall = PoppinsH6Bold,
    titleLarge = PoppinsLargeSemibold,
    titleMedium = PoppinsMediumSemibold,
    titleSmall = PoppinsRegularSemibold,
    bodyLarge = PoppinsRegularRegular,
    bodyMedium = PoppinsSmallRegular,
    bodySmall = PoppinsTinyRegular,
    labelLarge = PoppinsSmallMedium,
    labelMedium = PoppinsTinyMedium,
    labelSmall = PoppinsTinyRegular,
)