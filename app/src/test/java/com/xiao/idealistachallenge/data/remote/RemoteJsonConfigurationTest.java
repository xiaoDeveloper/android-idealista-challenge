package com.xiao.idealistachallenge.data.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import kotlinx.serialization.json.Json;
import org.junit.Test;

public class RemoteJsonConfigurationTest {

    @Test
    public void jsonConfigurationIgnoresUnknownFieldsWithoutCoercingMissingValues() throws Exception {
        Class<?> configurationClass = Class.forName(
                "com.xiao.idealistachallenge.data.remote.RemoteJson");
        Field singleton = configurationClass.getField("INSTANCE");
        Method instance = configurationClass.getMethod("getInstance");
        Json json = (Json) instance.invoke(singleton.get(null));

        assertTrue(json.getConfiguration().getIgnoreUnknownKeys());
        assertFalse(json.getConfiguration().getCoerceInputValues());
    }
}
