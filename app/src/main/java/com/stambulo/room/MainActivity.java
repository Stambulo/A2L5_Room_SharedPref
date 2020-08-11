package com.stambulo.room;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.stambulo.room.interfaces.OpenWeather;
import com.stambulo.room.model.WeatherRequest;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final float AbsoluteZero = -273.15f;
    private SharedPreferences sharedPref;
    private OpenWeather openWeather;
    private String[] historyList = {"Moscow"};
    private String keyAPI = "0d0685c3c18d92278b9ac3d7bd62d688";

    private TextView textCity;
    private TextView currentTemperatureTextView;
    private TextView textPressure;
    private TextView textHumidity;
    private TextView textWindSpeed;

    private TextInputEditText inputCityName;
    private MaterialButton refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetorfit();
        initEvents();
        initViews();
        initPreferences();
    }

    private void initViews() {
        textCity = findViewById(R.id.textCity);
        textPressure = findViewById(R.id.textPressure);
        textHumidity = findViewById(R.id.textHumidity);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        currentTemperatureTextView = findViewById(R.id.textTemprature);
        inputCityName = findViewById(R.id.inputCityName);
        refresh = findViewById(R.id.refresh);
    }

    private void initPreferences() {
        sharedPref = getPreferences(MODE_PRIVATE);
        textCity.setText(sharedPref.getString("city", "Moscow"));
        currentTemperatureTextView.setText(sharedPref.getString("temp", "20"));
        textHumidity.setText(sharedPref.getString("humidity", "35"));
        textPressure.setText(sharedPref.getString("pressure", "757"));
        textWindSpeed.setText(sharedPref.getString("windSpeed", "6"));
    }

    // Здесь создадим обработку клика кнопки
    private void initEvents() {
        Button btnRefresh = findViewById(R.id.refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = inputCityName.getText().toString();
                textCity.setText(city);
                requestRetrofit(inputCityName.getText().toString(), keyAPI);
                addCityToArray(city);
            }
        });

        Button btnHistory = findViewById(R.id.history);
        btnHistory.setOnClickListener(clickAlertDialog);
    }

    private View.OnClickListener clickAlertDialog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Создаем билдер и передаем контекст приложения
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // в билдере указываем заголовок окна (можно указывать как ресурс, так и строку)
            builder.setTitle(R.string.history)
                    // Добавим список элементов
                    .setItems(historyList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int item) {
                            String city = historyList[item];
                            textCity.setText(city);
                            requestRetrofit(city, keyAPI);
                            addCityToArray(city);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private void addCityToArray(String city) {
        String[] newArray = Arrays.copyOf(historyList, historyList.length + 1);
        newArray[newArray.length-1] = city;
        historyList = Arrays.copyOf(newArray, newArray.length);
    }

    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        openWeather = retrofit.create(OpenWeather.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    private void requestRetrofit(String city, String keyApi) {
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("city", city);
                openWeather.loadWeather(city, keyApi)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            float temp = response.body().getMain().getTemp() + AbsoluteZero;
                            currentTemperatureTextView.setText(Float.toString(temp));
                            editor.putString("temp", Float.toString(temp));

                            float humidity = response.body().getMain().getHumidity();
                            textHumidity.setText(Float.toString(humidity));
                            editor.putString("humidity", Float.toString(humidity));

                            float pressure = response.body().getMain().getPressure();
                            textPressure.setText(Float.toString(pressure));
                            editor.putString("pressure", Float.toString(pressure));

                            float windSpeed = response.body().getWind().getSpeed();
                            textWindSpeed.setText(Float.toString(windSpeed));
                            editor.putString("windSpeed", Float.toString(windSpeed));
                            editor.commit();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        currentTemperatureTextView.setText("Error");
                    }
                });
    }
}