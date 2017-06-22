package com.senter.demo.hf.commons.nfc;

import java.util.HashMap;

import com.senter.demo.hf.commons.SQLites;

import android.content.Context;
import android.graphics.Color;

/**
 * NFC 私有工具
 * 
 * @author neo
 *
 */
public class PrivateUtils {

	public static SQLites SQLS;

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "nfc-demo.db";

	public static final String TABLE_SAVED = "saved";
	public static final String TABLE_TMP = "tmp";

	private static final String SCHEMA_TABLE_DATA = "id INTEGER PRIMARY KEY, uid TEXT, time DATETIME, content TEXT";

	public static String LAST_TMP_DATE;
	public static String LAST_SAVED_DATE;

	public static final int GB_HOLO_LIGHT = Color.rgb(233, 233, 233);

	private static final HashMap<String, String> TABLES_MAP = new HashMap<String, String>();

	static {
		TABLES_MAP.put(TABLE_SAVED, SCHEMA_TABLE_DATA);
		TABLES_MAP.put(TABLE_TMP, SCHEMA_TABLE_DATA);
	}

	/**
	 * 初始化数据库
	 * 
	 * @param context
	 */
	public static void initDB(Context context) {
		if (null == SQLS) {
			SQLS = new SQLites();
			SQLS.open(context, DB_NAME, TABLES_MAP, DB_VERSION);
		}
	}
}
