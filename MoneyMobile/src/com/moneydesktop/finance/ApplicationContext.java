package com.moneydesktop.finance;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.BusinessObjectBase;
import com.moneydesktop.finance.database.DaoMaster;
import com.moneydesktop.finance.database.DaoMaster.DevOpenHelper;
import com.moneydesktop.finance.database.DaoSession;
import com.moneydesktop.finance.database.DatabaseDefaults;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.exception.CustomExceptionHandler;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.AuthEvent;
import com.moneydesktop.finance.model.EventMessage.LoginEvent;
import com.moneydesktop.finance.util.Enums.LockType;

import de.greenrobot.event.EventBus;

public class ApplicationContext extends Application {
	
	private final String TAG = "ApplicationContext";

	final static String WAKE_TAG = "wake_lock";

	private static ApplicationContext instance;
	
	private static ObjectMapper mapper;
	
    private static SQLiteDatabase db;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
	
	private static WakeLock wl;
	
	private static boolean screenOn = true;
	private BroadcastReceiver screenLock = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				screenOn = true;
			}
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				screenOn = false;
			}
			
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) && screenOn && BaseActivity.inForeground) {
				
				String code = Preferences.getString(Preferences.KEY_LOCK_CODE, "");
				if (!code.equals("")) {
					showLockScreen();
				}
			}
		}
	};
	
	public void onCreate() {
		super.onCreate();
		
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        initializeDatabase();
		DatabaseDefaults.ensureInstitutionsLoaded();
		
		EventBus.getDefault().register(this);
		
        getObjectMapper();

    	registerScreenLock();
		acquireWakeLock();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		EventBus.getDefault().unregister(this);
		
		Log.i(TAG, "Savig BusinessObjectBase ID count");
		Preferences.saveLong(Preferences.KEY_BOB_ID, BusinessObjectBase.getIdCount());
		
		unregisterReceiver(screenLock);
		releaseWakeLock();
	}
	
	private void initializeDatabase() {

        DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "finance-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
	}
	
	private void registerScreenLock() {

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
    	registerReceiver(screenLock, filter);
	}
	
	private void showLockScreen() {
		
		EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.LOCK));
	}
	
	/*********************************************************************************
	 * Event Listening
	 *********************************************************************************/
	
	public void onEvent(LoginEvent event) {
		
		SyncEngine.sharedInstance().setNeedsFullSync(true);
		SyncEngine.sharedInstance().beginSync();
	}
	
	public void onEvent(AuthEvent event) {
		
		Institution.processLocalBanksFile(R.raw.institutions);
	}
	
	/*********************************************************************************
	 * Getters
	 *********************************************************************************/
	
    public ApplicationContext() {
        instance = this;
    }
    
    public static Context getContext() {
        return instance;
    }
    
    public static ObjectMapper getObjectMapper() {
    	
    	if (mapper == null) {
    		mapper = new ObjectMapper();
    		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    	}
    	
    	return mapper;
    }

	public static SQLiteDatabase getDb() {
		return db;
	}

	public static DaoMaster getDaoMaster() {
		return daoMaster;
	}

	public static DaoSession getDaoSession() {
		return daoSession;
	}
	
	/*********************************************************************************
	 * Misc.
	 *********************************************************************************/

	public void acquireWakeLock() {
			
		PowerManager pm = (PowerManager) ApplicationContext.getContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKE_TAG);
		wl.acquire();
	}

	private void releaseWakeLock() {
		
		if (wl != null) {
			
			wl.release();
			wl = null;
		}
	}
}
