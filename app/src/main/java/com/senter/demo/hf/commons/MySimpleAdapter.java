package com.senter.demo.hf.commons;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 简易适配器
 * 
 * @author neo
 *
 */
public class MySimpleAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<HashMap<String, String>> list;
	private int layoutResId;
	private String[] mapKeys;
	private int[] resIds;

	public MySimpleAdapter(Context context,
			ArrayList<HashMap<String, String>> list, int layoutResId,
			String[] mapKeys, int[] resIds) throws NullPointerException,
			ArrayIndexOutOfBoundsException {
		if (null == context || null == list) {
			throw new NullPointerException();
		}

		if (mapKeys.length != resIds.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		this.context = context;
		this.list = list;
		this.layoutResId = layoutResId;
		this.mapKeys = mapKeys;
		this.resIds = resIds;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).hashCode();
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (null == view) {
			view = LayoutInflater.from(context).inflate(layoutResId, null);
		}
		HashMap<String, String> map = list.get(position);
		TextView textView = null;
		for (int i = 0; i < mapKeys.length; i++) {
			textView = (TextView) view.findViewById(resIds[i]);
			textView.setText(map.get(mapKeys[i]));
			// [Neo] TODO more uri customer
		}
		return view;
	}

	/**
	 * 手动更新
	 * 
	 * @param list
	 */
	public void update(ArrayList<HashMap<String, String>> list) {
		this.list = list;
		notifyDataSetChanged();
	}

}
