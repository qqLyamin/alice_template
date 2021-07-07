package com.justai.jaicf.template.scenario

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.yandexalice.alice
import com.justai.jaicf.channel.yandexalice.model.AliceEvent
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.helpers.logging.log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


val MainScenario = Scenario {

    var city = ""
    var cityData = ""
    var weatherData = ""
    var lat = ""
    var lon = ""

    state("main") {
        activators {
            event(AliceEvent.START)
        }

        action {
            reactions.run {
                buttons("Да", "Нет")
                alice?.image(
                    "https://i.imgur.com/u96v35i.jpeg",
                    "Привет! Я - погодный чатбот.",
                    "Вас интересует погода? А то я больше ничего не умею..."
                )
            }
        }
    }

    state("yes") {
        activators {
            regex("да|хочу|ага|интересует")
        }

        action {
            record("У меня нет Ваших геоданных, в каком" +
                    " городе Вас интересует погода?", "city")
        }

        state("city") {
            activators {
                catchAll()
            }

            action {
                city = request.input
                log(city);
                cityData = URL(
                    "http://api.openweathermap.org/geo/1.0/direct?q=${city}" +
                            "&limit=5&appid=0b6281885400ddf04c0b75fc7066989c"
                ).readText()

                val cityJson = JSONArray(cityData)
                val currentCity = cityJson.getJSONObject(0)
                lat = currentCity.getInt("lat").toString()
                lon = currentCity.getInt("lon").toString()
                log(lat + " " + lon);

                if (!cityData.isEmpty()) {
                    weatherData = URL("https://api.openweathermap.org/data/2.5/onecall?" +
                            "lat=${lat}&lon=${lon}" +
                            "&exclude=hourly,daily&" +
                            "appid=0b6281885400ddf04c0b75fc7066989c"
                    ).readText()
                }

                val weatherJson = JSONObject(weatherData)
                val currentWeather = weatherJson.getJSONObject("current")
                val temperature = (currentWeather.getInt("temp") - 273).toString()
                val feelsLike = (currentWeather.getInt("feels_like") - 273).toString()
                val windSpeed = currentWeather.getInt("wind_speed").toString()

                record(
                    "Погода в городе ${city} :\n" +
                            "Температура воздуха - ${temperature}\n" +
                            "Ощущается как - ${feelsLike}\n" +
                            "Скорость ветра - ${windSpeed}.\n" +
                            "А больше я ничего сказать не могу...",
                    "done"
                )
            }
        }


        state("done") {
            action {
                reactions.alice?.endSession()
            }
        }
    }


    state("no") {
        activators {
            regex("нет|не хочу")
        }

        action {
            reactions.say("Тогда не отвлекайте меня от работы. До свидания!")
            reactions.alice?.endSession()
        }
    }

    fallback {
        reactions.say("2Привет! Я - погодный чатбот и я в небольшой депрессии... " +
                "Так что если Вас реально интересует погода - скажите об этом побыстрей " +
                "и покончим с этим.")
        reactions.buttons("Да", "Нет")
    }
}

fun DefaultActionContext.record(message: String, path: String) {
    reactions.say(message)
    reactions.changeState(path)
}