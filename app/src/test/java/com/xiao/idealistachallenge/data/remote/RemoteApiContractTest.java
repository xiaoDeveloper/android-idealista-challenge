package com.xiao.idealistachallenge.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import org.junit.Test;
import retrofit2.http.GET;

public class RemoteApiContractTest {

    @Test
    public void apiUsesTheChallengeEndpointsWithoutASelectedAdParameter() throws Exception {
        Class<?> apiClass = Class.forName("com.xiao.idealistachallenge.data.remote.IdealistaApi");

        assertTrue(apiClass.isInterface());
        Method listAds = suspendMethod(apiClass, "listAds");
        Method getDetails = suspendMethod(apiClass, "getDetails");

        assertEquals("list.json", listAds.getAnnotation(GET.class).value());
        assertEquals("detail.json", getDetails.getAnnotation(GET.class).value());
        assertEquals("kotlin.coroutines.Continuation", getDetails.getParameterTypes()[0].getName());
    }

    private static Method suspendMethod(Class<?> apiClass, String name) throws NoSuchMethodException {
        for (Method method : apiClass.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }
}
