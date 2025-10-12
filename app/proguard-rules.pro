# ===============================================================
# ProGuard Rules for Atomic Periodic Table
# Last Updated: 2025.10.13
# ===============================================================

# ===============================================================
# General Configuration
# ===============================================================
-ignorewarnings
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable

# Keep all enums
-keepclassmembers enum * { *; }

# ===============================================================
# Kotlin & Coroutines
# ===============================================================
# Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ===============================================================
# AndroidX & Lifecycle
# ===============================================================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.core.** { *; }
-dontwarn androidx.lifecycle.**

# ===============================================================
# Google Services (AdMob, Play Services)
# ===============================================================
# Google Mobile Ads SDK (24.7.0)
-keep class com.google.android.gms.** { *; }
-keep class com.google.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Keep advertising ID classes
-keep class com.google.android.gms.ads.identifier.** { *; }

# Keep ad listeners and callbacks
-keep class * extends com.google.android.gms.ads.AdListener { *; }
-keep class * extends com.google.android.gms.ads.FullScreenContentCallback { *; }

# Keep application ID from manifest
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Google Play Services base
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.**

# Google Play In-App Review
-keep class com.google.android.play.** { *; }
-dontwarn com.google.android.play.**

# AppLovin Mediation Adapter (13.4.0.0)
-keep class com.applovin.** { *; }
-keep interface com.applovin.** { *; }
-dontwarn com.applovin.**

# ===============================================================
# Gson (2.13.1)
# ===============================================================
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep generic signatures for Gson
-keepattributes Signature

# Keep all model classes for serialization/deserialization
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep your model classes (adjust package name if needed)
-keep class com.mckimquyen.atomicPeriodicTable.model.** { *; }
-keep class com.mckimquyen.atomicPeriodicTable.data.** { *; }

# ===============================================================
# OkHttp (4.12.0)
# ===============================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ===============================================================
# Picasso (2.71828)
# ===============================================================
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**
-keepclasseswithmembernames class * {
    @com.squareup.picasso.* <methods>;
}

# ===============================================================
# Retrofit 2.x (nếu có sử dụng)
# ===============================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ===============================================================
# Material Components
# ===============================================================
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ===============================================================
# Third-party UI Libraries
# ===============================================================
# SlidingUpPanel (3.4.0)
-keep class com.sothree.slidinguppanel.** { *; }
-dontwarn com.sothree.slidinguppanel.**

# ZoomLayout (1.9.0)
-keep class com.otaliastudios.** { *; }
-dontwarn com.otaliastudios.**

# DragDropSwipeRecyclerView (1.1.1)
-keep class com.ernestoyaquello.dragdropswiperecyclerview.** { *; }
-dontwarn com.ernestoyaquello.dragdropswiperecyclerview.**

# TwoWayNestedScrollView
-keep class com.github.ultimate.deej.twowaynestedscrollview.** { *; }
-dontwarn com.github.ultimate.deej.twowaynestedscrollview.**

# ===============================================================
# RenderScript (for blur effects, image processing)
# ===============================================================
-dontwarn androidx.renderscript.**
-keep class androidx.renderscript.** { *; }
-keepclassmembers class androidx.renderscript.RenderScript {
    native *** rsn*(...);
    native *** n*(...);
}

# ===============================================================
# ViewBinding
# ===============================================================
-keep class * extends androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}

# ===============================================================
# Keep Application-specific classes
# ===============================================================
# Keep your AdMobManager
-keep class com.mckimquyen.atomicPeriodicTable.sdkadbmob.** { *; }

# Keep your Application class
-keep class com.mckimquyen.atomicPeriodicTable.RoyApp { *; }

# Keep all Activities, Fragments, Services
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ===============================================================
# Keep custom views
# ===============================================================
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ===============================================================
# Keep native methods
# ===============================================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===============================================================
# Keep Parcelable implementations
# ===============================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===============================================================
# Keep Serializable classes
# ===============================================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===============================================================
# R8 Optimization Settings
# ===============================================================
# Allow optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds to reduce APK size
# Uncomment the following to remove all logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove println statements
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
}

# Remove System.out.println
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# ===============================================================
# End of ProGuard Rules
# ===============================================================
