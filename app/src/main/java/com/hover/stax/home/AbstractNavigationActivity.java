package com.hover.stax.home;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.Constants;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.settings.SettingsFragment;
import com.hover.stax.utils.UIHelper;

public abstract class AbstractNavigationActivity extends AppCompatActivity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void setUpNav() {
		BottomAppBar nav = findViewById(R.id.nav_view);
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
		NavigationUI.setupWithNavController(nav, navController, appBarConfiguration);

		nav.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) item -> {
			checkPermissionsAndNavigate(getNavConst(item.getItemId()));
			return true;
		});

		navController.addOnDestinationChangedListener((controller, destination, arguments) -> setActiveNav(destination.getId(), nav));

		if (getIntent().getBooleanExtra(SettingsFragment.LANG_CHANGE, false))
			navController.navigate(R.id.navigation_settings);
	}

	private void setActiveNav(int destinationId, BottomAppBar nav) {
		UIHelper.changeDrawableColor(nav.findViewById(R.id.navigation_home), destinationId == R.id.navigation_home ? R.color.brightBlue : R.color.offWhite, this);
		UIHelper.changeDrawableColor(nav.findViewById(R.id.navigation_balance), destinationId == R.id.navigation_balance ? R.color.brightBlue : R.color.offWhite, this);
		UIHelper.changeDrawableColor(nav.findViewById(R.id.navigation_settings), destinationId == R.id.navigation_settings ? R.color.brightBlue : R.color.offWhite, this);
	}

	void checkPermissionsAndNavigate(int toWhere) {
		PermissionHelper permissionHelper = new PermissionHelper(this);
		if (toWhere == Constants.NAV_SETTINGS || toWhere == Constants.NAV_HOME || permissionHelper.hasBasicPerms()) {
			navigate(toWhere);
		} else
			PermissionUtils.showInformativeBasicPermissionDialog(
				pos -> PermissionUtils.requestPerms(getNavConst(toWhere), AbstractNavigationActivity.this),
				neg -> Amplitude.getInstance().logEvent(getString(R.string.perms_basic_cancelled)),
				this);
	}

	private void navigate(int toWhere) {
		switch (toWhere) {
			case Constants.NAV_TRANSFER:
				startTransfer(Action.P2P, false, getIntent());
				break;
			case Constants.NAV_AIRTIME:
				startTransfer(Action.AIRTIME, false, getIntent());
				break;
			case Constants.NAV_REQUEST:
				startActivityForResult(new Intent(this, RequestActivity.class), Constants.REQUEST_REQUEST);
				break;
			case Constants.NAV_HOME:
				Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.navigation_home);
				break;
			case Constants.NAV_BALANCE:
				Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.navigation_balance);
				break;
			case Constants.NAV_SETTINGS:
				Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.navigation_settings);
				break;
			default:
				break;
		}
	}

	public abstract void startTransfer(String type, boolean isFromStaxLink, Intent received);

	private int getNavConst(int destId) {
		if (destId == R.id.navigation_balance) return Constants.NAV_BALANCE;
		else if (destId == R.id.navigation_settings) return Constants.NAV_SETTINGS;
		else if (destId == R.id.navigation_home) return Constants.NAV_HOME;
		else return destId;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionUtils.logPermissionsGranted(grantResults, this);
		checkPermissionsAndNavigate(requestCode);
	}
}
