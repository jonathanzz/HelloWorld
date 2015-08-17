package com.example.hellowwifi;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity {
	ArrayAdapter<String> arrayWifiAdapter;
	List<ScanResult> distinctResults = new ArrayList<ScanResult>();
	WifiManager mWifiManager ;
	String ssid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myClick(v); /* my method to call new intent or activity */
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void myClick(View v) {
		ListView lv = (ListView) findViewById(R.id.listView1);
		
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!mWifiManager.isWifiEnabled())
			if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
				mWifiManager.setWifiEnabled(true);
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"SSID_OF_NETOWRK\"";
		config.allowedKeyManagement.set(KeyMgmt.NONE);
		config.status = WifiConfiguration.Status.ENABLED;
		// int netId = mWifiManager.addNetwork(config);
		mWifiManager.saveConfiguration();
		mWifiManager.reconnect();
		mWifiManager.startScan();
		final List<ScanResult> results = mWifiManager.getScanResults();

		List<String> data = new ArrayList<String>();
		data = distinct(results);
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, data);
		lv.setAdapter(ad);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@SuppressLint("NewApi")
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ssid = "\"" + distinctResults.get(position).SSID + "\"";
				dialog(position);
			}

		});

	}

	public List<String> distinct(List<ScanResult> results) {
		List<String> data = new ArrayList<String>();

		for (ScanResult result : results) {
			int n = 0;
			for (ScanResult distRes : distinctResults) {
				n++;
				if (result.SSID.equals(distRes.SSID)) {
					n = 0;
					if (result.level > distRes.level) {
						distinctResults.add(result);
						distinctResults.remove(distRes);
					}
					break;
				}
			}
			if (n == distinctResults.size())
				distinctResults.add(result);
		}
		int n = 0;
		for (ScanResult distRes : distinctResults) {
			data.add(String.valueOf(++n) + distRes.SSID + "--" + distRes.level);
		}
		return data;
	}

	@SuppressLint("InflateParams")
	protected void dialog(final int position) {
		Builder dialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.dialogview, null);
		dialog.setView(layout);
		final Builder dialog1 = new AlertDialog.Builder(this);
		LayoutInflater inflater1 = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout layout1 = (LinearLayout) inflater1.inflate(
				R.layout.dialog, null);
		final EditText et2 = (EditText) layout.findViewById(R.id.username);
		final EditText et1 = (EditText) layout.findViewById(R.id.password);

		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String username = et2.getText().toString();
				String password = et1.getText().toString();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("username", username);
				intent.putExtras(bundle);
				bundle.putString("password", password);
				intent.putExtras(bundle);
				WifiConfiguration c = new WifiConfiguration();
				c.allowedAuthAlgorithms.clear();
				c.allowedGroupCiphers.clear();
				c.allowedKeyManagement.clear();
				c.allowedPairwiseCiphers.clear();
				c.allowedProtocols.clear();
				c.SSID = ssid;
				c.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
				c.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
				WifiEnterpriseConfig wepc = new WifiEnterpriseConfig();
				System.out.println(username + "hehe" + password);
				wepc.setIdentity(username);
				wepc.setPassword(password);
				wepc.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
				c.enterpriseConfig = wepc;
				c.hiddenSSID = false;
				c.status = WifiConfiguration.Status.ENABLED;
				int wcgID = mWifiManager.addNetwork(c);
				mWifiManager.enableNetwork(wcgID, true);
				String succ = "SSID: " + distinctResults.get(position).SSID
						+ "/nIP address: "
						+ distinctResults.get(position).BSSID;
				dialog1.setView(layout1);
				TextView tv1 = (TextView) layout1.findViewById(R.id.textView1);
				tv1.setText(succ);
				dialog1.setNegativeButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog1,
									int which) {
							}
						});
				dialog1.show();

			}
		});
		dialog.setNegativeButton("cencel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		dialog.show();
	}

}
