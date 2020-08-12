package com.stambulo.room;

import android.content.DialogInterface;
import android.content.SearchRecentSuggestionsProvider;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.stambulo.room.interfaces.OpenWeather;
import com.stambulo.room.model.WeatherRequest;
import com.stambulo.room.room.MyDatabase;
import com.stambulo.room.room.WeatherHistory;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static MyDatabase myDatabase;

    private static final float AbsoluteZero = -273.15f;
    private SharedPreferences sharedPref;
    private OpenWeather openWeather;
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
        initRoom();
        initEvents();
        initViews();
        initPreferences();
    }

    private void initRoom() {
        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "weatherDB")
                .allowMainThreadQueries()
                .build();
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
                if (city.equals("")) {
                    Toast.makeText(getApplicationContext(), "Введите название города", Toast.LENGTH_SHORT).show();
                } else {
                    textCity.setText(city);
                    requestRetrofit(inputCityName.getText().toString(), keyAPI);
                }
            }
        });

        Button btnHistory = findViewById(R.id.history);
        btnHistory.setOnClickListener(clickAlertDialog);
    }

    private View.OnClickListener clickAlertDialog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            List<WeatherHistory> weatherHistories = myDatabase.myDAO().getWeatherHistory();
            String info = "";

            // Текущее время
            Date currentDate = new Date();
            // Форматирование времени как "день.месяц.год"
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String dateText = dateFormat.format(currentDate);

            final String[] historyList = new String[weatherHistories.size()];
            for (int i = 0; i < weatherHistories.size(); i++) {
                String city = weatherHistories.get(i).getCity();
                String temp = weatherHistories.get(i).getTemp();
                info = city + " \nТ=" + temp + " ℃" + "\n" + dateText + "\n";
                historyList[i] = info;
            }
            // Создаем билдер и передаем контекст приложения
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // в билдере указываем заголовок окна (можно указывать как ресурс, так и строку)
            builder.setTitle(R.string.history)
                    // Добавим список элементов
                    .setItems(historyList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int item) {
                            String city = historyList[item];
                            String[] tokens = city.split(" ");
                            textCity.setText(tokens[0]);
                            inputCityName.setText(tokens[0]);
                            requestRetrofit(tokens[0], keyAPI);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    /*private void addCityToArray(String city) {
        String[] newArray = Arrays.copyOf(historyList, historyList.length + 1);
        newArray[newArray.length - 1] = city;
        historyList = Arrays.copyOf(newArray, newArray.length);
    }*/

    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        openWeather = retrofit.create(OpenWeather.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    private void requestRetrofit(final String city, String keyApi) {
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("city", city);
        openWeather.loadWeather(city, keyApi)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            float temp = response.body().getMain().getTemp() + AbsoluteZero;
                            int iTemp = Math.round(temp);
                            String sTemp = Integer.toString(iTemp);
                            currentTemperatureTextView.setText(sTemp);
                            editor.putString("temp", sTemp);

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

                            WeatherHistory weatherHistory = new WeatherHistory();
                            weatherHistory.setCity(city);
                            weatherHistory.setTemp(sTemp);
                            myDatabase.myDAO().addWeatherHistory(weatherHistory);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        currentTemperatureTextView.setText("Error");
                    }
                });
    }
}