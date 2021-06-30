package com.yuan7.rpcserver.utils;

import android.content.pm.PackageInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AntiTools {
    public static String LogTag = "RPC";

    public static void generalAnti(XC_LoadPackage.LoadPackageParam loadPackageParam){
        try {
            XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages",Integer.TYPE, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                    List<PackageInfo> packageInfoListNew = new ArrayList<>();
                    for (PackageInfo packageInfo : (List<PackageInfo>) param.getResult()) {
                        if (!"de.robv.android.xposed.installer".equals(packageInfo.packageName) && !packageInfo.packageName.contains("org.meowcat.edxposed.manager")) {
                            packageInfoListNew.add(packageInfo);
                        }
                    }
                    param.setResult(packageInfoListNew);
                }
            });
            XposedHelpers.findAndHookMethod(StackTraceElement.class, "getClassName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String result = (String) param.getResult();
                    if (result != null){
                        if (result.contains("de.robv.android.xposed")) {
                            param.setResult("");
                        }else if(result.contains("com.android.internal.os.ZygoteInit")){
                            param.setResult("");
                        }else if(result.contains("fi.iki.elonen")){
                            param.setResult("");
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(LogTag,e.getMessage());
        }
    }
}
