package com.justai.jaicf.template.scenario

import com.justai.jaicf.template.structures.*
import com.justai.jaicf.template.httpRequest.*
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.yandexalice.alice
import com.justai.jaicf.channel.yandexalice.model.AliceEvent
import com.justai.jaicf.context.DefaultActionContext

val MainScenario = Scenario {

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
                    "Хотите расскажу про погоду, интересует? А то я больше ничего не умею..."
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
                var cityCoordinates : Coordinates = getCoordinates(request.input)
                var currentWeather : Weather = getWeather(cityCoordinates)

                record(
                    "Погода в городе ${request.input} :\n" +
                            "Температура воздуха - ${currentWeather.temperature} °С\n" +
                            "Ощущается как - ${currentWeather.feelsLike} °С\n" +
                            "Скорость ветра - ${currentWeather.windSpeed} м/с.\n" +
                            "Облачность - ${currentWeather.clouds} %.\n" +
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
        reactions.say("Привет! Я - погодный чатбот и я в небольшой депрессии... " +
                "Так что если Вас реально интересует погода - скажите об этом побыстрей " +
                "и покончим с этим.")
        reactions.buttons("Да", "Нет")
    }
}

fun DefaultActionContext.record(message: String, path: String) {
    reactions.say(message)
    reactions.changeState(path)
}