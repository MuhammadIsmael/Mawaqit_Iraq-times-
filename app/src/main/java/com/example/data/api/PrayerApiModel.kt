package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrayerApiResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: PrayerData?
)

@JsonClass(generateAdapter = true)
data class PrayerData(
    @Json(name = "timings") val timings: PrayerApiTimings,
    @Json(name = "date") val date: PrayerApiDate?,
    @Json(name = "meta") val meta: PrayerApiMeta?
)

@JsonClass(generateAdapter = true)
data class PrayerApiTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String? = null,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String? = null,
    @Json(name = "Midnight") val midnight: String? = null,
    @Json(name = "Firstthird") val firstThird: String? = null,
    @Json(name = "Lastthird") val lastThird: String? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiDate(
    @Json(name = "readable") val readable: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "hijri") val hijri: PrayerApiHijri? = null,
    @Json(name = "gregorian") val gregorian: PrayerApiGregorian? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiHijri(
    @Json(name = "date") val date: String? = null,
    @Json(name = "day") val day: String? = null,
    @Json(name = "weekday") val weekday: PrayerApiWeekday? = null,
    @Json(name = "month") val month: PrayerApiMonth? = null,
    @Json(name = "year") val year: String? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiGregorian(
    @Json(name = "date") val date: String? = null,
    @Json(name = "day") val day: String? = null,
    @Json(name = "weekday") val weekday: PrayerApiWeekday? = null,
    @Json(name = "month") val month: PrayerApiMonth? = null,
    @Json(name = "year") val year: String? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiWeekday(
    @Json(name = "en") val en: String? = null,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiMonth(
    @Json(name = "number") val number: Int? = null,
    @Json(name = "en") val en: String? = null,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class PrayerApiMeta(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "timezone") val timezone: String? = null
)
