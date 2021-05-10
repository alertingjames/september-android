package com.septmb.septmb.septmb.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.adapters.ProductListAdapter;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.Constants;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.models.ProductInfoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.septmb.septmb.septmb.R.id.productList;

public class ReviewMyProductsActivity extends Activity implements SwipyRefreshLayout.OnRefreshListener {

    LinearLayout search;
    EditText ui_edtsearch;
    ProductListAdapter productListAdapter=new ProductListAdapter(this);
    ArrayList<ProductInfoEntity> productInfoEntities=new ArrayList<>();
    GridView list;
    ProgressDialog progressDialog;
    String keyword="";
    boolean searchFlag=false;
    SwipyRefreshLayout ui_RefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_my_products);

        list=(GridView)findViewById(productList);

        ui_RefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        ui_RefreshLayout.setOnRefreshListener(this);
        ui_RefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);

        search=(LinearLayout)findViewById(R.id.search);
        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().toLowerCase(Locale.getDefault());
                if (text.length() != 0) {
                    productListAdapter.filter(text);
                    //   adapter.notifyDataSetChanged();
                }else  {
                    productListAdapter.setDatas(productInfoEntities);
                    list.setAdapter(productListAdapter);
                }

            }
        });

        ImageView cancel=(ImageView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_edtsearch.setText("");
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        searchFlag=getIntent().getBooleanExtra("awesome",false);
        if(searchFlag){
            getSimilarProducts(getIntent().getStringExtra("keyword"));
        }else
            getProducts();
        Commons.searchFlag=searchFlag;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        return true;
    }

    public void refreshProductList(MenuItem item){
        if(searchFlag){
            getSimilarProducts(getIntent().getStringExtra("keyword"));
        }else
            getProducts();
    }

    public void getProducts() {

        productInfoEntities.clear();

        String url = ReqConst.SERVER_URL + "getMyProductInfo";

        progressDialog.show();


        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseBroadmoorResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("email", Commons.thisUser.getEmail());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseBroadmoorResponse(String json) {

        progressDialog.dismiss();
        try {

            JSONObject response = new JSONObject(json);

            String success = response.getString("result");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {

                JSONArray productInfo = response.getJSONArray("product_info");

                for (int i = 0; i < productInfo.length(); i++) {

                    JSONObject jsonBroadmoor = (JSONObject) productInfo.get(i);

                    ProductInfoEntity productInfoEntity = new ProductInfoEntity();

                    productInfoEntity.setIdx(jsonBroadmoor.getString("id"));
                    productInfoEntity.setUser_id(jsonBroadmoor.getString("user_id"));
                    productInfoEntity.setTitle(jsonBroadmoor.getString("title"));
                    productInfoEntity.setBrand(jsonBroadmoor.getString("brand"));
                    productInfoEntity.setImage_url(jsonBroadmoor.getString("image_url"));
                    productInfoEntity.setGender(jsonBroadmoor.getString("gender"));
                    productInfoEntity.setPrice(jsonBroadmoor.getString("price"));
                    productInfoEntity.setCategory(jsonBroadmoor.getString("category"));
                    productInfoEntity.setSeller(jsonBroadmoor.getString("sale_url"));
                    productInfoEntity.setKeyword(jsonBroadmoor.getString("keyword"));
                    productInfoEntity.setDescription(jsonBroadmoor.getString("description"));

                    productInfoEntities.add(0,productInfoEntity);
                }
                if(productInfoEntities.isEmpty()) Toast.makeText(getApplicationContext(),"No product...", Toast.LENGTH_SHORT).show();
                else {
                    list.setVisibility(View.VISIBLE);
                    productListAdapter.setDatas(productInfoEntities);
                    productListAdapter.notifyDataSetChanged();
                    list.setAdapter(productListAdapter);
                }
            }
            else {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void getSimilarProducts(final String key) {

        productInfoEntities.clear();

        String url = ReqConst.SERVER_URL + "getSimilarProductInfo";

        progressDialog.show();


        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseSimilarProductInfoResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("keyword", key);

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseSimilarProductInfoResponse(String json) {

        progressDialog.dismiss();
        try {

            JSONObject response = new JSONObject(json);

            String success = response.getString("result");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {

                JSONArray productInfo = response.getJSONArray("product_info");

                for (int i = 0; i < productInfo.length(); i++) {

                    JSONObject jsonBroadmoor = (JSONObject) productInfo.get(i);

                    ProductInfoEntity productInfoEntity = new ProductInfoEntity();

                    productInfoEntity.setIdx(jsonBroadmoor.getString("id"));
                    productInfoEntity.setUser_id(jsonBroadmoor.getString("user_id"));
                    productInfoEntity.setTitle(jsonBroadmoor.getString("title"));
                    productInfoEntity.setBrand(jsonBroadmoor.getString("brand"));
                    productInfoEntity.setImage_url(jsonBroadmoor.getString("image_url"));
                    productInfoEntity.setGender(jsonBroadmoor.getString("gender"));
                    productInfoEntity.setPrice(jsonBroadmoor.getString("price"));
                    productInfoEntity.setCategory(jsonBroadmoor.getString("category"));
                    productInfoEntity.setSeller(jsonBroadmoor.getString("sale_url"));
                    productInfoEntity.setKeyword(jsonBroadmoor.getString("keyword"));
                    productInfoEntity.setDescription(jsonBroadmoor.getString("description"));

                    productInfoEntities.add(0,productInfoEntity);
                }
                if(productInfoEntities.isEmpty()) Toast.makeText(getApplicationContext(),"No product...", Toast.LENGTH_SHORT).show();
                else {
                    list.setVisibility(View.VISIBLE);
                    productListAdapter.setDatas(productInfoEntities);
                    productListAdapter.notifyDataSetChanged();
                    list.setAdapter(productListAdapter);
                }
            }
            else {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}






































