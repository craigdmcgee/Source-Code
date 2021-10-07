package com.technobrain.rms;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.technobrain.rms.models.OfflineReceiptModel;
import com.technobrain.rms.models.OfflineSavedTransactionModel;
import com.technobrain.rms.models.PrintingModel;
import com.technobrain.rms.models.ProductModel;
import com.technobrain.rms.models.SaveTransactionsModels;
import com.technobrain.rms.pojo.FetchCollectionRevenueModel;
import com.technobrain.rms.pojo.FetchTransactionsModel;
import com.technobrain.rms.pojo.FetchTransactionsResponseModel;
import com.technobrain.rms.pojo.LoginRequestModel;
import com.technobrain.rms.pojo.LoginResponseModel;
import com.technobrain.rms.pojo.SubmitTransactionResponse;
import com.technobrain.rms.pojo.sent.TransactionDetails;
import com.technobrain.rms.pojo.sent.TransactionSubmitRequestModel;
import com.technobrain.rms.utils.Global;
import com.technobrain.rms.utils.PreferencesEko;
import com.technobrain.rms.utils.TypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

import static com.technobrain.rms.utils.Global.LOGINSTATUS;

public class Dashboard extends AppCompatActivity
{

    LinearLayout idCollection;
    LinearLayout download;
    LinearLayout upload;
    LinearLayout settings;
    LinearLayout linlay_transactions;
    LinearLayout verificationcard;
    TextView unsurrenderd_amount, cashlimit_amount, cashlimit_balance, next_checkin_date, last_sync, last_sync_date;
    private List<OfflineSavedTransactionModel> offlineSavedTransactionModels;
    private List<OfflineReceiptModel> offlineReceiptModels;
    private String receiptNo;
    private String paymentMode;
    private SpotsDialog dialog;

    private String payerName;
    private String payerId;
    private String paymentMethod;


    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        SpannableString s = new SpannableString(" Dashboard");

        setContentView(R.layout.activity_dashboard);

        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("Dashboard"); // set the top title
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F48221")));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        dialog = new SpotsDialog(Dashboard.this);
        unsurrenderd_amount = findViewById(R.id.unsurrenderd_amount);
        cashlimit_amount = findViewById(R.id.cashlimit_amount);
        cashlimit_balance = findViewById(R.id.cashlimit_balance);
        next_checkin_date = findViewById(R.id.next_checkin_date);
        last_sync = findViewById(R.id.last_sync);
        last_sync_date = findViewById(R.id.last_sync_date);

        unsurrenderd_amount.setText("Unsurrendered Amount: "+PreferencesEko.getIntPref(Global.UNSURRENDERED_AMOUNT, getApplication()));
        cashlimit_amount.setText("Cash Limit Amount: "+PreferencesEko.getStringPref(Global.CASHLIMIT_AMOUNT, getApplication()));
        cashlimit_balance.setText("Cash Limit Balance: "+PreferencesEko.getStringPref(Global.CASHLIMITBAL_AMOUNT, getApplication()));
        next_checkin_date.setText("Last Check in Date: "+PreferencesEko.getStringPref(Global.NEXTCHECKIN_DATE, getApplication()));
        last_sync.setText("Last Sync: "+PreferencesEko.getStringPref(Global.LAST_SYNC, getApplication()));
        last_sync_date.setText("Check in By: \n"+PreferencesEko.getStringPref(Global.CHECKINBY, getApplication()));



        idCollection= findViewById(R.id.collectionCard);
        idCollection.setOnClickListener(v ->
        {
            //Toasty.normal(getApplicationContext(), "TESTING TOST", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), RmsProducts.class));
            finish();
        });

        download= findViewById(R.id.download);
        download.setOnClickListener(v ->
        {
            String str_username = PreferencesEko.getStringPref(Global.USERNAME, getApplicationContext());
            String str_password = PreferencesEko.getStringPref(Global.PASSWORD, getApplicationContext());
            String str_deviceIMIEI = PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplicationContext());
            String str_simSerialno = PreferencesEko.getStringPref(Global.SERIALNUMB, getApplicationContext());

            dologin("login", str_username, str_password, str_deviceIMIEI, str_simSerialno);
        });

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(v ->

            {
                new SweetAlertDialog(Dashboard.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Settings")
                        .setConfirmText("Change Password")
                        .setCustomImage(R.drawable.ic_settings)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog)
                            {
                                sDialog.dismissWithAnimation();
                                startActivity(new Intent(getApplicationContext(), ChangePassoword.class));
                                finish();

                            }
                        })
                        .setCancelButton("Logout", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })
                        .show();

        });

        upload= findViewById(R.id.upload);
        upload.setOnClickListener(v ->
        {

            databaseHelper = new DatabaseHelper(Dashboard.this);
            int checkAvailableTransactionsCount = databaseHelper.checkIfItemExistsInTransactions();
            if (checkAvailableTransactionsCount == 0)
            {
                Toasty.normal(getApplicationContext(), "No Pending Transactions", Toast.LENGTH_LONG).show();
            }
            else
            {
                dialog.setCancelable(false);
                dialog.show();

                offlineReceiptModels = new ArrayList<OfflineReceiptModel>();
                offlineReceiptModels = databaseHelper.getAllOfllineSavedReceipts();

                if (offlineReceiptModels.size() > 0) {
                    for (OfflineReceiptModel offlineReceiptModels : offlineReceiptModels) {

                        offlineSavedTransactionModels = new ArrayList<OfflineSavedTransactionModel>();
                        offlineSavedTransactionModels = databaseHelper.getAllTransactionsWithThisReceiptNumber(offlineReceiptModels.getCartId());

                        for(OfflineSavedTransactionModel offlineSavedTransactionModel : offlineSavedTransactionModels)
                        {
                            payerName = offlineSavedTransactionModel.getFirstname()+ " "+offlineSavedTransactionModel.getLastname();
                            payerId = String.valueOf(offlineReceiptModels.getPayer_id());
                            paymentMethod = offlineSavedTransactionModel.getPayment_method();
                        }


                        String imeiDevice = PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplication());
                        String simcardSerial = PreferencesEko.getStringPref(Global.SERIALNUMB, getApplication());
                        String username = PreferencesEko.getStringPref(Global.USERNAME, getApplication());
                        String passwd = PreferencesEko.getStringPref(Global.PASSWORD, getApplication());

                        TransactionSubmitRequestModel transactionSubmitRequestModel = new TransactionSubmitRequestModel();
                        transactionSubmitRequestModel.setSIMCardSerial(simcardSerial);
                        transactionSubmitRequestModel.setUserName(username);
                        transactionSubmitRequestModel.setDeviceIMEI(imeiDevice);
                        transactionSubmitRequestModel.setRequestid(String.valueOf(offlineReceiptModels.getCartId()));
                        transactionSubmitRequestModel.setPassword(passwd);
                        transactionSubmitRequestModel.setPayer(payerName);
                        transactionSubmitRequestModel.setPayerid(payerId);
                        transactionSubmitRequestModel.setPaymode(paymentMethod);
                        transactionSubmitRequestModel.setBillTotal(offlineReceiptModels.getBillTotal());


                        List<TransactionDetails> transactionDetails = new ArrayList<>();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

//                                    offlineSavedTransactionModels.stream().forEach(ls -> transactionDetails.add(new TransactionDetails(ls.getItemId(), ls.getItemName(), ls.getUnitprice(), ls.getQty(), ls.getLineTotal(), paymentMode, payerName, "123456")));
                            offlineSavedTransactionModels.stream().forEach(ls -> transactionDetails.add(new TransactionDetails(ls.getItemId(), ls.getItemName(), ls.getUnitprice(), ls.getQty(), ls.getLineTotal())));

                        }

                        transactionSubmitRequestModel.setTransaction(transactionDetails);

                        AsyncHttpClient client = new AsyncHttpClient();
                        client.setTimeout(45000);
                        client.setConnectTimeout(45000);

                        //String url = getString(R.string.baseurl) + "/transaction";
                        String url = getString(R.string.liveurl)+"/FFATransaction/POSPost";
                        Log.e("Calling URL posttrx2:", "-> "+url);


                        System.out.println("=======REQUEST BODY====== " + new Gson().toJson(transactionSubmitRequestModel));

                        client.setSSLSocketFactory(new SSLSocketFactory(Global.getSslContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
                        try {
                            client.post(getApplicationContext(), url, new StringEntity(new Gson().toJson(transactionSubmitRequestModel)), "application/json", new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    dialog.dismiss();
                                    String response = new String(responseBody);
                                    PreferencesEko.setStringPref(Global.LOGINRESP, response, getApplicationContext());
                                    SubmitTransactionResponse submitTransactionResponse = new Gson().fromJson(response, SubmitTransactionResponse.class);
                                    Log.e("RESPONSE FROM Server:", response);


                                    if (submitTransactionResponse.getCode() == 200) {

//                                                JSONObject jsonObject = null;
//                                                try {
//                                                    jsonObject = new JSONObject(response);
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                JSONArray arr = null;
//                                                try {
//                                                    arr = jsonObject.getJSONArray("data");
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                for (int i = 0; i < arr.length(); i++) {
//                                                    JSONObject json = null;
//                                                    try {
//                                                        json = arr.getJSONObject(i);
//                                                    } catch (JSONException e) {
//                                                        e.printStackTrace();
//                                                    }
//
//                                                    try {
//                                                        receiptNo = json.getString("receiptnumber");
//                                                    } catch (JSONException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                    System.out.println(receiptNo);
//                                                }


//
                                        databaseHelper.deleteReceiptItems(offlineReceiptModels.getCartId());
                                        databaseHelper.deleteSubmittedTransactionItems(offlineReceiptModels.getCartId());
                                        Toasty.success(getApplicationContext(), "Transactions Submitted successfully", Toast.LENGTH_SHORT).show();

                                        dialog.dismiss();


                                    } else {

//                                PreferencesEko.setStringPref(LOGINSTATUS, "NOL", getApplicationContext());
                                        Toasty.normal(getApplicationContext(), submitTransactionResponse.getMessage() + " Upload Failed", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();


                                    }

                                }


                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    dialog.dismiss();
                                    try {

                                        String response = new String(responseBody);
                                        //Log.e("failed", response);
                                        // productModel productModel = new Gson().fromJson(response, productModel.class);
                                        Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : AA", Toast.LENGTH_SHORT).show();


                                    } catch (NullPointerException e) {


                                        Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : BB", Toast.LENGTH_SHORT).show();

                                    } catch (Exception ex) {
                                        Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : CC", Toast.LENGTH_SHORT).show();

                                    }

                                }


                            });
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }


                }
            }
        });




        verificationcard= findViewById(R.id.verificationcard);
        verificationcard.setOnClickListener(v ->
        {
            //Toasty.normal(getApplicationContext(), "TESTING TOST", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), VerifyActivity.class));
            finish();
        });

        linlay_transactions= findViewById(R.id.linlay_transactions);
        linlay_transactions.setOnClickListener(v ->
        {
            //Toasty.normal(getApplicationContext(), "TESTING TOST", Toast.LENGTH_LONG).show();
            doUplad();
        });
    }


    public void  showForgotPass()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
        final EditText txte_username = new EditText(getApplicationContext());
        txte_username.setHint("Username");

        alertDialogBuilder.setView(txte_username);

        alertDialogBuilder.setMessage("Select an option.");
        alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                String str_usernameRescue = txte_username.getText().toString().trim();

                if (str_usernameRescue.isEmpty())
                {
                    Toasty.error(getApplicationContext(), "Enter Username!", Toast.LENGTH_LONG).show();
                }
                else
                {

                }
            }
        });


        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
        {
            //Override
            public void onClick(DialogInterface dialog, int which)
            {
                //finish();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void dologin(String str_login, String str_username, String str_password, String str_deviceIMIEI, String str_simSerialno)
    {
        String url = getString(R.string.liveurl)+"/Login/poslogin";
        Log.e("Calling URL login1:", "-> "+url);

        dialog.show();

        LoginRequestModel loginRequestModel = new LoginRequestModel();
//        loginRequestModel.set(str_login);
        loginRequestModel.setUserName(str_username);
        loginRequestModel.setPassword(str_password);
        loginRequestModel.setDeviceIMEI(str_deviceIMIEI);
        loginRequestModel.setSIMCardSerial(str_simSerialno);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(45000);
        client.setConnectTimeout(45000);

        client.setSSLSocketFactory(new SSLSocketFactory(Global.getSslContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
        try
        {
            client.post(getApplicationContext(), url, new StringEntity(new Gson().toJson(loginRequestModel)), "application/json", new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    String response = new String(responseBody);

                    PreferencesEko.setStringPref(Global.LOGINRESP,response, getApplicationContext());

                    //String loginRespFromMem = PreferencesEko.getStringPref(Global.LOGINRESP, getApplicationContext());

                    LoginResponseModel logrespmod = new Gson().fromJson(response, LoginResponseModel.class);

                    //String status = String.valueOf(productModel.getStatus());
                    Log.e("RESPONSE FROM Server:", response);
                    //Toasty.success(getApplicationContext(), status, Toast.LENGTH_LONG, true).show();

                    if (logrespmod.getCode()==200)
                    {
                        Log.e("RESPONSE SOURCE:", "");
                        String sJson =  new Gson().toJson(logrespmod);
                        //int walletbalance = productModel.getWalletBalance();
                        //PreferencesEko.setStringPref(Global.TRANSACTIONSSTORED,  response, getApplicationContext());

                        JSONObject stringResponse = null;
                        try
                        {
                            Log.e("RESPONSE JSON SOURCE:", response);
                            stringResponse = new JSONObject(response);
                            JSONArray ja_data = stringResponse.getJSONArray("data");

                            for (int i = 0; i < ja_data.length(); i++)
                            {
                                Log.e("RESPONSE ARRAY SOURCE:", "");
                                JSONObject json = ja_data.getJSONObject(i);


                                String agent_id = json.getString("Id");
                                String FirstName = json.getString("FirstName");
                                String LastName = json.getString("LastName");
                                String QrCode = json.optString("QRCodeUrl");
                                String Appname = json.optString("APPNAME");
                                Log.e("APP NAME---", Appname);
                                Log.e("QT NAME---", QrCode);
                                Log.e("FNA NAME---", FirstName);
                                Log.e("LNA NAME---", LastName);

                                PreferencesEko.setStringPref(Global.AGENTID, String.valueOf(agent_id), getApplicationContext());
                                PreferencesEko.setStringPref(Global.FIRSTNAME, String.valueOf(FirstName), getApplicationContext());
                                PreferencesEko.setStringPref(Global.LASTNAME, String.valueOf(LastName), getApplicationContext());
                                PreferencesEko.setStringPref(Global.URLSCAN, String.valueOf(QrCode), getApplicationContext());
                                PreferencesEko.setStringPref(Global.APPNAME, String.valueOf(Appname), getApplicationContext());

                                String jsonConts = json.getString("content");

                                JSONObject jsonContent = new JSONObject(jsonConts);
                                String checkINDATE = jsonContent.getString("NextCheckinDate");
                                String lastSync = jsonContent.getString("LastSync");
                                String checkinBy = jsonContent.getString("CheckInBy");
                                int unsurrenderdAmount = jsonContent.getInt("UnsurrenderdAmount");
                                double cashLimitAmount = jsonContent.getDouble("CashLimitAmount");
                                double cashLimitBal = jsonContent.getDouble("CashLimitBal");

                                PreferencesEko.setStringPref(Global.CASHLIMIT_AMOUNT, String.valueOf(cashLimitAmount), getApplicationContext());
                                PreferencesEko.setStringPref(Global.CASHLIMITBAL_AMOUNT, String.valueOf(cashLimitBal), getApplicationContext());
                                PreferencesEko.setIntPref(Global.UNSURRENDERED_AMOUNT, unsurrenderdAmount, getApplicationContext());
                                PreferencesEko.setStringPref(Global.NEXTCHECKIN_DATE, checkINDATE, getApplicationContext());
                                PreferencesEko.setStringPref(Global.LAST_SYNC, lastSync, getApplicationContext());
                                PreferencesEko.setStringPref(Global.CHECKINBY,checkinBy, getApplicationContext());
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }


                        //str_loginstatus = new String();
                        String str_transaction = "revenues";
                        String str_username = PreferencesEko.getStringPref(Global.USERNAME, getApplicationContext());
                        String str_password = PreferencesEko.getStringPref(Global.PASSWORD, getApplicationContext());
                        PreferencesEko.setStringPref(Global.LOGINSTATUS, "OKL", getApplicationContext());
                        PreferencesEko.setStringPref(Global.USERNAME, str_username, getApplicationContext());
                        PreferencesEko.setStringPref(Global.PASSWORD, str_password, getApplicationContext());

                        // dialog.dismiss();

                        String str_deviceIMIEI = PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplicationContext());
                        String str_simSerialno = PreferencesEko.getStringPref(Global.SERIALNUMB, getApplicationContext());

                        fetchTransactions(str_transaction, str_username, str_password, str_deviceIMIEI, str_simSerialno);
                    }
                    else
                    {
                        PreferencesEko.setStringPref(LOGINSTATUS, "NOL", getApplicationContext());

                        Toasty.normal(getApplicationContext(), logrespmod.getMessage()+" Authentication Failure!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        dialog.dismiss();
                        finish();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                {
                    dialog.dismiss();
                    try
                    {
                        String response = new String(responseBody);
                        //Log.e("failed", response);
                        // productModel productModel = new Gson().fromJson(response, productModel.class);
                        Toasty.normal(getApplicationContext(), "Check internet!", Toast.LENGTH_SHORT).show();

                    }
                    catch (NullPointerException e)
                    {
                        Toasty.normal(getApplicationContext(), "We were not able to complete your request", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }


    public void fetchTransactions(String str_transaction, String str_username, String str_password, String str_deviceIMIEI, String str_simSerialno)
    {
        Log.e("In LoginActivity -> ", "inside fetchTransactions Method().");


        str_deviceIMIEI = PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplicationContext());
        str_simSerialno = PreferencesEko.getStringPref(Global.SERIALNUMB, getApplicationContext());


        FetchTransactionsModel fetchTransactionsModel = new FetchTransactionsModel();
        fetchTransactionsModel.setDeviceIMEI(str_deviceIMIEI);
        fetchTransactionsModel.setPassword(PreferencesEko.getStringPref(Global.PASSWORD, getApplicationContext()));
        fetchTransactionsModel.setSIMCardSerial(str_simSerialno);
        fetchTransactionsModel.setUserName(PreferencesEko.getStringPref(Global.USERNAME, getApplicationContext()));

        //String url = getString(R.string.baseurl)+"/transactions";
        String url = getString(R.string.liveurl)+"/FFATransaction/Transactions";
        Log.e("Calling URL trxn1:", "-> "+url);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(45000);
        client.setConnectTimeout(45000);

        client.setSSLSocketFactory(new SSLSocketFactory(Global.getSslContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
        try
        {
            client.post(getApplicationContext(), url, new StringEntity(new Gson().toJson(fetchTransactionsModel)), "application/json", new AsyncHttpResponseHandler()
            {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {

                    String response = new String(responseBody);
                    Log.e("ress", response);

                    PreferencesEko.setStringPref(Global.TRANSACTIONSRESP, response, getApplicationContext());

                    FetchTransactionsResponseModel fetchTransactionsResponseModel = new Gson().fromJson(response, FetchTransactionsResponseModel.class);

                    if (fetchTransactionsResponseModel.getCode()==200)
                    {
                        fetchRevenueProducts();
                    }
                    else
                    {

                        fetchRevenueProducts();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                {
                    dialog.dismiss();
                    try
                    {
                        Toasty.normal(getApplication().getApplicationContext(), "Failed to fetch Revenue Collection : TR", Toast.LENGTH_LONG).show();
                        fetchRevenueProducts();
                    }
                    catch (NullPointerException e)
                    {
                        e.printStackTrace();
                        Toasty.normal(getApplication().getApplicationContext(), "Failed to fetch Revenue Collection : TRN", Toast.LENGTH_LONG).show();
                        fetchRevenueProducts();
                    }
                }
            });
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();

        }

    }

    private void fetchRevenueProducts()
    {
        FetchCollectionRevenueModel fetchCollectionRevenueModel = new FetchCollectionRevenueModel();
        fetchCollectionRevenueModel.setDeviceIMEI(PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplicationContext()));
        fetchCollectionRevenueModel.setPassword(PreferencesEko.getStringPref(Global.PASSWORD, getApplicationContext()));
        fetchCollectionRevenueModel.setSIMCardSerial(PreferencesEko.getStringPref(Global.SERIALNUMB, getApplicationContext()));
        fetchCollectionRevenueModel.setUserName(PreferencesEko.getStringPref(Global.USERNAME, getApplicationContext()));

        String url = getString(R.string.liveurl)+"/POSFee/revenues";
        Log.e("Calling URL revns1:", "-> "+url);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(45000);
        client.setConnectTimeout(45000);

        client.setSSLSocketFactory(new SSLSocketFactory(Global.getSslContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
        try
        {
            client.post(getApplicationContext(), url, new StringEntity(new Gson().toJson(fetchCollectionRevenueModel)), "application/json", new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    //dialog.dismiss();
                    String response = new String(responseBody);
                    Log.e("ress", response);

                    PreferencesEko.setStringPref(Global.REVENUESRESP,response, getApplicationContext());
                    String str_revenueresp = PreferencesEko.getStringPref(Global.REVENUESRESP, getApplicationContext());
                    Log.e("RevenueResp is: ", str_revenueresp);

                    ProductModel productModel = new Gson().fromJson(response, ProductModel.class);
                    //Log.e("ress", response);

                    if (productModel.getCode()==200)
                    {
                        dialog.dismiss();
                        Toasty.success(getApplicationContext(), "Download Successful!", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        finish();
                    }
                    else
                    {

                        Toasty.error(getApplicationContext(), "Failed to fetch Revenue Collection : R", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                {
                    dialog.dismiss();
                    try
                    {
                        String response = new String(responseBody);
                        //Log.e("ress", response);
                        Toasty.normal(getApplicationContext(), "Failed to fetch Revenue Collection : RV", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    }
                    catch (NullPointerException e)
                    {
                        e.printStackTrace();
                        Toasty.normal(getApplicationContext(), "Failed to fetch Revenue Collection : RVN", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    }
                }
            });

        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    public void doUplad()
    {
        databaseHelper = new DatabaseHelper(Dashboard.this);
        int checkAvailableTransactionsCount = databaseHelper.checkIfItemExistsInTransactions();
        if (checkAvailableTransactionsCount == 0)
        {
            //Toasty.normal(getApplicationContext(), "No Pending Transactions", Toast.LENGTH_LONG).show();

            startActivity(new Intent(getApplicationContext(), AllTransactionsActivity.class));
            finish();
        }
        else
        {
            dialog.setCancelable(false);
            dialog.show();

            offlineReceiptModels = new ArrayList<OfflineReceiptModel>();
            offlineReceiptModels = databaseHelper.getAllOfllineSavedReceipts();

            if (offlineReceiptModels.size() > 0) {
                for (OfflineReceiptModel offlineReceiptModels : offlineReceiptModels) {

                    offlineSavedTransactionModels = new ArrayList<OfflineSavedTransactionModel>();
                    offlineSavedTransactionModels = databaseHelper.getAllTransactionsWithThisReceiptNumber(offlineReceiptModels.getCartId());

                    for(OfflineSavedTransactionModel offlineSavedTransactionModel : offlineSavedTransactionModels)
                    {
                        payerName = offlineSavedTransactionModel.getFirstname()+ " "+offlineSavedTransactionModel.getLastname();
                        payerId = String.valueOf(offlineReceiptModels.getPayer_id());
                        paymentMethod = offlineSavedTransactionModel.getPayment_method();
                    }


                    String imeiDevice = PreferencesEko.getStringPref(Global.DEVICEIMEI, getApplication());
                    String simcardSerial = PreferencesEko.getStringPref(Global.SERIALNUMB, getApplication());
                    String username = PreferencesEko.getStringPref(Global.USERNAME, getApplication());
                    String passwd = PreferencesEko.getStringPref(Global.PASSWORD, getApplication());

                    TransactionSubmitRequestModel transactionSubmitRequestModel = new TransactionSubmitRequestModel();
                    transactionSubmitRequestModel.setSIMCardSerial(simcardSerial);
                    transactionSubmitRequestModel.setUserName(username);
                    transactionSubmitRequestModel.setDeviceIMEI(imeiDevice);
                    transactionSubmitRequestModel.setRequestid(String.valueOf(offlineReceiptModels.getCartId()));
                    transactionSubmitRequestModel.setPassword(passwd);
                    transactionSubmitRequestModel.setPayer(payerName);
                    transactionSubmitRequestModel.setPayerid(payerId);
                    transactionSubmitRequestModel.setPaymode(paymentMethod);
                    transactionSubmitRequestModel.setBillTotal(offlineReceiptModels.getBillTotal());


                    List<TransactionDetails> transactionDetails = new ArrayList<>();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

//                                    offlineSavedTransactionModels.stream().forEach(ls -> transactionDetails.add(new TransactionDetails(ls.getItemId(), ls.getItemName(), ls.getUnitprice(), ls.getQty(), ls.getLineTotal(), paymentMode, payerName, "123456")));
                        offlineSavedTransactionModels.stream().forEach(ls -> transactionDetails.add(new TransactionDetails(ls.getItemId(), ls.getItemName(), ls.getUnitprice(), ls.getQty(), ls.getLineTotal())));

                    }

                    transactionSubmitRequestModel.setTransaction(transactionDetails);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.setTimeout(45000);
                    client.setConnectTimeout(45000);

                    //String url = getString(R.string.baseurl) + "/transaction";
                    String url = getString(R.string.liveurl)+"/FFATransaction/POSPost";
                    Log.e("Calling URL posttrx2:", "-> "+url);


                    System.out.println("=======REQUEST BODY====== " + new Gson().toJson(transactionSubmitRequestModel));

                    client.setSSLSocketFactory(new SSLSocketFactory(Global.getSslContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
                    try {
                        client.post(getApplicationContext(), url, new StringEntity(new Gson().toJson(transactionSubmitRequestModel)), "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                dialog.dismiss();
                                String response = new String(responseBody);
                                PreferencesEko.setStringPref(Global.LOGINRESP, response, getApplicationContext());
                                SubmitTransactionResponse submitTransactionResponse = new Gson().fromJson(response, SubmitTransactionResponse.class);
                                Log.e("RESPONSE FROM Server:", response);


                                if (submitTransactionResponse.getCode() == 200)
                                {

//                                                JSONObject jsonObject = null;
//                                                try {
//                                                    jsonObject = new JSONObject(response);
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                JSONArray arr = null;
//                                                try {
//                                                    arr = jsonObject.getJSONArray("data");
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                for (int i = 0; i < arr.length(); i++) {
//                                                    JSONObject json = null;
//                                                    try {
//                                                        json = arr.getJSONObject(i);
//                                                    } catch (JSONException e) {
//                                                        e.printStackTrace();
//                                                    }
//
//                                                    try {
//                                                        receiptNo = json.getString("receiptnumber");
//                                                    } catch (JSONException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                    System.out.println(receiptNo);
//                                                }


//
                                    databaseHelper.deleteReceiptItems(offlineReceiptModels.getCartId());
                                    databaseHelper.deleteSubmittedTransactionItems(offlineReceiptModels.getCartId());
                                    //Toasty.success(getApplicationContext(), "Transactions Submitted successfully", Toast.LENGTH_SHORT).show();

                                    //dialog.dismiss();

                                    startActivity(new Intent(getApplicationContext(), AllTransactionsActivity.class));
                                    finish();


                                } else {

//                                PreferencesEko.setStringPref(LOGINSTATUS, "NOL", getApplicationContext());
                                    //Toasty.normal(getApplicationContext(), submitTransactionResponse.getMessage() + " Upload Failed", Toast.LENGTH_LONG).show();
                                    //dialog.dismiss();
                                    startActivity(new Intent(getApplicationContext(), AllTransactionsActivity.class));
                                    finish();


                                }

                            }


                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                dialog.dismiss();
                                try {

                                    String response = new String(responseBody);
                                    //Log.e("failed", response);
                                    // productModel productModel = new Gson().fromJson(response, productModel.class);
                                    //Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : AA", Toast.LENGTH_SHORT).show();


                                } catch (NullPointerException e) {


                                    //Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : BB", Toast.LENGTH_SHORT).show();

                                } catch (Exception ex) {
                                    //Toasty.normal(getApplicationContext(), "Uplopad Failed. Check Internet : CC", Toast.LENGTH_SHORT).show();

                                }

                            }


                        });
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }


            }
        }
    }
}
