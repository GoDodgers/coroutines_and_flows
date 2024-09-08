package com.coroutines_and_flows

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.coroutines_and_flows.ui.theme.Coroutines_and_flowsTheme
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.concurrent.thread

import org.slf4j.Logger
import org.slf4j.LoggerFactory

var logg: Logger = LoggerFactory.getLogger("coroutines_and_flows")


class MainActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {

        logg.debug("logg foobar :: doWork() :: main activity")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Coroutines_and_flowsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                        Text("foobar")
                        var scope = rememberCoroutineScope()

                        scope.launch {
                            var foo: Deferred<String> = async {  doWork() }
                            println(foo.await())
                        }
                    }
                }
            }
        }
    }
}

suspend fun doSomething(): String {
    // continuation - data structure
    delay(5000L) // suspends / blocks
    // continuation restored
    return "something"
}

// structured concurrency
suspend fun doSomethingElse(): String {
    delay(5000L)
    return "something else"
}

suspend fun doEverythingElse(): String {
    delay(4000L)
    return "everything else"
}

suspend fun doSequentially(): String {
    // starts context for coroutines
    // parent
    coroutineScope {
        // child
        doSomething()
        // isolated from other coroutineScopes
        // parrallel code needs to finish before scope is closed
    }

    coroutineScope {
        doSomethingElse()
        // parrallel code needs to finish before scope is closed
    }
    return "foobar"
}

suspend fun doConcurrently() {
    // parent
    coroutineScope {
        // child
        launch {
            doSomething()
        }
        // launch a new coroutie that will run in parrallel  new Thread(() -> ...)
        launch {
            doSomethingElse()
        }
    }
}

// No Parent
 suspend fun doUnStructuredConcurrency() {
     // Main ( global scope ) thread needs to have enough time to complete
     GlobalScope.launch {
         doSomething()
     }
     GlobalScope.launch {
         doSomethingElse()
     }
 }

// (doSomething, doSomethingElse) => { something after both are done }
suspend fun doSequentiallyThenConcurrently() {
    coroutineScope {
        var job1: Job = launch { doSomething() }
        var job2: Job = launch { doSomethingElse() }
        job1.join()
        job2.join()
        launch {
            doEverythingElse()
        }
    }
}

suspend fun doSequentiallyThenConcurrentlyAlt() {
    coroutineScope {
        coroutineScope {
            launch { doSomething() }
            launch { doSomethingElse() }
        }
        // CoroutineName ( name coroutine ) + Dispatchers == coroutine context
        launch (CoroutineName("foobar") + Dispatchers.Default) {
            doSomethingElse()
        }
    }
}

suspend fun doEverything(): String {
    coroutineScope {
        // deffered ( analogous to Future type )
        var foo = async { doSomething() }
        var bar = async { doSomethingElse() }
        // semantic blocking
        println("foo.await():: ${foo.await()}")
        println("bar.await():: ${bar.await()}")
    }
    return ""
}

suspend fun doWork(): String {
//    return@runBlocking doWork()
    try {
        logg.debug("logg foobar :: doWork() :: before doEverything()")
    } catch (err: Exception) {
        println(err)
        println("println foobar doWork() :: inside catch")
    }
    println("println foobar :: doWork() :: before doEverything()")
    return doEverything()
}

/**
 * Basically, there are 3 variables you need to consider:
 *  1. What's your current execution environment? Synchronous (i.e., regular code) or async (a suspend function)
 *  2. What code are you trying to run? Synchronous or async
 *  3. Do you want that code to run synchronously (wait until it completes) or async
 *
 * That's 2 * 2 * 2, or 8 possibilities. I've given you examples in all 8
 */

/**
 * A sample synchronous function
 */
fun syncHello() {
    println("Sync Hello")
}

/**
 * A sample asynchronous function
 */
suspend fun asyncHello() = coroutineScope {
    println("Async Hello")
}

/****** In "sync" code ******/

fun syncHaveSyncCodeWantSyncExecution() {
    // Just do a normal function call
    syncHello()
}

fun syncHaveSyncCodeWantAsyncExecution() {
    // Get (or create) a scope and launch for fire-and-forget
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        syncHello()
    }
    // Use `async` method if you want to track or cancel the execution
    val d = scope.async {
        syncHello()
    }
    d.cancel()
}

fun syncHaveAsyncCodeWantSyncExecution() {
    // Use runBlocking to execute async code synchronously
    runBlocking {
        asyncHello()
    }
}

fun syncHaveAsyncCodeWantAsyncExecution() {
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        asyncHello()
    }
    val d = scope.async {
        syncHello()
    }
    d.cancel()
}

/****** In "async" code ******/
suspend fun asyncHaveSyncCodeWantSyncExecution() {
    // Just "normal" function call
    syncHello()
}

suspend fun asyncHaveSyncCodeWantAsyncExecution() {
    // Can reuse existing coroutine scope to run sync code
    coroutineScope {
        syncHello()
    }

    // Synchronous code can't be cancelled so you can try putting expensive computation
    // on a separate thread using the Dispatchers.Default context
    withContext(Dispatchers.Default) {
        syncHello()
    }
}

suspend fun asyncHaveAsyncCodeWantSyncExecution() {
    // Just "normal" function call
    asyncHello()
}

suspend fun asyncHaveAsyncCodeWantAsyncExecution() {
    // Can reuse existing coroutine scope to run async code
    coroutineScope {
        asyncHello()
    }

    // Create a new coroutine context if you want to handle the cancellation of the
    // jobs separately
    val fiveSecondMaxScope = CoroutineScope(Dispatchers.IO)
    fiveSecondMaxScope.launch { syncHello() }
    delay(5000)
    fiveSecondMaxScope.cancel("Time's up", CancellationException())
}
