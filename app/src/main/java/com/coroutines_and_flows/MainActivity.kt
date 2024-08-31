package com.coroutines_and_flows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.coroutines_and_flows.ui.theme.Coroutines_and_flowsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Coroutines_and_flowsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val pp = Json { // this returns the JsonBuilder
                            prettyPrint = true
                        }
                        val data = main()
                        lateinit var pokemonList: Map<String, JsonElement>

                        try {
                            fun jsonStringToMapWithKotlinx(_json: String): Map<String, JsonElement> {
                                val json = Json.parseToJsonElement(_json)
                                require(json is JsonObject) { "Only JSON Objects can be converted to a Map!" }
                                return json
                            }
                            pokemonList = jsonStringToMapWithKotlinx(data)

                        } catch (err: Exception) {
                            err.printStackTrace()
                        }

                        LazyColumn (
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {

                            try {
                                var result = pokemonList["results"]?.jsonArray?.asIterable()
                                if (result != null) {
                                    result = result.toList()
                                    items(result.size) { i ->
                                        Box(
                                            modifier = Modifier
                                                .height(300.dp)
                                                .width(300.dp)
                                                .border(
                                                    4.dp,
                                                    color = Color.Black,
                                                    shape = RoundedCornerShape(25.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = AnnotatedString(result[i].toString()))
                                        }
                                    }
                                }
                            } catch (err: Exception) {
                                err.printStackTrace()
                            }

                            item {
                                Text(text = "Foobar")
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun doWork(): String {
    val _url: String = "https://pokeapi.co/api/v2/pokemon?limit=151"
    var response: StringBuilder = StringBuilder()

    try {
        withContext(Dispatchers.IO) {
            val url: URL = URI.create(_url).toURL()
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            val responseCode: Int = connection.responseCode

            println("response code :: ${ responseCode }")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
            }
        }
        println("response : ${ response }")

    } catch (err: Exception) {
        err.printStackTrace()
    }

    return response.toString()
}

@Composable
fun main(): String = runBlocking {
    return@runBlocking doWork()
}
