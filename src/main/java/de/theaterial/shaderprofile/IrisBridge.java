package de.theaterial.shaderprofile;

import java.lang.reflect.Method;

/** Spricht die Iris-API per Reflection an, damit Iris keine harte Abhängigkeit ist. */
public final class IrisBridge {
    private IrisBridge() {}

    private static Object config() throws Exception {
        Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        Object api = apiClass.getMethod("getInstance").invoke(null);
        return apiClass.getMethod("getConfig").invoke(api);
    }

    public static boolean isAvailable() {
        try {
            config();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean areShadersEnabled() {
        try {
            Class<?> cfgClass = Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig");
            Method m = cfgClass.getMethod("areShadersEnabled");
            return (boolean) m.invoke(config());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean setShadersEnabled(boolean enabled) {
        try {
            Class<?> cfgClass = Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig");
            Method m = cfgClass.getMethod("setShadersEnabledAndApply", boolean.class);
            m.invoke(config(), enabled);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
