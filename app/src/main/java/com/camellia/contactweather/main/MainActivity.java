package com.camellia.contactweather.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camellia.contactweather.R;
import com.camellia.contactweather.contacts.AllContactsActivity;
import com.camellia.contactweather.contacts.DataBaseHelper;
import com.camellia.contactweather.webservice.ApiManager;
import com.camellia.contactweather.webservice.model.DataModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnOptionMenuItemClickListener {

  private FloatingActionButton floatingButton;
  private RecyclerView recyclerView;
  private RecyclerView.LayoutManager layoutManager;
  private RecyclerView.Adapter myAdapter;
  private List<ContactData> contactList = new ArrayList<>();
  private TextView emptyText;
  private TextView emptyTextSub;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setSupportActionBar((Toolbar) findViewById(R.id.myToolBar));
    setTitle("Contact Weather");

    emptyText = findViewById(R.id.emptyText);
    emptyTextSub = findViewById(R.id.emptyTextSub);
    recyclerView = findViewById(R.id.myRecyclerView);
    floatingButton = findViewById(R.id.floatingButton);

    layoutManager = new LinearLayoutManager(getApplicationContext());
    recyclerView.setLayoutManager(layoutManager);
    myAdapter = new ContactAdapter(contactList, this, this);
    recyclerView.setAdapter(myAdapter);

    clickOnFloatingButton();

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0) {
          // Scroll Down
          if (floatingButton.isShown()) {
            floatingButton.hide();
          }
        } else if (dy < 0) {
          // Scroll Up
          if (!floatingButton.isShown()) {
            floatingButton.show();
          }
        }
      }
    });
  }

  public void showEmptyState() {

    if (myAdapter.getItemCount() == 0) {
      emptyText.setVisibility(View.VISIBLE);
      emptyTextSub.setVisibility(View.VISIBLE);
    } else {
      emptyText.setVisibility(View.GONE);
      emptyTextSub.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    initContactData();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //Toast.makeText(this, "sync clicked", Toast.LENGTH_SHORT).show();
    if (item.getItemId() == R.id.sync) {
      WeatherCache.getInstance().clearCache();
      refreshData();
    }
    return super.onOptionsItemSelected(item);
  }

  private void refreshData() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        for (ContactData contactData : contactList) {
          if (contactData.getCity() == null ||
            (contactData.getDataModel() == null && WeatherCache.getInstance().getCacheData(contactData.getCity()) == null)) {

            double latitude = contactData.getLatitude();
            double longitude = contactData.getLongitude();

            Response<DataModel> response = null;
            try {
              response = ApiManager.getInstance().getCurrentWeather(latitude, longitude).execute();
            } catch (IOException e) {
              e.printStackTrace();
            }

            if (response != null) {
              DataModel dataModel = response.body();
              if (dataModel != null) {
                contactData.setDataModel(dataModel);
                contactData.setCity(dataModel.getCityName());

                DataBaseHelper.getInstance(getApplicationContext()).updateContactCity(contactData.getPhoneNumber(), dataModel.getCityName());

                WeatherCache.getInstance().putCacheData(dataModel.getCityName(), dataModel);

                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    myAdapter.notifyDataSetChanged();
                    showEmptyState();
                  }
                });
              }
            }
          } else {
            contactData.setDataModel(WeatherCache.getInstance().getCacheData(contactData.getCity()));
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                myAdapter.notifyDataSetChanged();
                showEmptyState();
              }
            });
          }
        }
      }
    }).start();
  }

  public void initContactData() {
    ArrayList<ContactData> list = DataBaseHelper.getInstance(this).readContacts();
    contactList.clear();
    contactList.addAll(list);
    myAdapter.notifyDataSetChanged();
    showEmptyState();

    refreshData();
  }

  public void clickOnFloatingButton() {
    floatingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, AllContactsActivity.class);
        startActivity(intent);
      }
    });
  }

  @Override
  public void onOptionMenuItemClicked(View view, final int position) {
    //Display option menu

    final Context context = this;
    final ContactData contactData = contactList.get(position);
    PopupMenu popupMenu = new PopupMenu(context, view);
    popupMenu.inflate(R.menu.option_menu);
    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

          case R.id.menu_item_delete:

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Are you sure to delete \"" + contactData.getDisplayName() + "\"?");

            builder.setNegativeButton("Cancel", null);

            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                DataBaseHelper.getInstance(getApplicationContext()).deleteContact(contactData.getPhoneNumber());
                contactList.remove(position);
                myAdapter.notifyDataSetChanged();
                showEmptyState();
                floatingButton.show();
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
              }
            });

            builder.create().show();
            break;

          case R.id.menu_item_changeLocation:
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("displayName", contactData.getDisplayName());
            intent.putExtra("phone", contactData.getPhoneNumber());
            intent.putExtra("longitude", contactData.getLongitude());
            intent.putExtra("latitude", contactData.getLatitude());
            intent.putExtra("isUpdate", true);

            context.startActivity(intent);

            Toast.makeText(context, "change location", Toast.LENGTH_SHORT).show();
            break;

          default:
            break;

        }
        return false;
      }
    });
    popupMenu.show();
  }

}


