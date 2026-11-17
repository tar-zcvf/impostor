# Masked - app-specific ProGuard rules
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keep class com.maskedparty.game.data.** { *; }
-keep class com.maskedparty.game.game.** { *; }
