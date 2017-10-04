package adriano.wifimap;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LinearLayout baseProgressBar;
    private static final String TAG = "Controller";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        baseProgressBar = (LinearLayout)findViewById(R.id.baseProgressBar);
        new setMarkers().execute();
    }

    private class setMarkers extends AsyncTask<String, Integer, String> implements OnMapReadyCallback{
        private JSONArray json= new JSONArray();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String url = "http://www.portoalegrelivre.com.br/php/services/WSPoaLivreRedes.php";
            try {

                HttpURLConnection conexao = conectar(url);
                int resposta = conexao.getResponseCode();

                if (resposta ==  HttpURLConnection.HTTP_OK) {
                    InputStream is = conexao.getInputStream();
                    json = new JSONArray(bytesParaString(is));
                }

            } catch (Exception e) {
                Log.e("TAG_ASYNC_TASK", e.getMessage());
            }
            return result;
        }



        @Override
        protected void onPostExecute(String s) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            baseProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onMapReady(GoogleMap googleMap){
            mMap = googleMap;
            for(int i=0;i<json.length();i++){
                try {
                    LatLng markerPosition = new LatLng(json.getJSONObject(i).getDouble("Latitude"), json.getJSONObject(i).getDouble("Longitude"));
                    mMap.addMarker(new MarkerOptions().position(markerPosition).title("NomeRede:"+json.getJSONObject(i).getString("NomeRede")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-30.0277,-51.2287)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        private String bytesParaString(InputStream is) throws IOException {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bufferzao = new ByteArrayOutputStream();
            int bytesLidos;
            while ((bytesLidos = is.read(buffer)) != -1) {
                bufferzao.write(buffer, 0, bytesLidos);
            }
            return new String(bufferzao.toByteArray(), "UTF-8");
        }


        private HttpURLConnection conectar(String urlArquivo) throws IOException {
            final int SEGUNDOS = 1000;
            URL url = new URL(urlArquivo);
            HttpURLConnection conexao = (HttpURLConnection)url.openConnection();
            conexao.setReadTimeout(10 * SEGUNDOS);
            conexao.setConnectTimeout(15 * SEGUNDOS);
            conexao.setRequestMethod("GET");
            conexao.setDoInput(true);
            conexao.setDoOutput(false);
            conexao.connect();
            return conexao;
        }
    }
}
