package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;
import android.os.Build;

public class AsyncTaskHelper {
    @SafeVarargs
    public static <Params,Progress,Result> void StartAsyncTask(AsyncTask<Params,Progress,Result> asyncTask, Params... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

}
