package hasyimcom.androidphp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ahmad Hasyim BSA on 3/7/2015.
 */
public class AllProductsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productList;

    // url to get all products list
    // example url "http://api.androidhive.info/android_connect/get_all_products.php"
    private static String url_all_products =
            "http://10.0.3.2/pelatihan/phpAndroid/getAllProducts.php";

    //JSON Node Names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_DES = "description";

    // products JSONArray
    JSONArray products = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_products);

        // hashmap for ListView
        productList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllProducts().execute();
        //Get listview
        ListView lv = getListView();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), EditProductActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
                // starting new activity and expecting some response
                startActivityForResult(in, 100);


            }
        });
    }

    // Response from Edit Product Activity


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
    /**
     * Background Async Task to Load all product by making HTTP request
     * */

    //  class LoadAllProducts extends AsyncTask<String, String, String> {
     class LoadAllProducts extends AsyncTask<String, String, String> {
        // Before starting background thread show Progress Dialog


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Loading products. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting all products from url
         * */
        @Override
        protected String doInBackground(String... args) {
            //Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
            // check your log cat for JSON response
            Log.d("All Products: ", json.toString());

            try {
                //Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_PRODUCTS);

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);


                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String price = c.getString(TAG_PRICE);
                        String description = c.getString(TAG_DES);

                        // Creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        //adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_PRICE,price);
                        map.put(TAG_DES,description);

                        // adding HashList to Array List
                        productList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add new product Activity
                    Intent i = new Intent(getApplicationContext(), NewProductActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * */
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Updating parsed JSON data into ListView
                    ListAdapter adapter = new SimpleAdapter(
                            AllProductsActivity.this, productList,
                            R.layout.list_item, new String[] { TAG_PID,
                            TAG_NAME,TAG_PRICE,TAG_DES}, new int[] {R.id.pid, R.id.name,R.id.price,R.id.des});
                    // updating listview
                    setListAdapter(adapter);
                }
            });
        }
    }
}
