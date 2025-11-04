package com.example.weatherapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val apikey = "2f9019260bc8045ff042f51300a75f4d"
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editCity = findViewById<EditText>(R.id.edit_city)
        val btnBuscar = findViewById<Button>(R.id.btn_search)
        val txtCidade = findViewById<TextView>(R.id.text_city)
        val txtTemperatura = findViewById<TextView>(R.id.text_temperature)
        val txtClima = findViewById<TextView>(R.id.text_condition)
        val imageWeatherIcon = findViewById<ImageView>(R.id.image_weather_icon)

        btnBuscar.setOnClickListener {
            val city = editCity.text.toString().trim()
            if (city.isEmpty()) {
                Toast.makeText(this, "Digite o nome de uma cidade!", Toast.LENGTH_SHORT).show()
            } else {
                buscarClima(city, txtCidade, txtTemperatura, txtClima, imageWeatherIcon)
            }
        }
    }

    private fun buscarClima(
        city: String,
        txtCidade: TextView,
        txtTemperatura: TextView,
        txtClima: TextView,
        imageWeatherIcon: ImageView
    ) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apikey&units=metric&lang=pt_br"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao buscar clima: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val main = json.getJSONObject("main")
                        val weather = json.getJSONArray("weather").getJSONObject(0)

                        val temperatura = main.getDouble("temp")
                        val descricao = weather.getString("description")
                        val cidade = json.getString("name")

                        runOnUiThread {
                            txtCidade.text = cidade
                            txtTemperatura.text = "%.1f°C".format(temperatura)
                            txtClima.text = descricao.replaceFirstChar { it.uppercase() }

                            // 🧠 Troca o ícone conforme o clima
                            when {
                                descricao.contains("chuva", ignoreCase = true) -> {
                                    imageWeatherIcon.setImageResource(R.drawable.ic_rain)
                                }
                                descricao.contains("nublado", ignoreCase = true) ||
                                        descricao.contains("nuvem", ignoreCase = true) -> {
                                    imageWeatherIcon.setImageResource(R.drawable.ic_cloudy)
                                }
                                descricao.contains("noite", ignoreCase = true) -> {
                                    imageWeatherIcon.setImageResource(R.drawable.ic_night)
                                }
                                else -> {
                                    imageWeatherIcon.setImageResource(R.drawable.ic_sunny)
                                }
                            }
                        }

                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Cidade não encontrada.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }
}

