# Keep entry points declared in the AndroidManifest (Activity, Service, Receiver).
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep the Quick Settings tile service and its public API.
-keep public class com.inod.dnd.DndTileService {
    public <methods>;
}

# Keep the DndController singleton (object) and its state.
-keep class com.inod.dnd.DndController { *; }
-keep class com.inod.dnd.DndState { *; }

# Preserve annotations, generic signatures and line numbers for sane stack traces.
-keepattributes *Annotation*,Signature,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
