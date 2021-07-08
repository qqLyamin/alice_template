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
                buttons("Москва", "Питер")
                alice?.image(
                    "https://i.imgur.com/u96v35i.jpeg",
                    "Привет!\n" +
                            "Я Чайка и я слежу за погодой!" +
                            "В каком городе Вас интересует погода?" +
                            "И интересует ли?"
                )
            }
        }
    }

    state("Moscow") {
        activators {
            regex(
                "в Москве|в москве|москва|Москва|" +
                        "погода в москве|мск|Мск|" +
                        "погода в мск"
            )
        }

        action {
            var cityCoordinates: Coordinates = getCoordinates("Москва")
            var currentWeather: Weather = getWeather(cityCoordinates)

            record(
                "Погода в городе Москва :\n" +
                        "Температура воздуха - ${currentWeather.temperature} °С\n" +
                        "Ощущается как - ${currentWeather.feelsLike} °С\n" +
                        "Скорость ветра - ${currentWeather.windSpeed} м/с.\n" +
                        "Облачность - ${currentWeather.clouds} %.\n" +
                        "А больше я ничего сказать не могу...",
                "done"
            )
        }
    }
    state("Piter") {
        activators {
            regex(
                "в Питере|в питере|питер|Питер|" +
                        "погода в питере|спб|Спб|СпБ|СПБ|" +
                        "погода в спб"
            )
        }

        action {
            var cityCoordinates: Coordinates = getCoordinates("Санкт-Петербург")
            var currentWeather: Weather = getWeather(cityCoordinates)

            record(
                "Погода в городе Санкт-Петербург :\n" +
                        "Температура воздуха - ${currentWeather.temperature} °С\n" +
                        "Ощущается как - ${currentWeather.feelsLike} °С\n" +
                        "Скорость ветра - ${currentWeather.windSpeed} м/с.\n" +
                        "Облачность - ${currentWeather.clouds} %.\n" +
                        "А больше я ничего сказать не могу...",
                "done"
            )
        }
    }
    state("Yes") {
        activators {
            regex(
                "да|Да|интересует|расскажи|" +
                        "ну и какая|какая|давай|" +
                        "ну|допустим"
            )
        }
        action {
            record("У меня нет Ваших геоданных, в каком" +
                    " городе Вас интересует погода?", "city")
        }
        state("Yes") {
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
            regex("нет|не хочу|не интересуюсь|" +
                    "меня не интересует погода|не интересует" +
                    "|Ни в каком")
        }
        action {
            reactions.say("Тогда не отвлекайте меня от работы. До свидания!")
            reactions.alice?.endSession()
        }
    }
    state("done") {
        action {
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