package com.arc.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arc.pomodoro.ui.theme.PomodoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroApp()
        }
    }
}

enum class TimerModel(val label:String, val duration:Int, val gradient:List<Color>){
    Work("Work",25*60, listOf(Color.Red,Color.Yellow)),
    ShortBreak("Short Break",5*60, listOf(Color.Green,Color.Blue)),
    LongBreak("Long Break",15*60, listOf(Color.Cyan,Color.Magenta))
}

class PomodoroViewModel: ViewModel(){
    var timerMode by mutableStateOf(TimerModel.Work)
    var timeRemaining by mutableStateOf(timerMode.duration)
    var isActive by mutableStateOf(false)
    var showPopup by mutableStateOf(false)
    private var timer: CountDownTimer? = null

    fun startTimer(){
        if(isActive) return
        isActive = true
        timer = object : CountDownTimer((timeRemaining*1000).toLong(),1000){
            override fun onTick(milisUntilFinised: Long) {
                timeRemaining = (milisUntilFinised/1000).toInt()
            }
            override fun onFinish() {
                isActive = false
                showPopup = true
            }
        }.start()
    }

    fun pauseTimer(){
        timer?.cancel()
        isActive = false
    }

    fun resetTimer(){
        timer?.cancel()
        isActive = false
        timeRemaining = timerMode.duration
    }
}

@Composable
fun PomodoroApp(viewModel: PomodoroViewModel = viewModel()) {
    val progress by animateFloatAsState(
        targetValue = 1f - (viewModel.timeRemaining.toFloat() / viewModel.timerMode.duration),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(viewModel.timerMode.gradient)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ModeSelector(viewModel)
            TimerCircle(progress, viewModel.timeRemaining)
            TimerControls(viewModel)
        }
    }
}

@Composable
fun ModeSelector(viewModel: PomodoroViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TimerModel.values().forEach { mode ->
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                onClick = {
                    viewModel.timerMode = mode
                    viewModel.resetTimer()
                },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(mode.label)
            }
        }
    }
}



@Composable
fun TimerCircle(progress:Float, timeRemaining:Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 40f
            val radius = size.minDimension / 2
            val arcRect = android.graphics.RectF(
                strokeWidth / 2,
                strokeWidth / 2,
                size.width - strokeWidth / 2,
                size.height - strokeWidth / 2
            )

            // Latar belakang lingkaran transparan
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Lingkaran progres dengan efek trim
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Teks Timer
        Text(
            text = "%02d:%02d".format(timeRemaining / 60, timeRemaining % 60),
            fontSize = 32.sp,
            color = Color.White
        )
    }
}

@Composable
fun TimerControls(viewModel: PomodoroViewModel){
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)){
        IconButton(onClick = { if (viewModel.isActive) viewModel.pauseTimer() else viewModel.startTimer()}) {
            Icon(imageVector = if (viewModel.isActive) ImageVector.vectorResource(R.drawable.baseline_pause_24) else ImageVector.vectorResource(R.drawable.baseline_play_arrow_24), contentDescription = "play/pause", tint = Color.White)
        }
        IconButton(onClick = { viewModel.resetTimer() }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PomodoroTheme {
        Greeting("Android")
    }
}