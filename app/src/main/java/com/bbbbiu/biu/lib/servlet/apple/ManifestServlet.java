package com.bbbbiu.biu.lib.servlet.apple;

import android.content.Context;
import android.util.Log;

import com.bbbbiu.biu.gui.transfer.FileItem;
import com.bbbbiu.biu.gui.transfer.apple.ReceivingActivity;
import com.bbbbiu.biu.lib.util.HttpConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yieldnull.httpd.HttpDaemon;
import com.yieldnull.httpd.HttpRequest;
import com.yieldnull.httpd.HttpResponse;
import com.yieldnull.httpd.HttpServlet;

import java.util.ArrayList;

/**
 * 接收IOS端要传来的文件清单
 * <p/>
 * Created by YieldNull at 5/9/16
 */
public class ManifestServlet extends HttpServlet {
    private static final String TAG = ManifestServlet.class.getSimpleName();


    public static void register(Context context) {
        HttpDaemon.registerServlet(HttpConstants.Apple.URL_MANIFEST, new ManifestServlet(context));
    }


    private Context context;

    private ManifestServlet(Context context) {
        this.context = context;
    }

    @Override
    public HttpResponse doGet(HttpRequest request) {
        return null;
    }

    @Override
    public HttpResponse doPost(HttpRequest request) {
        Gson gson = new Gson();

        String json = request.text();
        ArrayList<FileItem> manifest = gson.fromJson(json, new TypeToken<ArrayList<FileItem>>() {
        }.getType());

        Log.i(TAG, json);

        ReceivingActivity.startReceiving(context, manifest);
        return HttpResponse.newResponse("");
    }
}