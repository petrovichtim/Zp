package com.rusdelphi.zp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {
	 Context ctx;

	public MySimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		ctx = context;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		// TextView tv = (TextView) arg0.findViewById(R.id.name);
		// tv.setText(arg2.getString(arg2.getColumnIndex("name")));
		ImageView iv = (ImageView) arg0.findViewById(R.id.imageView1);
		if (!arg2.getString(arg2.getColumnIndex("sum")).equals("0"))
			iv.setImageResource(android.R.drawable.checkbox_on_background);
		else
			iv.setImageResource(android.R.drawable.ic_menu_help);
		ImageButton button = (ImageButton) arg0
				.findViewById(R.id.imageButtonPlus);
		Object obj = arg2.getInt(arg2.getColumnIndex("_id"));
		button.setTag(obj);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v != null) {
					Object obj = v.getTag();
					if (obj != null && obj instanceof Integer) {
						Number n = (Number) obj;
						// return n.longValue();
						Main.mExpenses_id = n.longValue(); // obj. ((long)
															// obj).longValue();
						mCursor.moveToFirst();
						mCursor.move((int) Main.mExpenses_id - 1);

						// Intent intent = new Intent(
						// getApplicationContext(),
						// Ac_item_view_Activity.class);
						Main.Name_item = mCursor.getString(mCursor
								.getColumnIndex("name"));
						Main.Desc_item = mCursor.getString(mCursor
								.getColumnIndex("description"));
						Main.Sum_item = mCursor.getString(mCursor
								.getColumnIndex("sum"));

						Main.mEx_id = mCursor.getInt(mCursor
								.getColumnIndex("ex_id"));
						// Main.mExpenses_id = Main.mExpenses_id;

						// startActivity(intent);
						/*
						 * Toast.makeText( getApplicationContext(),
						 * "Delete row with id = " + ((Integer) obj).intValue(),
						 * Toast.LENGTH_LONG).show();
						 */
						Fragment f = Ac_item_view_fragment.getInstance();
						FragmentTransaction ft = ((FragmentActivity) ctx).getSupportFragmentManager().beginTransaction();
						ft.replace(R.id.content, f);
						ft.addToBackStack(null);
						ft.commit();
					}
				}

			}
		});
		super.bindView(arg0, arg1, arg2);
	}
}
