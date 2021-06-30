package com.yuan7.rpcserver.handler.douyin;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.yuan7.rpcserver.utils.Store;
import com.yuan7.rpcserver.utils.TraceUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public class DySearchGoods implements HttpServerRequestCallback {
    private final Gson gson = new Gson();

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        String query = request.getQuery().getString("query");
        String page = request.getQuery().getString("page");
        ClassLoader classLoader = Store.appClassLoader.get("dy");
        Class <?> SearchCommodityAggregateApi = XposedHelpers.findClass("com.ss.android.ugc.aweme.discover.commodity.api.SearchCommodityAggregateApi", classLoader);
        Object IRetrofit = XposedHelpers.getStaticObjectField(SearchCommodityAggregateApi, "a");
        Class <?> RealApiClass = XposedHelpers.findClass("com.ss.android.ugc.aweme.discover.commodity.api.SearchCommodityAggregateApi$RealApi", classLoader);
        Object realApiObj = XposedHelpers.callMethod(IRetrofit, "create", RealApiClass);
        Object zz = XposedHelpers.callMethod(realApiObj, "searchCommodityAggregate", query, Integer.valueOf(page), 20, 1, 1, "switch_tab", "homepage_hot", "", "", "");
        Store.appAsyncHttpResponse.put("dy", response);
        XposedHelpers.callMethod(zz,"continueWith",Continuation(classLoader));
    }
    public static Object Continuation(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, new Class[]{XposedHelpers.findClass("bolts.Continuation", classLoader)}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("then")) {
                    Object task = args[0];
                    Gson gson = new Gson();
                    JSONObject object = JSON.parseObject(gson.toJson(XposedHelpers.callMethod(task,"getResult")));
                    if(object.containsKey("complete")&&object.containsKey("continuations")){
                        TraceUtil.e("StartUrl:complete has");
                    }else{
                        TraceUtil.e("StartUrl no:"+object.toJSONString());
//                        Store.appObject.put("dy", object.toJSONString());
                    }
                    AsyncHttpServerResponse response = Store.appAsyncHttpResponse.remove("dy");
                    if (response != null) {
                        response.send(gson.toJson(object));
                    }else {
                        response.send(gson.toJson("{\"ret\":\"no response\"}"));
                    }
                    return null;
                }
                return null;
            }
        });
    }
}
