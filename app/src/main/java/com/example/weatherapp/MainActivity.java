package com.example.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

//http://api.openweathermap.org/data/2.5/weather?q=Katowice&lang=PL&units=metric&appid=a71fa7369a61aae850d392b6c8d96807


public class MainActivity extends AppCompatActivity {

    TextView selectCity, cityField, detailsField,
            currentTemperatureField, minTempField, maxTempField,
            humidityField, pressureField, windField,
            weatherIcon, dateField;

    ProgressBar loader;
    Typeface weatherFont;
    public static String city = "Katowice";
    public static String city_last = city;
    String OPEN_WEATHER_MAP_API = "a71fa7369a61aae850d392b6c8d96807";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        loader = (ProgressBar) findViewById(R.id.loader);
        selectCity = (TextView) findViewById(R.id.select_city);
        cityField = (TextView) findViewById(R.id.city_field);
        dateField = (TextView) findViewById(R.id.date_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temp_field);
        minTempField = (TextView) findViewById(R.id.min_temp_field);
        maxTempField = (TextView) findViewById(R.id.max_temp_field);
        windField = (TextView) findViewById(R.id.wind_field);
        humidityField = (TextView) findViewById(R.id.humidity_field);
        pressureField = (TextView) findViewById(R.id.pressure_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);

        taskLoadUp(city);

        city_last = city;

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Change city");
                final EditText input = new EditText(MainActivity.this);
                input.setText(city);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                city = input.getText().toString();
                                taskLoadUp(city);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

    }


    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    class DownloadWeather extends AsyncTask < String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);

        }
        protected String doInBackground(String...args) {
            String xml = Function.executeGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&lang=pl&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    JSONObject wind = json.getJSONObject("wind");

                    DateFormat df = DateFormat.getDateTimeInstance();

                    cityField.setText(json.getString("name").toUpperCase(Locale.getDefault()) + ", " + json.getJSONObject("sys").getString("country"));
                    detailsField.setText(details.getString("description").toUpperCase(Locale.getDefault()));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + " °C");

                    minTempField.setText(String.format("%.2f", main.getDouble("temp_min")) + " °C");
                    maxTempField.setText(String.format("%.2f", main.getDouble("temp_max")) + " °C");

                    windField.setText(wind.getInt("speed")*3.6 + " km/h");

                    humidityField.setText(main.getString("humidity") + "%");
                    pressureField.setText(main.getString("pressure") + " hPa");
                    dateField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000 )));

                    loader.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "     Error!\nCheck City", Toast.LENGTH_SHORT).show();
                openCityError();

            }
        }
    }

    public void openCityError(){
        Intent intent = new Intent(this, CityError.class);
        startActivity(intent);
    }
}