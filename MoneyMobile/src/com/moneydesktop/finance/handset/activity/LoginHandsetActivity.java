package com.moneydesktop.finance.handset.activity;

import android.content.Intent;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.activity.LoginBaseActivity;

public class LoginHandsetActivity extends LoginBaseActivity {

    @Override
    protected int getContentResource() {
        return R.layout.handset_login_view;
    }

    @Override
    protected Intent getDashboardIntent() {
        return new Intent(this, DashboardHandsetActivity.class);
    }

    @Override
    protected Intent getLoginIntent() {
        return new Intent(this, IntroHandsetActivity.class);
    }
}
