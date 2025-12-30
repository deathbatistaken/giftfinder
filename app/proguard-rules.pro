# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.gift.finder.domain.model.** { *; }
-keep class com.gift.finder.data.local.entities.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Google Play Billing
-keep class com.android.vending.billing.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Compose
-dontwarn androidx.compose.**
