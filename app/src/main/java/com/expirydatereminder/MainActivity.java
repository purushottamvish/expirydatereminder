package com.expirydatereminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.expirydatereminder.Custom.CustomTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogHandler.ExampleDialogListener, SettingsDialogHandler.SettingsDialog, AdapterView.OnItemSelectedListener {

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter, ad;
    private ListView listView;
    private FloatingActionButton refreshButton, addItemButton, settingsButton, helpButton;
    private Button sortButton;
    private DatabaseHandler dbHandler;
    private List<ItemModel> modelList;
    private List<String> categories;

    private CustomTextView filter;
    private ProgressBar progressBar;

    SettingsDatabaseHandler settingsDatabaseHandler;
    DateFormatDatabase dbh;

    private Spinner categorySpinner;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }

        Objects.requireNonNull(getSupportActionBar()).show();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        createNotificationChannel();

        listView = findViewById(R.id.listView);
        refreshButton = findViewById(R.id.refreshButton);
        addItemButton = findViewById(R.id.addItemButton);

        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();

        addItemButton.setOnClickListener(view -> {

            scanCode();
            openDialog();
        });

        dbHandler = new DatabaseHandler(MainActivity.this);
        dbh = new DateFormatDatabase(MainActivity.this);

        modelList = dbHandler.getAllItems();
        modelList.sort(Comparator.comparingInt(ItemModel::getDate));
        modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
        modelList.sort(Comparator.comparingInt(ItemModel::getYear));
        populate(modelList);

        refreshButton.setOnClickListener(view -> {
            scanCode();
        });

        settingsDatabaseHandler = new SettingsDatabaseHandler(MainActivity.this);
        categories = new ArrayList<>();
        categories = settingsDatabaseHandler.getCategories();
        categorySpinner = findViewById(R.id.category_spinner);
        categorySpinner.setOnItemSelectedListener(this);
        ad = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        ad.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(ad);



      /*  settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            openSettingsDialog();
            ad.notifyDataSetChanged();
        });*/


        sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(view -> {
            String txt = sortButton.getText().toString();
            System.out.println("Button clicked");
            System.out.println(txt);
            if (txt.equals("Sort By: Name")) {
                String a = categorySpinner.getSelectedItem().toString();
                if (a.equals("All Items")) {
                    modelList = dbHandler.getAllItems();
                } else modelList = dbHandler.getAllItems(a);
                itemsAdapter.clear();
                modelList.sort(Comparator.comparing(ItemModel::getItem));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
                sortButton.setText("Sort By: Date");
            } else if (txt.contains("Sort By: Date")) {
                String a = categorySpinner.getSelectedItem().toString();
                if (a.equals("All Items")) {
                    modelList = dbHandler.getAllItems();
                } else modelList = dbHandler.getAllItems(a);
                itemsAdapter.clear();
                modelList.sort(Comparator.comparingInt(ItemModel::getDate));
                modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
                modelList.sort(Comparator.comparingInt(ItemModel::getYear));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
                sortButton.setText("Sort By: Name");
            }
        });

       /* helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(view -> {
            HelpDialogHandler hd = new HelpDialogHandler();
            hd.show(getSupportFragmentManager(), "Help");
        });*/


        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, WakeUpReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Log.d("Alarm Set", "interval 15 min");
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, alarmIntent);
    }

    void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result ->
    {
        if (result.getContents() != null) {
          /*  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.toString());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();*/
        }
    });


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.

        switch (item.getItemId()) {

            case R.id.menu_refresh:
                refresh();
                return true;

            case R.id.menu_settings:
                openSettingsDialog();
                ad.notifyDataSetChanged();
                return true;
            case R.id.menu_help:
                help();
                return true;

            case R.id.menu_logOut:
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void help() {
        HelpDialogHandler hd = new HelpDialogHandler();
        hd.show(getSupportFragmentManager(), "Help");
    }

    public void refresh() {
        modelList.clear();
        itemsAdapter.clear();
        itemsAdapter.notifyDataSetChanged();
        String a = categorySpinner.getSelectedItem().toString();
        if (a.equals("All Items")) {
            modelList = dbHandler.getAllItems();
        } else modelList = dbHandler.getAllItems(a);
        modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
        modelList.sort(Comparator.comparingInt(ItemModel::getYear));
        populate(modelList);
        itemsAdapter.notifyDataSetChanged();
    }

    public void logout() {
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("flag", false);
        editor.apply();
        finish();
        Intent in = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(in);
    }


    private void populate(List<ItemModel> list) {
        for (ItemModel a : list) {
            addItem(a, dbh.getCurrentFormat());
        }
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            AlertDialog.Builder altdial = new AlertDialog.Builder(MainActivity.this);
            altdial.setMessage("Delete this item?").setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Context context = getApplicationContext();
                        Toast.makeText(context, "Item Removed", Toast.LENGTH_SHORT).show();
                        items.remove(i);
                        System.out.println(modelList.get(i).getItem());
                        dbHandler.deleteRow(modelList.get(i));
                        itemsAdapter.notifyDataSetChanged();

                        String fileNameText = "ed_" + modelList.get(i).getItem() + "." + modelList.get(i).getDate() + "." + modelList.get(i).getMonth() + "." + modelList.get(i).getYear() + "." + modelList.get(i).getCategory() + ".jpg";
                        Uri uri = Uri.parse("content://com.expirydatereminder.provider/cache/images/" + fileNameText);
                        ContentResolver contentResolver = getContentResolver();
                        contentResolver.delete(uri, null, null);
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());

            AlertDialog alert = altdial.create();
            alert.setTitle(items.get(i));
            alert.show();
            return true;
        });
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent myIntent = new Intent(MainActivity.this, ItemDetailsOnClick.class);
            myIntent.putExtra("item name", modelList.get(i).getItem());
            myIntent.putExtra("month", modelList.get(i).getMonth());
            myIntent.putExtra("year", modelList.get(i).getYear());
            myIntent.putExtra("date", modelList.get(i).getDate());
            myIntent.putExtra("category", modelList.get(i).getCategory());
            myIntent.putExtra("days", modelList.get(i).getNotifyDays());

            MainActivity.this.startActivity(myIntent);

        });
    }

    private int checkIfItemExists(String item, int month, int year, int date, String category, String days) {
        List<ItemModel> list_of_items = dbHandler.getAllItems();
        for (ItemModel obj : list_of_items) {
            if (obj.getItem().equals(item)) {
                if (obj.getMonth() == month) {
                    if (obj.getYear() == year) {
                        System.out.println(obj.getCategory());
                        if (obj.getCategory().equals(category)) {
                            return 3;
                        } else return 4;
                    }
                }
                return 2;
            }
        }
        return 1;
    }

    private void addItem(ItemModel obj, int dateFormat) {
        String itemName = obj.getItem();
        int month = obj.getMonth();
        int year = obj.getYear();
        int date = obj.getDate();

        String m = month + "", d = date + "";
        if (month < 10) {
            m = "0" + month;
        }
        if (date < 10) {
            d = "0" + date;
        }

        String totalItem;
        if (dateFormat == 1) {
            totalItem = m + "/" + d + "/" + year + " : " + itemName;
        } else totalItem = d + "/" + m + "/" + year + " : " + itemName;
        if (!itemName.isEmpty()) {
            itemsAdapter.add(totalItem);
        } else {
            Toast.makeText(getApplicationContext(), "Cannot be empty string, enter text", Toast.LENGTH_SHORT).show();
        }
    }

    private void addItem(String itemName, int date, int month, int year, String category, String days) {
        int checker = checkIfItemExists(itemName, month, year, date, category, days);

        if (checker == 3) {
            Toast.makeText(getApplicationContext(), "This item already exists!", Toast.LENGTH_SHORT).show();
            return;
        } else if (checker == 2) {
            Toast.makeText(getApplicationContext(), "Item with same name exists! ", Toast.LENGTH_SHORT).show();
            return;
        } else if (checker == 4) {
            Toast.makeText(getApplicationContext(), "Item with same name exists in a different category!", Toast.LENGTH_SHORT).show();
            return;
        }

        String m = month + "", d = date + "";
        if (month < 10) {
            m = "0" + month;
        }
        if (date < 10) {
            d = "0" + date;
        }

        String text;

        DateFormatDatabase dfd = new DateFormatDatabase(getApplicationContext());
        if (dfd.getCurrentFormat() == 1) {
            text = m + "/" + d + "/" + year + " : " + itemName;
        } else text = d + "/" + m + "/" + year + " : " + itemName;

        itemsAdapter.add(text);
        dbHandler.addNewItem(new ItemModel(itemName, month, year, date, category, days));
        modelList.add(new ItemModel(itemName, month, year, date, category, days));
    }

    private void createNotificationChannel() {
        CharSequence name = "EDR Channel";
        String description = "Channel for Expiry Date Reminder notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("edr_channel_1", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void openDialog() {
        DialogHandler dialogHandler = new DialogHandler();
        dialogHandler.show(getSupportFragmentManager(), "Add item");
    }

    private void openSettingsDialog() {
        SettingsDialogHandler settingsDialogHandler = new SettingsDialogHandler();
        settingsDialogHandler.show(getSupportFragmentManager(), "Settings");
    }

    @Override
    public void addItemAsNeeded(String item_name, int date, int month, int year, String category_name, String item_days) {
        addItem(item_name, date, month, year, category_name, item_days);
        System.out.println(item_days);
        itemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i == 0) {
            if (sortButton.getText().toString().equals("Sort By: Date")) {
                modelList = dbHandler.getAllItems();
                itemsAdapter.clear();
                modelList.sort(Comparator.comparingInt(ItemModel::getDate));
                modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
                modelList.sort(Comparator.comparingInt(ItemModel::getYear));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
            } else {
                modelList = dbHandler.getAllItems();
                itemsAdapter.clear();
                modelList.sort(Comparator.comparing(ItemModel::getItem));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
            }
        } else {
            if (sortButton.getText().toString().equals("Sort By: Date")) {
                modelList = dbHandler.getAllItems(categories.get(i));
                itemsAdapter.clear();
                modelList.sort(Comparator.comparingInt(ItemModel::getDate));
                modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
                modelList.sort(Comparator.comparingInt(ItemModel::getYear));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
            } else {
                modelList = dbHandler.getAllItems(categories.get(i));
                itemsAdapter.clear();
                modelList.sort(Comparator.comparing(ItemModel::getItem));
                populate(modelList);
                itemsAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refresh(int a) {
        ad.clear();
        categories.clear();
        ad.notifyDataSetChanged();
        categories = settingsDatabaseHandler.getCategories();
        ad.addAll(categories);
        ad.notifyDataSetChanged();

        modelList.clear();
        itemsAdapter.clear();
        itemsAdapter.notifyDataSetChanged();
        modelList = dbHandler.getAllItems();
        modelList.sort(Comparator.comparingInt(ItemModel::getDate));
        modelList.sort(Comparator.comparingInt(ItemModel::getMonth));
        modelList.sort(Comparator.comparingInt(ItemModel::getYear));
        populate(modelList);
        itemsAdapter.notifyDataSetChanged();
        sortButton.setText("Sort By: Date");
    }

    @Override
    public void deleteImages(String category) {
        List<ItemModel> items_of_category = dbHandler.getAllItems(category);
        for (ItemModel a : items_of_category) {
            String fileNameText = "ed_" + a.getItem() + "." + a.getDate() + "." + a.getMonth() + "." + a.getYear() + "." + a.getCategory() + ".jpg";
            Uri uri = Uri.parse("content://com.expirydatereminder.provider/cache/images/" + fileNameText);
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(uri, null, null);
        }
    }

    @Override
    public void deleteImages() {
        List<ItemModel> items_of_category = dbHandler.getAllItems();
        for (ItemModel a : items_of_category) {
            String fileNameText = "ed_" + a.getItem() + "." + a.getDate() + "." + a.getMonth() + "." + a.getYear() + "." + a.getCategory() + ".jpg";
            Uri uri = Uri.parse("content://com.expirydatereminder.provider/cache/images/" + fileNameText);
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(uri, null, null);
        }
    }
}