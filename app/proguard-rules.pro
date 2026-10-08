# kotlinx.serialization, Retrofit and Navigation ship their own consumer rules.
# Keep our wire models and navigation routes explicitly as a safety net.
-keep class dev.sathish.learningdashboard.data.remote.dto.** { *; }
-keep class dev.sathish.learningdashboard.ui.navigation.** { *; }
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
