package com.example.sunnyweather.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            nowIncludeLayout.navBtn.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {}
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {
                    val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    manager.hideSoftInputFromWindow(
                        drawerView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            })
        }

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        }
        binding.swipeRefresh.apply {
            setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
            setOnRefreshListener { refreshWeather() }
        }
        refreshWeather()

        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
    }

    fun closeDrawers() {
        binding.drawerLayout.closeDrawers()
    }

    private fun showWeatherInfo(weather: Weather) {
        binding.apply {
            // 填充now.xml布局中的数据
            val realtime = weather.realtime
            val daily = weather.daily
            val currentTempText = "${realtime.temperature.toInt()} ℃"
            val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
            nowIncludeLayout.apply {
                placeName.text = viewModel.placeName
                currentTemp.text = currentTempText
                currentSky.text = getSky(realtime.skycon).info
                currentAQI.text = currentPM25Text
                nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
            }
            // 填充forecast.xml布局中的数据
            forecastIncludeLayout.apply {
                forecastLayout.removeAllViews()
                val days = daily.skycon.size
                for (i in 0 until days) {
                    val skycon = daily.skycon[i]
                    val temperature = daily.temperature[i]
                    val view = LayoutInflater.from(this@WeatherActivity).inflate(
                        R.layout.forecast_item,
                        forecastLayout, false
                    )
                    val dateInfo = view.findViewById(R.id.dateInfo) as TextView
                    val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
                    val skyInfo = view.findViewById(R.id.skyInfo) as TextView
                    val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    dateInfo.text = simpleDateFormat.format(skycon.date)
                    val sky = getSky(skycon.value)
                    skyIcon.setImageResource(sky.icon)
                    skyInfo.text = sky.info
                    val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
                    temperatureInfo.text = tempText
                    forecastLayout.addView(view)
                }
            }
            // 填充life_index.xml布局中的数据
            lifeIndexIncludeLayout.apply {
                val lifeIndex = daily.lifeIndex
                coldRiskText.text = lifeIndex.coldRisk[0].desc
                dressingText.text = lifeIndex.dressing[0].desc
                ultravioletText.text = lifeIndex.ultraviolet[0].desc
                carWashingText.text = lifeIndex.carWashing[0].desc
            }
            // 视图更新完毕，父布局 scrollView 设为可见
            weatherLayout.visibility = View.VISIBLE
        }
    }
}
