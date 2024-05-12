package com.xiaohezi.myserialportrealm;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity {
    Realm uiThreadRealm;
    String realmName = "MyRealmDataBase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).build();
        uiThreadRealm = Realm.getInstance(config);
        addChangeListenerToRealm(uiThreadRealm);

        FutureTask<String> Task = new FutureTask(new BackgroundQuickStart(), "TestFutureTask");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(Task);
    }

    private void addChangeListenerToRealm(Realm realm) {
        // all Tasks in the realm
        RealmResults<HistoryData> Tasks = uiThreadRealm.where(HistoryData.class).findAllAsync();
        Tasks.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<HistoryData>>() {
            @Override
            public void onChange(RealmResults<HistoryData> collection, OrderedCollectionChangeSet changeSet) {
                // process deletions in reverse order if maintaining parallel data structures so indices don't change as you iterate
                OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                for (OrderedCollectionChangeSet.Range range : deletions) {
                    Log.v("QUICKSTART", "Deleted range: " + range.startIndex + " to " + (range.startIndex + range.length - 1));
                }
                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    Log.v("QUICKSTART", "Inserted range: " + range.startIndex + " to " + (range.startIndex + range.length - 1));                            }
                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                for (OrderedCollectionChangeSet.Range range : modifications) {
                    Log.v("QUICKSTART", "Updated range: " + range.startIndex + " to " + (range.startIndex + range.length - 1));                            }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // the ui thread realm uses asynchronous transactions, so we can only safely close the realm
        // when the activity ends and we can safely assume that those transactions have completed
        uiThreadRealm.close();
    }
    public class BackgroundQuickStart implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "zwh: **********start");
            RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).build();
            Realm backgroundThreadRealm = Realm.getInstance(config);
            Log.i(TAG, "zwh: **********0");
            HistoryData data1 = new HistoryData(3,33,10,"2024-05-11 15:07:00");
            HistoryData data2 = new HistoryData(1,11,10,"2024-05-12 15:07:00");
            HistoryData data3 = new HistoryData(2,33,10,"2024-05-12 15:07:00");
            Log.i(TAG, "zwh: **********1");
            backgroundThreadRealm.executeTransaction (transactionRealm -> {
                transactionRealm.insert(data1);
            });
            Log.i(TAG, "zwh: **********2");
            // all Tasks in the realm
            RealmResults<HistoryData> Tasks = backgroundThreadRealm.where(HistoryData.class).findAll();
            Log.i(TAG, "zwh: **********"+Tasks.stream().count());
            // you can also filter a collection
            RealmResults<HistoryData> TasksThatBeginWithN = Tasks.where().beginsWith("TimeStamp", "2024-05-12").findAll();
            RealmResults<HistoryData> openTasks = Tasks.where().equalTo("ProbeID", "33").findAll();
            HistoryData otherTask = Tasks.get(0);
            // all modifications to a realm must happen inside of a write block
            backgroundThreadRealm.executeTransaction( transactionRealm -> {
                HistoryData innerOtherTask = transactionRealm.where(HistoryData.class).equalTo("ProbeID", "11").findFirst();
                innerOtherTask.setGasValue(99);
            });
            HistoryData yetAnotherTask = Tasks.get(0);
            // all modifications to a realm must happen inside of a write block
            backgroundThreadRealm.executeTransaction( transactionRealm -> {
                HistoryData innerYetAnotherTask = transactionRealm.where(HistoryData.class).equalTo("ProbeID", yetAnotherTask.getProbeID()).findFirst();
                innerYetAnotherTask.deleteFromRealm();
            });
            // because this background thread uses synchronous realm transactions, at this point all
            // transactions have completed and we can safely close the realm
            backgroundThreadRealm.close();
        }
    }
}