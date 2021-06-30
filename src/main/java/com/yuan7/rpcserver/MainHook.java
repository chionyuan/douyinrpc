package com.yuan7.rpcserver;
import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.yuan7.rpcserver.handler.PingHandler;
import com.yuan7.rpcserver.handler.douyin.DySearchGoods;
import com.yuan7.rpcserver.utils.AntiTools;
import com.yuan7.rpcserver.utils.Store;
import com.yuan7.rpcserver.utils.TraceUtil;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam){
        AntiTools.generalAnti(loadPackageParam);
        /**
         * 抖音hook入口
         */
        if(loadPackageParam.processName.equals("com.ss.android.ugc.aweme")){
            TraceUtil.e("===================检测到抖音包名，开始hook============");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if(loadPackageParam.packageName.equals("com.ss.android.ugc.aweme")){
                        final Context context = (Context) param.args[0];
                        Store.appContext.put("dy", context);
                        ClassLoader classLoader = context.getClassLoader();
                        Store.appClassLoader.put("dy", classLoader);
                        TraceUtil.e("===================检测到抖音包名，准备开始注册服务=======");
                        registerServer();
//                        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.base.api.BaseResponse", classLoader, "checkValid", new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//                                Object ret = param.getResult();
//                                TraceUtil.e(String.format("=======================BaseResponse rest %s", ret.toString()));
//                                Gson gson = new Gson();
//                                AsyncHttpServerResponse response = Store.appAsyncHttpResponse.remove("dy");
//                                if (response != null) {
//                                    response.send(gson.toJson(ret));
//                                }else {
//                                    response.send(gson.toJson("{\"ret\":\"no response\"}"));
//                                }
//                            }
//                        });
                    }
                }
            });
        }


    }

    private void registerServer() {
        AsyncHttpServer server = Store.appAsyncHttpServer.get("dy");
        if (server == null) {
            server = new AsyncHttpServer();
            Store.appAsyncHttpServer.put("dy", server);
        }
        server.get("/ping", new PingHandler());
        server.get("/dysearchgoods", new DySearchGoods());
        server.listen(7654);
        TraceUtil.e("dy server registered! port:7654");
    }
}
