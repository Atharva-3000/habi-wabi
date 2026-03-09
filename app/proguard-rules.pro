# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to all release builds,
# but should only be adjusted after testing.
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
