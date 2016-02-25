package com.murraycole.appusagesample;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 3/2/15.
 */
public class UStats {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
    public static final String TAG = "stats";

    public static List<UsageStats> getUsageStatsList(Context context){

        PackageManager pm = context.getPackageManager();

        //printInstalledApps(pm);

        UsageStatsManager usm = getUsageStatsManager(context);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -24);
        long startTime = calendar.getTimeInMillis();

        Log.d(TAG, "Range start:" + dateFormat.format(startTime) );
        Log.d(TAG, "Range end:" + dateFormat.format(endTime));

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        return usageStatsList;
    }

    private static void printInstalledApps(PackageManager pm) {
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            Log.d(TAG, "App name :" + packageInfo.loadLabel(pm));
            Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
            Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
        }
    }

    public static void printUsageStats(List<UsageStats> usageStatsList, Context context){
        final PackageManager pm = context.getPackageManager();

        Map<String, Long> stats = buildConsolidatedStats(usageStatsList, pm);

        for (String appName : stats.keySet()) {
            Long appMs = stats.get(appName);
            long minutes = calcMinutes(appMs);
            if(minutes > 1) {
                Log.d(
                        TAG,
                        "App: " + appName +
                        "\n" + "ForegroundTime: "
                        + minutes);
            }

        }
    }

    private static Map<String, Long> buildConsolidatedStats(List<UsageStats> usageStatsList, PackageManager pm) {
        Map<String, Long> consolidated = new HashMap<>();
        for (UsageStats usage : usageStatsList) {
            ApplicationInfo applicationInfo = getApplicationInfo(pm, usage);
            String appName = (String) applicationInfo.loadLabel(pm);
            if(consolidated.containsKey(appName)) {
                long usageMs = consolidated.get(appName);
                usageMs += usage.getTotalTimeInForeground();
                consolidated.put(appName, usageMs);
            } else {
                consolidated.put(appName, usage.getTotalTimeInForeground());
            }
        }
        return consolidated;
    }

    private static ApplicationInfo getApplicationInfo(PackageManager pm, UsageStats usage) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(usage.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static long calcMinutes(long millis) {
        return (millis / 1000) / 60;
    }

    public static void printCurrentUsageStatus(Context context){
        printUsageStats(getUsageStatsList(context), context);
    }
    @SuppressWarnings("ResourceType")
    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        return usm;
    }
}
