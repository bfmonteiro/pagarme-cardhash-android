package com.qrntt.pagarmecardhash;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;


public class CardHash {

    private static final String API_URL = "https://api.pagar.me/1/transactions/card_hash_key";
    private static final String META_DATA_KEY = "me.pagar.EncryptionKey";

    public String holderName;
    public String number;
    public String expirationDate;
    public String cvv;

    public CardHash() {
    }

    public void generate(Context context, final Listener listener) {
        getPublicKey(context,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final String cardHash = buildCardHash(response);
                            listener.onSucess(cardHash);
                        } catch (Exception e) {
                            listener.onError(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                });
    }


    private void getPublicKey(Context context,
                              Response.Listener<JSONObject> responseListener,
                              Response.ErrorListener errorListener) {
        final String apiKey = getApiKey(context);
        final String url = String.format("%s?encryption_key=%s", API_URL, apiKey);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                responseListener, errorListener);
        final RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }


    private String buildCardHash(JSONObject publicKeyResponse)
            throws Exception {
        final long publicKeyId = publicKeyResponse.getLong("id");
        final String publicKey = publicKeyResponse.getString("public_key");
        final String cardData = String.format("card_number=%s"
                        + "&card_holder_name=%s"
                        + "&card_expiration_date=%s"
                        + "&card_cvv=%s",
                number, holderName, expirationDate, cvv);
        final String encryptedData = encrypt(cardData, publicKey);
        return String.format("%d_%s", publicKeyId, encryptedData);
    }


    private String encrypt(String plain, String publicKey) throws Exception {
        final String pubKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        final byte[] keyBytes = Base64.decode(pubKeyPEM.getBytes("utf-8"), Base64.DEFAULT);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        final PublicKey key = keyFactory.generatePublic(spec);
        final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final byte[] encryptedBytes = cipher.doFinal(plain.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }


    private String getApiKey(Context context) {
        try {
            final ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData.getString(META_DATA_KEY);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException("invalid context");
        }
    }


    public interface Listener {
        void onSucess(String cardHash);
        void onError(Exception e);
    }
}