package com.justai.jaicf.template.httpRequest

import com.justai.jaicf.template.structures.Coordinates
import com.justai.jaicf.template.structures.Weather
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

fun getCoordinates (cityName : String) : Coordinates {
    var cityData = URL(
        "http://api.openweathermap.org/geo/1.0/direct?q=${cityName}" +
                "&limit=5&appid=0b6281885400ddf04c0b75fc7066989c"
    ).readText()

    val cityJson = JSONArray(cityData)
    val firstElement = cityJson.getJSONObject(0)

    return Coordinates(firstElement.getInt("lat").toString(),
                       firstElement.getInt("lon").toString())
}

fun getWeather (coordinates: Coordinates) : Weather {
    var weatherData = URL("https://api.openweathermap.org/data/2.5/onecall?" +
            "lat=${coordinates.lat}&lon=${coordinates.lon}" +
            "&exclude=hourly,daily&" +
            "appid=0b6281885400ddf04c0b75fc7066989c"
    ).readText()
    val weatherJson = JSONObject(weatherData)
    val currentWeatherJson = weatherJson.getJSONObject("current")

    return Weather((currentWeatherJson.getInt("temp") - 273).toString(),
        (currentWeatherJson.getInt("feels_like") - 273).toString(),
        currentWeatherJson.getInt("wind_speed").toString(),
        currentWeatherJson.getInt("clouds").toString())
}