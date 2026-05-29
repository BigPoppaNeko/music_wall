# ── Last.fm API Models (Gson needs field names intact) ───────────────────────
-keep class com.jfcardenas.musicwall.api.** { *; }
-keepclassmembers class com.jfcardenas.musicwall.api.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class com.jfcardenas.musicwall.data.local.db.** { *; }

# ── Hilt generated code ───────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── Gson ──────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── Obfuscate BuildConfig fields (API keys) ──────────────────────────────────
# R8 will rename LASTFM_API_KEY and LASTFM_SHARED_SECRET field names,
# making static analysis harder without disabling BuildConfig entirely.
# For production, move API calls to a backend proxy.
-keepclassmembers class com.jfcardenas.musicwall.BuildConfig {
    public static final String APPLICATION_ID;
    public static final boolean DEBUG;
    public static final int VERSION_CODE;
    public static final String VERSION_NAME;
}

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
