package com.seaory.seaorydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.support.v7.app.AppCompatActivity;

import android.widget.Toast;

import com.seaory.sdk.SeaorySDK;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String GET_API_URL = "https://example.com/api/images";
    private static final String POST_API_URL = "https://example.com/api/print";

    private RecyclerView recyclerView;
    private ImageView qrImageView;
    private Button printButton;
    private Button connectionButton;
    private CardViewAdapter imageAdapter;
    private List<CardViewItem> cards;

    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;

    private SeaorySDK printerFn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printerFn = new SeaorySDK(this);
        printerFn.RequestUsbPermission();

        recyclerView = findViewById(R.id.recyclerView);
        qrImageView = findViewById(R.id.qrImageView);
        printButton = findViewById(R.id.printButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        cards = new ArrayList<>();
        imageAdapter = new CardViewAdapter(cards, this);
        imageAdapter.setOnItemClickListener(new CardViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                for (int i = 0; i < cards.size(); i++) {
                    CardViewItem card = cards.get(i);
                    if (i == position) {
                        card.setSelected(!card.isSelected());
                    } else {
                        card.setSelected(false);
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(imageAdapter);

        qrImageView.setImageBitmap(loadBitmapFromAssets("qr-img.png", this));

        connectionButton = findViewById(R.id.connectionButton);

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CardViewItem obj : cards) {
                    if (obj.isSelected()) {
                        btn_simplePrint_onClick(obj);
                        return;
                    }
                }
            }
        });

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isPrinterConnected()) {
                            btn_connectPrinter_onClick();
                        } else {
//                            btn_disconnectPrinter_onClick();
                        }
                    }
                }).start();
            }
        });

        // Set up pull-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshImages();
            }
        });

        // Load images from API
        loadImages();
    }

    private void loadImages() {
        if (!isLoading) {
            isLoading = true;
            new GetImagesTask().execute();
        }
    }

    private void refreshImages() {
        cards.clear();
        imageAdapter.notifyDataSetChanged();
        loadImages();
    }

    private class GetImagesTask extends AsyncTask<Void, Void, List<CardViewItem>> {
        @Override
        protected List<CardViewItem> doInBackground(Void... voids) {
            try {
                // Make the API call to retrieve the data
                String apiUrl = "https://api.sosleek.io/rpc/events/getIrlCards";
                String requestBody = "{\"code\":\"df077872-1308-11ee-be56-0242ac120002\",\"eventId\":3}";

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse the API response and extract the required data
                    List<CardViewItem> cardViewItems = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String name = jsonObject.getString("name");
                        String orderStatus = jsonObject.getString("order_status");
                        String shippingStatus = jsonObject.getString("shipping_status");
                        String printerUrl = jsonObject.getString("printerUrl");

                        // Load the image from the printerUrl using Picasso library
                        Bitmap bitmap = Picasso.get().load(printerUrl).get();

                        cardViewItems.add(new CardViewItem(id, name, orderStatus, shippingStatus, printerUrl, bitmap, false));
                    }

                    return cardViewItems;
                } else {
                    Log.e("GetImagesTask", "API call failed with response code: " + responseCode);
                }
            } catch (IOException | JSONException | RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<CardViewItem> cardViewItems) {
            if (cardViewItems != null) {
                // Update the list of images and notify the adapter
                cards.addAll(cardViewItems);
                imageAdapter.notifyDataSetChanged();
            }
            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    Bitmap loadBitmapFromAssets(String name, Context context) {

        if (name == null)
            return null;

        InputStream is = null;
        try {
            is = context.getAssets().open(name);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return BitmapFactory.decodeStream(is, null, null);
    }

    public void btn_connectPrinter_onClick() {
        try {
            printerFn.OpenDevice();
            connectionButton.setText("🟢");
        } catch (Exception e) {
            e.printStackTrace();
            showConnectionError(e);
        }
    }

    /**
     *
     */
    public void btn_disconnectPrinter_onClick() {
        try {
            printerFn.CloseDevice();
            Toast.makeText(MainActivity.this, "Printer Disconnected!", Toast.LENGTH_SHORT).show();
            connectionButton.setText("🛑");
        } catch (Exception e) {
            e.printStackTrace();
            showConnectionError(e);
        }

    }

    private void showConnectionError(Exception e) {
        connectionButton.setText("🛑");
        String errStr = String.format("Connection Err (%s)!", e.toString());
        Toast.makeText(MainActivity.this, errStr, Toast.LENGTH_SHORT).show();
    }

    public void btn_simplePrint_onClick(CardViewItem obj) {
        if (!isPrinterConnected()) {
            btn_connectPrinter_onClick();
        }
        Bitmap bmp = obj.getBitmapImage();
        int status = 0;
        String resultStr = "";

        int nHeatFrontK = 0, nRespFrontK = 0;
        try {

            int nX = 0, nY = 0, nWidth = 0, nHeight = 0;
            boolean b600x600dpi = false;
            char[] szModel = new char[64];
            status = printerFn.GetPrinterInfo(SeaorySDK.INFO_MODEL_NAME, szModel);

            String modelName = new String(szModel).trim();
            if (modelName.compareTo("R600") == 0 || modelName.compareTo("R600M") == 0 || modelName.compareTo("DCE905") == 0 || modelName.compareTo("DR600") == 0 || modelName.compareTo("D600") == 0)
                b600x600dpi = true;

            SeaorySDK.SEAORY_DOC_PROP docProp = new SeaorySDK.SEAORY_DOC_PROP();
            docProp.byResolution = 0;
            docProp.byPrintSide = 1;
            printerFn.SetHeatingEnergy(
                    (short) 0,    //Front YMC
                    (short) nHeatFrontK,    //Front K
                    (short) 0,    //Front O
                    (short) 0,    //Front Resin K
                    (short) 0,    //Back  YMC
                    (short) 0,    //Back  K
                    (short) 0,    //Back  O
                    (short) 0,     //Back  Resin K
                    (short) nRespFrontK,    //Front K
                    (short) 0    //Back  K
            );

            status = printerFn.SOY_PR_StartPrinting(docProp);
            resultStr = "SOY_PR_StartPrinting() return " + status + ".\n";
//                showResultString(100, resultStr);

            status = printerFn.SOY_PR_StartPage();
            resultStr = "SOY_PR_StartPage() return " + status + ".\n";
//                showResultString(100, resultStr);

            Bitmap aBmp = bmp;// loadBitmapFromAssets("printer-1756.png", this);
            nX = 60;
            nY = 120;
            nWidth = 300;
            nHeight = 400;
            if (b600x600dpi) {
                nX *= 2;
                nY *= 2;
                nWidth *= 2;
                nHeight *= 2;
            }
            status = printerFn.SOY_PR_PrintImage(0, 0, 1012, 648, aBmp);
            resultStr = "SOY_PR_PrintImage() return " + status + ".\n";


            status = printerFn.SOY_PR_EndPrinting(false);
            resultStr = "SOY_PR_EndPrinting() return " + status + ".\n";

            resultStr = "";
            if (status == 0)//send print data to printer ok
            {
                //wait printer to print card completely
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                int i = 0;
                String TAG = "SeaoryDemo";
                while (true) {
                    status = printerFn.CheckStatus();

                    Log.i("TAG", String.format("CheckStatus(%d) return 0x%08X,", i, status));

                    if (status != 170)
                        break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    i++;
                }

                if (status != 0) {
                    resultStr = "Printer status = " + status;
                    resultStr += " (" + printerFn.errorMap.get((int) status) + ")";
                    resultStr += "\n";
                    Log.i(TAG, resultStr);
                }
            } else {
                resultStr = "";
            }

            postOrderUpdate(getSelectedOrderId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSelectedOrderId() {
        for (CardViewItem obj : cards) {
            if (obj.isSelected()) {
                return obj.getId();
            }
        }
        return -1; // Return an appropriate default value or handle the case when no card is selected
    }

    public void postOrderUpdate(int orderId) {
        String apiUrl = "https://api.sosleek.io/rpc/events/updateIrlCard?orderId=" + orderId;
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("orderId", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            // Create the URL object
            URL url = new URL(apiUrl);

            // Create the HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write the request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Handle the response
                System.out.println("API call successful. Response: " + response.toString());
            } else {
                // Handle the error
                System.out.println("API call failed. Response Code: " + responseCode);
            }

            // Disconnect the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap removeBackgroundColor(Bitmap bitmap) {
        int backgroundColor = Color.TRANSPARENT; // Replace with the actual background color you want to remove
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(backgroundColor, 0);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return resultBitmap;
    }
    private boolean isPrinterConnected() {
        return connectionButton.getText().equals("🟢");
    }
}
