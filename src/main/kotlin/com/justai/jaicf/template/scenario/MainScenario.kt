package com.justai.jaicf.template.scenario

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.yandexalice.model.AliceEvent
import com.justai.jaicf.channel.yandexalice.alice
import java.net.URL

var city = "";

val MainScenario = Scenario {
    append(RecordScenario)

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
            var cityData = ""
            action {
                cityData = URL("http://api.openweathermap.org/geo/1.0/direct?q=${city}" +
                        "&limit=5&appid=0b6281885400ddf04c0b75fc7066989c").readText()
                reactions.say("City - ${city} ====== cityData - ${cityData}")
                record("", "done")
            }

/*            if (!cityData.isEmpty()) {
                action {
                    URL("https://api.openweathermap.org/data/2.5/onecall?" +
                                "lat=${CITY.lat}&lon=${CITY.lon}" +
                                "&exclude=hourly,daily&" +
                                "appid=0b6281885400ddf04c0b75fc7066989c"
                    ).readText()
                }
            }
*/
            state("done") {
                action {
                    reactions.alice?.endSession()
                }
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