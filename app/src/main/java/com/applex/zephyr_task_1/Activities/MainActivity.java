package com.applex.zephyr_task_1.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.applex.zephyr_task_1.Adapters.RecyclerAdapter;
import com.applex.zephyr_task_1.Models.FormModel;
import com.applex.zephyr_task_1.R;
import com.applex.zephyr_task_1.Utilities.JSONHelper;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerAdapter adapter;
    private ImageView no_data, error;
    private RecyclerView recyclerView;
    private ProgressBar progress_more;
    private CoordinatorLayout main_layout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ArrayList<FormModel> formModelArrayList;
    private long mLastClickTime = 0;
    private int checkGetMore = -1;
    private File fileJson;
    private String strFileJson;
    private Dialog dialog;
    private int fetch_more = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        formModelArrayList = new ArrayList<>();

        error = findViewById(R.id.error);
        no_data = findViewById(R.id.no_data);
        main_layout = findViewById(R.id.main_layout);
        recyclerView = findViewById(R.id.recycler_list);
        progress_more = findViewById(R.id.progress_more);
        shimmerFrameLayout = findViewById(R.id.shimmerLayout);

        settingImage(R.drawable.no_data);
        settingImage(R.drawable.error);

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        fileJson = new File(Environment.getExternalStorageDirectory() + "/Zephyr","Forms.json");
        try {
            strFileJson = JSONHelper.getStringFromFile(fileJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(true);

        new Handler().postDelayed(() -> {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            if(strFileJson == null) {
                main_layout.setBackgroundColor(getResources().getColor(R.color.white));
                recyclerView.setVisibility(View.GONE);
                no_data.setVisibility(View.GONE);
                error.setVisibility(View.VISIBLE);
            }
            else {
                formModelArrayList.clear();
                buildRecyclerView(fetch_more);
            }
        }, 1000);

        ExtendedFloatingActionButton create_form = findViewById(R.id.create_form);
        create_form.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.dialog_create_form);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());

            dialog.findViewById(R.id.save).setOnClickListener(v -> {
                EditText name_edit_text = dialog.findViewById(R.id.name);
                EditText age_edit_text = dialog.findViewById(R.id.age);
                EditText address_line_edit_text = dialog.findViewById(R.id.address);
                EditText city_edit_text = dialog.findViewById(R.id.city);
                EditText state_edit_text = dialog.findViewById(R.id.state);
                EditText pin_code_edit_text = dialog.findViewById(R.id.pin_code);

                final String name = name_edit_text.getText().toString().trim();
                final String age = age_edit_text.getText().toString().trim();
                final String address_line = address_line_edit_text.getText().toString().trim();
                final String city = city_edit_text.getText().toString().trim();
                final String state = state_edit_text.getText().toString().trim();
                final String pin_code = pin_code_edit_text.getText().toString().trim();

                if (name.isEmpty()) {
                    name_edit_text.setError("Name missing");
                    name_edit_text.requestFocus();
                }
                else if (age.isEmpty() || Integer.parseInt(age) < 18 || Integer.parseInt(age) > 65) {
                    if (age.isEmpty()) {
                        age_edit_text.setError("Age missing");
                    }
                    else {
                        age_edit_text.setError("Age must be between 18 and 65");
                    }
                    age_edit_text.requestFocus();
                }
                else if (address_line.isEmpty()) {
                    address_line_edit_text.setError("Address line missing");
                    address_line_edit_text.requestFocus();
                }
                else if (city.isEmpty()) {
                    city_edit_text.setError("City missing");
                    city_edit_text.requestFocus();
                }
                else if (state.isEmpty()) {
                    state_edit_text.setError("State missing");
                    state_edit_text.requestFocus();
                }
                else if (pin_code.length() != 6) {
                    if (pin_code.isEmpty()) {
                        pin_code_edit_text.setError("Pin code missing");
                    }
                    else {
                        pin_code_edit_text.setError("Please enter a valid pin_code");
                    }
                    pin_code_edit_text.requestFocus();
                }
                else {
                    FormModel formModel = new FormModel();
                    formModel.setName(name);
                    formModel.setAge(Integer.parseInt(age));
                    formModel.setAddress(address_line + ", " + city + " - " + pin_code + ", " + state);
                    new Background_Task(formModel).execute();
                }
            });
        });

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(v, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(scrollY > oldScrollY) {
                new Handler().postDelayed(create_form::shrink, 200);
            }
            else if(scrollY < oldScrollY) {
                new Handler().postDelayed(create_form::extend, 200);
            }

            if(v.getChildAt(v.getChildCount() - 1) != null) {
                if((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    if(checkGetMore != -1) {
                        if(progress_more.getVisibility() == View.GONE) {
                            checkGetMore = -1;
                            progress_more.setVisibility(View.VISIBLE);
                            buildRecyclerView(fetch_more);
                        }
                    }
                }
            }
        });
    }

    private void buildRecyclerView(int index) {
        try {
            JSONObject jsonObject = new JSONObject(strFileJson);
            JSONArray jsonArray = jsonObject.getJSONArray("Forms");
            if(jsonArray.length() > 0) {
                int size = Math.min(jsonArray.length(), index + 10);
                ArrayList<FormModel> arrayList = new ArrayList<>();

                for(int i = index; i < size; i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    FormModel formModel = new FormModel();
                    formModel.setName(object.getString("Name"));
                    formModel.setAge(object.getInt("Age"));
                    formModel.setAddress(object.getString("Address"));
                    arrayList.add(formModel);
                }

                main_layout.setBackgroundColor(getResources().getColor(R.color.grey));
                error.setVisibility(View.GONE);
                no_data.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                progress_more.setVisibility(View.GONE);

                if(arrayList.size() > 0) {
                    formModelArrayList.addAll(arrayList);

                    if(index == 0) {
                        adapter = new RecyclerAdapter(formModelArrayList);
                        recyclerView.setAdapter(adapter);
                    }
                    else {
                        adapter.notifyItemRangeInserted(index + 10, size);
                    }
                }

                if(arrayList.size() < 10) {
                    checkGetMore = -1;
                } else {
                    checkGetMore = 0;
                    fetch_more = fetch_more + 10;
                }
            }
            else {
                main_layout.setBackgroundColor(getResources().getColor(R.color.white));
                recyclerView.setVisibility(View.GONE);
                error.setVisibility(View.GONE);
                no_data.setVisibility(View.VISIBLE);
            }
        }
        catch (JSONException e) {
            main_layout.setBackgroundColor(getResources().getColor(R.color.white));
            recyclerView.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
            error.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class Background_Task extends AsyncTask<Void, Void, Void> {

        private final FormModel formModel;

        Background_Task(FormModel formModel) {
            this.formModel = formModel;
        }

        @Override
        protected void onPreExecute() {
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject previousJsonObj, currentJsonObject;
            JSONArray array;
            try {
                previousJsonObj = new JSONObject(strFileJson);
                array = previousJsonObj.getJSONArray("Forms");

                JSONObject jsonObj= new JSONObject();
                jsonObj.put("Name", formModel.getName());
                jsonObj.put("Age", formModel.getAge());
                jsonObj.put("Address", formModel.getAddress());

                array.put(jsonObj);
                currentJsonObject = new JSONObject();
                currentJsonObject.put("Forms", array);
                JSONHelper.writeJsonFile(fileJson, currentJsonObject.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                strFileJson = JSONHelper.getStringFromFile(fileJson.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            formModelArrayList.clear();
            buildRecyclerView(0);
        }
    }

    private void settingImage(int id) {
        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), id, options);

        int width = options.outWidth;
        if (width > displayWidth) {
            options.inSampleSize = Math.round((float) width / (float) displayWidth);
        }
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), id, options);

        if(id == R.drawable.no_data) {
            no_data.setImageBitmap(scaledBitmap);
        }
        else {
            error.setImageBitmap(scaledBitmap);
        }
    }
}