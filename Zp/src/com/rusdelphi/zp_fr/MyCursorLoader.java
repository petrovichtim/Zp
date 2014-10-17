package com.rusdelphi.zp_fr;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

public class MyCursorLoader extends CursorLoader {

	DbAdapter mDb;
	int actype;

	public MyCursorLoader(Context context, DbAdapter db, int accounts) {
		super(context);
		this.mDb = db;
		actype=accounts;
	}

	@Override
	public Cursor loadInBackground() {

		return mDb.getAccounts(actype);
	}

}