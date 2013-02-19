package com.moneydesktop.finance.shared;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.views.CaretView;
import com.moneydesktop.finance.views.VerticalTextView;

public class TransactionViewHolder {

	public RelativeLayout root;
	public LinearLayout cell;
	
    public VerticalTextView newText;
    public TextView date;
    public TextView payee;
    public TextView category;
    public TextView amount;
    public ImageView type;
    public ImageView flag;
    public CaretView caret;
    public TextView title;
    public TextView dollar;
}
