package com.senter.demo.hf;

import com.senter.demo.hf.commons.nfc.NFCBaseActivity;
import com.senter.demo.hf.commons.nfc.NFCs;
import com.senter.demo.hf.commons.nfc.PrivateUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

/**
 * 主界面
 * 
 * @author neo
 * 
 */
public class MainActivity extends NFCBaseActivity {

	private TextView infosTextView;
	private Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		infosTextView = (TextView) findViewById(R.id.infos);

		try {
			// [Neo] 获取当前版本信息
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_ACTIVITIES);
			infosTextView.setText(String.format(
					getString(R.string.infos_formatter), info.versionName,
					info.versionCode));
		} catch (NameNotFoundException e) {
			infosTextView.setVisibility(View.GONE);
		}

	}

	public void onShowReader(View view) {
		startActivity(new Intent(MainActivity.this, ReaderActivity.class));
	}

	public void onShowWriter(View view) {
		startActivity(new Intent(MainActivity.this, WriterActivity.class));
	}

	public void onShowViewer(View view) {
		startActivity(new Intent(MainActivity.this, ViewerActivity.class));
	}

	public void onShowHelper(View view) {
		startActivity(new Intent(MainActivity.this, HelperActivity.class));
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (null == adapter || false == adapter.isEnabled()) {
			findViewById(R.id.reader).setEnabled(false);
			findViewById(R.id.writer).setEnabled(false);

			if (null != dialog && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}

			dialog = new AlertDialog.Builder(MainActivity.this,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK)
					.setTitle(R.string.warning)
					.setMessage(R.string.msg_nfc_not_working)
					.setPositiveButton(R.string.settings,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Settings.ACTION_NFC_SETTINGS));
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// [Neo] Empty

								}
							}).create();
			dialog.show();

		} else {
			findViewById(R.id.reader).setEnabled(true);
			findViewById(R.id.writer).setEnabled(true);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (isTagDiscovered) {
			String msg = null;
			if (techMap.containsKey("isWritable")
					&& "true".equals(techMap.get("isWritable"))) {
				msg = String.format(
						getString(R.string.msg_tag_discovered_rw_formatter),
						techMap.get("uid").replace(":", " "));
			} else {
				msg = String.format(
						getString(R.string.msg_tag_discovered_r_formatter),
						techMap.get("uid").replace(":", " "));
			}

			if (null != dialog && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}

			dialog = new AlertDialog.Builder(MainActivity.this,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK)
					.setTitle(R.string.tips)
					.setMessage(msg)
					.setPositiveButton(R.string.write,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(MainActivity.this,
											WriterActivity.class));
								}
							})
					.setNeutralButton(R.string.read,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(MainActivity.this,
											ReaderActivity.class));
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// [Neo] Empty

								}
							}).create();

			dialog.show();

		}
	}

	@Override
	protected void onDestroy() {
		if (null != dialog && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}

		super.onDestroy();

		if (null == PrivateUtils.SQLS) {
			PrivateUtils.initDB(MainActivity.this);
		}

		NFCs.clear();
		PrivateUtils.SQLS.execSQL("DELETE FROM tmp");
		PrivateUtils.SQLS.close();
		PrivateUtils.SQLS = null;
	}
}
