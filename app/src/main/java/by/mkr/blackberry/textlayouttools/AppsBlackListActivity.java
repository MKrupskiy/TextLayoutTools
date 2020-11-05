package by.mkr.blackberry.textlayouttools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;



enum SortOrder {
    TitleAsc,
    TitleDesc,
    StatusAsc,
    StatusDesc;
}


public class AppsBlackListActivity extends AppCompatActivity {
    private List<AppsBlackListItem> _values;
    private List<AppsBlackListItem> _filteredValues;
    private SwipeRefreshLayout _swipeContainer;
    private AppsBlackListAdapter _appsBlackListAdapter;
    private SortOrder _sortOrder;
    private RecyclerView _appsBlackListView;
    private SearchView _searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appblacklist);

        _swipeContainer = findViewById(R.id.appblacklist_swipe_container);
        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                _swipeContainer.setRefreshing(false);
            }
        });


        _appsBlackListView = findViewById(R.id.appblacklist_view);
        _appsBlackListView.setHasFixedSize(true);
        _appsBlackListView.setLayoutManager(new LinearLayoutManager(this));
        _appsBlackListView.setItemAnimator(new DefaultItemAnimator());



        // Pre-Fetch a list of apps
        _sortOrder = SortOrder.TitleAsc;
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadData() {
        try {
            _swipeContainer.setRefreshing(true);

            AppSettings appSettings = ReplacerService.getAppSettings();
            if (appSettings == null) {
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Get items async
                        _values = getDeviceApps(false);
                        _filteredValues = new ArrayList(_values);
                        sort(_sortOrder);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update the view
                                updateViewWithValues();

                                _swipeContainer.setRefreshing(false);
                            }
                        });

                    } catch (Exception ex) {
                        Log.d(LOG_TAG, "! Ex Thread: " + ex.toString());
                        _swipeContainer.setRefreshing(false);
                    }
                }
            }).start();
        } catch (Exception ex) {
            Log.d(LOG_TAG, "! Ex: " + ex.toString());
        }
    }

    private void updateViewWithValues() {
        final AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings == null) {
            return;
        }

        _appsBlackListAdapter = new AppsBlackListAdapter(_filteredValues);
        _appsBlackListAdapter.setListener(new AppsBlackListAdapter.AppsBlackListListener() {
            @Override
            public void onItemStateChanged(int itemIndex, BlacklistItemBlockState newState) {
                _filteredValues.get(itemIndex).state = newState;
                updateAppStateByPackageName(_filteredValues.get(itemIndex).packageName, _filteredValues.get(itemIndex).state);

                appSettings.updateAppsBlackListAutocorrect(filterByState(_values, BlacklistItemBlockState.Autocorrect));
                appSettings.updateAppsBlackListAll(filterByState(_values, BlacklistItemBlockState.All));
                _appsBlackListAdapter.notifyDataSetChanged();
            }
        });
        _appsBlackListView.setAdapter(_appsBlackListAdapter);
        _appsBlackListView.scheduleLayoutAnimation();
    }

    private void updateAppStateByPackageName(String packageName, BlacklistItemBlockState newState) {
        for (AppsBlackListItem item : _values) {
            if (item.packageName == packageName) {
                item.state = newState;
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blacklist, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        _searchView = (SearchView) searchItem.getActionView();
        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (_values != null) {
                    newText = newText.toLowerCase().trim();

                    _filteredValues = filter(newText, _values);
                    sort(_sortOrder);
                    updateViewWithValues();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help: {
                //Snackbar.make(findViewById(R.id.appblacklist_view),"action_help", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                showHelpDialog(this);
                return true;
            }
            case R.id.action_sort: {
                if (_values != null) {
                    _sortOrder = _sortOrder == SortOrder.TitleAsc
                            ? SortOrder.StatusAsc
                            : SortOrder.TitleAsc;
                    item.setIcon(_sortOrder == SortOrder.TitleAsc ? R.drawable.ic_sort_title_white : R.drawable.ic_sort_status_white);
                    item.setTitle(_sortOrder == SortOrder.TitleAsc ? R.string.text_menu_action_sorttitle : R.string.text_menu_action_sortstatus);
                    sort(_sortOrder);
                    _appsBlackListAdapter.notifyDataSetChanged();
                    _appsBlackListView.scheduleLayoutAnimation();
                }
                return true;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!_searchView.isIconified()) {
            _searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }


    private List<String> filterByState(List<AppsBlackListItem> items, BlacklistItemBlockState targetState) {
        List<String> mappedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).state == targetState) {
                mappedItems.add(items.get(i).packageName);
            }
        }
        return mappedItems;
    }

    private List<AppsBlackListItem> getDeviceApps(boolean includeSystem) {
        List<AppsBlackListItem> runningApps = new ArrayList<AppsBlackListItem>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            //system apps! get out
            if (/*(packageInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0
                    &&*/ (includeSystem || (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                    || packageInfo.packageName.equals("com.android.chrome")
            ) {
                try {
                    String appLabel = pm.getApplicationLabel(packageInfo).toString();
                    Drawable ico = pm.getApplicationIcon(packageInfo.packageName);
                    runningApps.add(new AppsBlackListItem(packageInfo.packageName, appLabel, ico, getPackageState(packageInfo.packageName)));
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "! Ex: " + packageInfo.packageName + "; " + ex.getMessage());
                }
            }
        }
        return runningApps;
    }


    private List<AppsBlackListItem> filter(String filterText, List<AppsBlackListItem> items) {
        if (filterText != null && filterText.length() > 0) {
            List<AppsBlackListItem> newList = new ArrayList<AppsBlackListItem>();
            for (AppsBlackListItem item : items) {
                if (item.label.toLowerCase().contains(filterText)
                        || item.packageName.toLowerCase().contains(filterText)) {
                    newList.add(item);
                }
            }
            return newList;
        } else {
            return new ArrayList<>(items);
        }
    }

    private void sort(SortOrder sortOrder) {
        if (_values == null || _filteredValues == null) return;

        switch (sortOrder) {
            case TitleAsc:
                sortByTitle(true);
                break;
            case TitleDesc:
                sortByTitle(false);
                break;
            case StatusAsc:
                sortByStatus(true);
                break;
            case StatusDesc:
                sortByStatus(false);
                break;
            default:
                break;
        }
    }

    private void sortByTitle(final boolean asc) {
        _sortOrder = asc ? SortOrder.TitleAsc : SortOrder.TitleDesc;
        _filteredValues.sort(new Comparator<AppsBlackListItem>() {
            @Override
            public int compare(AppsBlackListItem o1, AppsBlackListItem o2) {
                return o1.label.compareToIgnoreCase(o2.label) * (asc ? 1 : -1);
            }
        });
    }

    private void sortByStatus(final boolean asc) {
        _sortOrder = asc ? SortOrder.StatusAsc : SortOrder.StatusDesc;
        _filteredValues.sort(new Comparator<AppsBlackListItem>() {
            @Override
            public int compare(AppsBlackListItem o1, AppsBlackListItem o2) {
                return o1.state.compareTo(o2.state) * (asc ? -1 : 1);
            }
        });
    }

    private String getPackageLabel(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(applicationInfo).toString();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "! Ex: Can't get label for " + packageName);
        }
        return "";
    }

    private Drawable getPackageIcon(String packageName) {
        try {
            Drawable ico = getPackageManager().getApplicationIcon(packageName);
            return ico;
        } catch (Exception ex) {
            Log.e(LOG_TAG, "! Ex: Can't get icon for " + packageName);
        }
        return null;
    }

    private BlacklistItemBlockState getPackageState(String packageName) {
        BlacklistItemBlockState state = BlacklistItemBlockState.None;
        AppSettings appSettings = ReplacerService.getAppSettings();

        if (appSettings.appsBlackListAll.contains(packageName)) {
            state = BlacklistItemBlockState.All;
        } else if (appSettings.appsBlackListAutocorrect.contains(packageName)) {
            state = BlacklistItemBlockState.Autocorrect;
        }
        return state;
    }


    public static void showHelpDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptsView = inflater.inflate(R.layout.content_blacklist_help, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(promptsView);

        builder
                .setTitle(R.string.text_menu_action_help)
                .setMessage(R.string.text_help_dialog_title)
                .setCancelable(true)
                //.setPositiveButton(R.string.dialog_ok, null)
                .setNegativeButton(R.string.dialog_cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
