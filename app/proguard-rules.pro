# Add project specific ProGuard rules here.
-keep class com.example.noteapp.data.local.** { *; }
-keepclassmembers class com.example.noteapp.data.local.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**