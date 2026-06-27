# General Rules
-keepattributes SourceFile,LineNumberTable,Signature,EnclosingMethod,InnerClasses,*Annotation*
-dontwarn android.support.**
-dontwarn androidx.**

# Keep EVERYTHING in our package to be safe (Crucial for dynamic lookups and serialization)
-keep class com.olivearchi.goodroutine.** { *; }
-keep interface com.olivearchi.goodroutine.** { *; }

# Google Play Services / AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# ML Kit (OCR)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-keep class com.google.android.libraries.vision.** { *; }

# Serializable & Parcelable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Android Framework & Jetpack
-keep class androidx.lifecycle.** { *; }
-keep class androidx.viewbinding.** { *; }
-keep class com.google.android.material.** { *; }
-keep class androidx.appcompat.widget.** { *; }

# Resource preservation
-keepclassmembers class **.R$* {
    public static <fields>;
}
