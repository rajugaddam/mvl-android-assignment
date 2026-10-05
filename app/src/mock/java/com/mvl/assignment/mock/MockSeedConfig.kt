package com.mvl.assignment.mock

/** A real place for sample bookings: its name always matches its coordinates. */
data class SeedPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Sample data of the mock backend, provided by Hilt in the mock flavor's BooksNetworkModule.
 * Tests pass their own config (e.g. [samplesEnabled] = false to see only what they posted).
 */
data class MockSeedConfig(
    val samplesEnabled: Boolean = true,
    val samplesPerMonth: IntRange = 2..6,
    val sampleAqi: IntRange = 10..150,
    val places: List<SeedPlace> = SEOUL_PLACES,
) {
    init {
        require(!samplesEnabled || places.size >= 2) { "Sample bookings need at least two places" }
        require(samplesPerMonth.first >= 0 && !samplesPerMonth.isEmpty()) { "Invalid samplesPerMonth" }
        require(!sampleAqi.isEmpty()) { "Invalid sampleAqi" }
    }

    companion object {
        /** Seoul neighbourhoods in the app's address format (district, dong), at their real location. */
        val SEOUL_PLACES = listOf(
            SeedPlace("Seocho District, Yangjae 2(i)-dong", 37.4706, 127.0384),
            SeedPlace("Gangnam District, Yeoksam 1(il)-dong", 37.4999, 127.0365),
            SeedPlace("Jongno District, Sajik-dong", 37.5757, 126.9690),
            SeedPlace("Mapo District, Seogyo-dong", 37.5551, 126.9198),
            SeedPlace("Songpa District, Jamsil 3(sam)-dong", 37.5133, 127.0866),
            SeedPlace("Yongsan District, Itaewon 1(il)-dong", 37.5345, 126.9940),
            SeedPlace("Jung District, Myeong-dong", 37.5609, 126.9861),
            SeedPlace("Yeongdeungpo District, Yeouido-dong", 37.5219, 126.9245),
        )
    }
}
