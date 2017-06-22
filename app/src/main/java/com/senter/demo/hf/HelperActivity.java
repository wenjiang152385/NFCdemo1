package com.senter.demo.hf;

import com.senter.demo.hf.commons.nfc.PrivateUtils;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelperActivity extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_helper);
		
		webView = (WebView) findViewById(R.id.helper_webview);
		webView.setBackgroundColor(PrivateUtils.GB_HOLO_LIGHT);
		webView.loadUrl(getString(R.string.help_url));
		
	}
}
