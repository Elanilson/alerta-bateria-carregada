package br.com.apkdoandroid.alertadebateriacarregada

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import br.com.apkdoandroid.alertadebateriacarregada.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var batteryLevelTextView: TextView
  //  private lateinit var carregadoTextView: TextView
  //  private lateinit var conectadoTextView: TextView

    private lateinit var batteryLevelReceiver: BroadcastReceiver
    private lateinit var batteryStatusReceiver: BroadcastReceiver
    private lateinit var batteryImageView: ImageView
    private lateinit var handler: Handler
    private lateinit var binding: ActivityMainBinding
    private lateinit var runnable : Runnable
    private var pausarThread = false
    var notificacao : Notificacao? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryLevel = level * 100 / scale.toFloat()

            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
            val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            //val current = intent?.getIntExtra(BatteryManager.EXTRA_, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            // atualize o textView com as informações da bateria
            updateBatteryInfo(voltage, temperature, status)
            batteryLevelTextView.text = "${batteryLevel.toInt()}%"
            binding.textViewSaudeBateria.setText("${getSaudeBateria(health)}")

            intent?.let {
                when (it.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val batteryLevel = level * 100 / scale.toFloat()
                        val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging =
                            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                    status == BatteryManager.BATTERY_STATUS_FULL
                        val chargePlug = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                        val isChargerConnected =
                            chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                                    chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                        pausarThread = !isCharging
                        // Exibir informações sobre a bateria
                        Log.d("Bateria", "Levelx: $batteryLevel%")
                        Log.d("Bateria", "está carregando: $isCharging")
                        Log.d("Bateria", "O carregador está conectado: $isChargerConnected")
                      //  carregadoTextView.text = "está carregando: $isCharging"
                      //  conectadoTextView.text = "O carregador está conectado: $isChargerConnected"
                        Toast.makeText(this@MainActivity,"está carregando: $isCharging", Toast.LENGTH_SHORT).show()
                        updateBatteryLevel(batteryLevel.toInt()) // vai ser chamado novamento quando houver mudancas
                        if(batteryLevel.toInt() == 100){
                            pausarThread = true
                            // Define o ID único do canal de notificação
                            val channelId = getString(R.string.channel_id)
                            //verficar em outras versoes -----
                            // Cria o canal de notificação
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(
                                    channelId,
                                    getString(R.string.channel_name),
                                    NotificationManager.IMPORTANCE_DEFAULT
                                )
                                channel.description = getString(R.string.channel_description)
                                val notificationManager = getSystemService(NotificationManager::class.java)
                                notificationManager?.createNotificationChannel(channel)
                            }

                            var notificacao = Notificacao(this@MainActivity)
                            notificacao.setVibrationEnabled(true)
                            notificacao.sendNotification("Bateria 100% carregada ok", "A bateria do dispositivo está completamente carregada!")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler()
        batteryLevelTextView = findViewById(R.id.Main)
      //  carregadoTextView = findViewById(R.id.textViewCarregando)
      //  conectadoTextView = findViewById(R.id.textViewConectado)
        notificacao = Notificacao(this@MainActivity)
        notificacao?.cancelVibration()

    }


    private fun updateBatteryLevel(level: Int) {
        handler.post {
            when (level) {
                in 0..10 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_vermelho_xml)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_vermelho)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_black)
                    binding.bateria50.setImageResource(R.drawable.barra_black)
                    binding.bateria40.setImageResource(R.drawable.barra_black)
                    binding.bateria30.setImageResource(R.drawable.barra_black)
                    binding.bateria20.setImageResource(R.drawable.barra_black)
                    binding.bateria10.setImageResource(R.drawable.barra_vermelha)

                    if(level < 20){
                        binding.bateria20.setImageResource(R.drawable.barra_black)
                    }

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.vermelho))

                    animacaoNivelBateria(binding.bateria10,R.drawable.barra_vermelha)

                }
                in 11..20 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_vermelho_xml)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_vermelho)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_black)
                    binding.bateria50.setImageResource(R.drawable.barra_black)
                    binding.bateria40.setImageResource(R.drawable.barra_black)
                    binding.bateria30.setImageResource(R.drawable.barra_black)
                    binding.bateria20.setImageResource(R.drawable.barra_vermelha)
                    binding.bateria10.setImageResource(R.drawable.barra_vermelha)

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.vermelho))

                    animacaoNivelBateria(binding.bateria20,R.drawable.barra_vermelha)

                }
                in 21..30 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_amarelo)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_amarelo)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_black)
                    binding.bateria50.setImageResource(R.drawable.barra_black)
                    binding.bateria40.setImageResource(R.drawable.barra_black)
                    binding.bateria30.setImageResource(R.drawable.barra_amarela)
                    binding.bateria20.setImageResource(R.drawable.barra_amarela)
                    binding.bateria10.setImageResource(R.drawable.barra_amarela)

                    if(level < 30){
                        binding.bateria30.setImageResource(R.drawable.barra_black)
                    }

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.amarelo))


                    animacaoNivelBateria(binding.bateria30,R.drawable.barra_amarela)

                }
                in 31..40 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_amarelo)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_amarelo)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_black)
                    binding.bateria50.setImageResource(R.drawable.barra_black)
                    binding.bateria40.setImageResource(R.drawable.barra_amarela)
                    binding.bateria30.setImageResource(R.drawable.barra_amarela)
                    binding.bateria20.setImageResource(R.drawable.barra_amarela)
                    binding.bateria10.setImageResource(R.drawable.barra_amarela)

                    if(level < 40){
                        binding.bateria40.setImageResource(R.drawable.barra_black)
                    }
                    batteryLevelTextView.setTextColor(resources.getColor(R.color.amarelo))

                        animacaoNivelBateria(binding.bateria40,R.drawable.barra_amarela)

                }
                in 41..50 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_amarelo)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_amarelo)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_black)
                    binding.bateria50.setImageResource(R.drawable.barra_amarela)
                    binding.bateria40.setImageResource(R.drawable.barra_amarela)
                    binding.bateria30.setImageResource(R.drawable.barra_amarela)
                    binding.bateria20.setImageResource(R.drawable.barra_amarela)
                    binding.bateria10.setImageResource(R.drawable.barra_amarela)

                    if(level < 50){
                        binding.bateria50.setImageResource(R.drawable.barra_black)
                    }
                    batteryLevelTextView.setTextColor(resources.getColor(R.color.amarelo))

                        animacaoNivelBateria(binding.bateria50,R.drawable.barra_amarela)

                }
                in 51..60 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_amarelo)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_amarelo)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_black)
                    binding.bateria60.setImageResource(R.drawable.barra_amarela)
                    binding.bateria50.setImageResource(R.drawable.barra_amarela)
                    binding.bateria40.setImageResource(R.drawable.barra_amarela)
                    binding.bateria30.setImageResource(R.drawable.barra_amarela)
                    binding.bateria20.setImageResource(R.drawable.barra_amarela)
                    binding.bateria10.setImageResource(R.drawable.barra_amarela)
                    if(level < 60){
                        binding.bateria60.setImageResource(R.drawable.barra_black)
                    }
                    batteryLevelTextView.setTextColor(resources.getColor(R.color.amarelo))

                        animacaoNivelBateria(binding.bateria60,R.drawable.barra_amarela)

                }
                in 61..70 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_amarelo)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_amarelo)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_black)
                    binding.bateria70.setImageResource(R.drawable.barra_amarela)
                    binding.bateria60.setImageResource(R.drawable.barra_amarela)
                    binding.bateria50.setImageResource(R.drawable.barra_amarela)
                    binding.bateria40.setImageResource(R.drawable.barra_amarela)
                    binding.bateria30.setImageResource(R.drawable.barra_amarela)
                    binding.bateria20.setImageResource(R.drawable.barra_amarela)
                    binding.bateria10.setImageResource(R.drawable.barra_amarela)

                    if(level < 70){
                        binding.bateria70.setImageResource(R.drawable.barra_black)
                    }
                    batteryLevelTextView.setTextColor(resources.getColor(R.color.amarelo))

                        animacaoNivelBateria(binding.bateria70,R.drawable.barra_amarela)


                }
                in 71..80 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_verde)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_verde)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_black)
                    binding.bateria80.setImageResource(R.drawable.barra_verde)
                    binding.bateria70.setImageResource(R.drawable.barra_verde)
                    binding.bateria60.setImageResource(R.drawable.barra_verde)
                    binding.bateria50.setImageResource(R.drawable.barra_verde)
                    binding.bateria40.setImageResource(R.drawable.barra_verde)
                    binding.bateria30.setImageResource(R.drawable.barra_verde)
                    binding.bateria20.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)

                    if(level < 80){
                        binding.bateria80.setImageResource(R.drawable.barra_black)
                    }

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.verde))

                         animacaoNivelBateria(binding.bateria90,R.drawable.barra_verde)

                   // Toast.makeText(this@MainActivity,"74",Toast.LENGTH_SHORT).show()
                    Log.d("Bateria","74")
                }
                in 81..90 -> {
                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_verde)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_verde)

                    binding.bateria100.setImageResource(R.drawable.barra_black)
                    binding.bateria90.setImageResource(R.drawable.barra_verde)
                    binding.bateria80.setImageResource(R.drawable.barra_verde)
                    binding.bateria70.setImageResource(R.drawable.barra_verde)
                    binding.bateria60.setImageResource(R.drawable.barra_verde)
                    binding.bateria50.setImageResource(R.drawable.barra_verde)
                    binding.bateria40.setImageResource(R.drawable.barra_verde)
                    binding.bateria30.setImageResource(R.drawable.barra_verde)
                    binding.bateria20.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)

                    if(level < 90){
                        binding.bateria90.setImageResource(R.drawable.barra_black)
                    }

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.verde))

                        animacaoNivelBateria(binding.bateria90,R.drawable.barra_verde)



                }
                100 ->{
                    Log.d("Bateria","Carregado 100%")


                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_verde)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_verde)

                    binding.bateria100.setImageResource(R.drawable.barra_verde)
                    binding.bateria90.setImageResource(R.drawable.barra_verde)
                    binding.bateria80.setImageResource(R.drawable.barra_verde)
                    binding.bateria70.setImageResource(R.drawable.barra_verde)
                    binding.bateria60.setImageResource(R.drawable.barra_verde)
                    binding.bateria50.setImageResource(R.drawable.barra_verde)
                    binding.bateria40.setImageResource(R.drawable.barra_verde)
                    binding.bateria30.setImageResource(R.drawable.barra_verde)
                    binding.bateria20.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.verde))

                        animacaoNivelBateria(binding.bateria100,R.drawable.barra_verde)

                }
                else -> {

                    //batteryImageView.setImageResource(R.drawable.battery_100)
                    binding.imageViewTopBateria.setImageResource(R.drawable.battery_line_top_verde)
                    binding.imageViewBateria.setImageResource(R.drawable.battery_verde)

                    binding.bateria100.setImageResource(R.drawable.barra_verde)
                    binding.bateria90.setImageResource(R.drawable.barra_verde)
                    binding.bateria80.setImageResource(R.drawable.barra_verde)
                    binding.bateria70.setImageResource(R.drawable.barra_verde)
                    binding.bateria60.setImageResource(R.drawable.barra_verde)
                    binding.bateria50.setImageResource(R.drawable.barra_verde)
                    binding.bateria40.setImageResource(R.drawable.barra_verde)
                    binding.bateria30.setImageResource(R.drawable.barra_verde)
                    binding.bateria20.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)
                    binding.bateria10.setImageResource(R.drawable.barra_verde)

                    batteryLevelTextView.setTextColor(resources.getColor(R.color.verde))

                    animacaoNivelBateria(binding.bateria100,R.drawable.barra_verde)


                }
            }
        }
    }

    fun teste1(view: View){
        animacaoNivelBateria(binding.bateria50,R.drawable.barra_amarela)
    }
    fun teste2(view: View){
        animacaoNivelBateria(binding.bateria40,R.drawable.barra_vermelha)
    }


    private fun updateBatteryInfo( voltage: Int, celsius: Int, status: Int) {
     //   val textView = findViewById<TextView>(R.id.textViewInfo)
      /*  textView.append("Voltagem: ${voltage/1000.0} V\n")
        textView.append("Temperatura: ${temperature/10.0} °C\n")
        textView.append("Estado: ${getBatteryStatusString(status)}\n")*/
        // Converter para graus Fahrenheit
        var temperatura = celsius/10.0
        val fahrenheit = (temperatura * 9/5) + 32


        binding.textViewStatusBateria.setText(" ${getBatteryStatusString(status)}")
        binding.textViewTemperaturaBateria.setText("${String.format("%.1f", temperatura).replace(",",".")} °C  / ${String.format("%.1f", fahrenheit).replace(",",".")} °F")
        binding.textViewTensaoBateria.setText("${voltage/1000.0}V")
    }
    private fun getSaudeBateria(health : Int) : String{

        return  when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Boa"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Superaquecendo"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Morta"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensão"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Falha não especificada"
            else -> "Unknown"
        }
    }
    private fun animacaoNivelBateria( imageButton: ImageButton ,  drawable: Int){
        // Remove callbacks do Handler
        handler.removeCallbacksAndMessages(null)
        // Define o Runnable como nulo
        //runnable = null
        val transitionDrawable = TransitionDrawable(arrayOf(
            resources.getDrawable(R.drawable.barra_black),
            resources.getDrawable(drawable)
        ))

       // val handler = Handler()
         runnable = object : Runnable {
            override fun run() {
                if(pausarThread){
                    Log.d("ThreadAnimacao","Thread finalizada")
                  //  pausarThread = false
                    return
                }
                    Log.d("ThreadAnimacao","Thread inicializada")
                // verifique o nível de carga da bateria e atualize a interface do usuário
                imageButton.setImageDrawable(transitionDrawable)
                transitionDrawable.startTransition(1000) // tempo em milissegundos
                handler.postDelayed(this, 1000) // chame novamente após 5 segundos
            }
        }
        handler.postDelayed(runnable, 1000) // chame pela primeira vez após 5 segundos

    }

    private fun getBatteryStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Carregando"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
            BatteryManager.BATTERY_STATUS_FULL -> "Totalmente Carregada"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Não Carregando"
            else -> "Desconhecido"
        }
    }

    override fun onResume() {
        super.onResume()
        pausarThread = false
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryLevel = level * 100 / scale.toFloat()
        batteryLevelTextView.text = "${batteryLevel.toInt()}%"
        updateBatteryLevel(batteryLevel.toInt())
    }

     fun notificar(view : View){
         // Define o ID único do canal de notificação
         val channelId = getString(R.string.channel_id)
         //verficar em outras versoes -----
        // Cria o canal de notificação
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val channel = NotificationChannel(
                 channelId,
                 getString(R.string.channel_name),
                 NotificationManager.IMPORTANCE_DEFAULT
             )
             channel.description = getString(R.string.channel_description)
             val notificationManager = getSystemService(NotificationManager::class.java)
             notificationManager?.createNotificationChannel(channel)
         }


         notificacao?.setVibrationEnabled(true)
        notificacao?.sendNotification("Bateria 100% carregada", "A bateria do dispositivo está completamente carregada!")
    }

    fun paraVibrar(view: View){
        notificacao?.cancelVibration()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStop() {
        super.onStop()
        pausarThread = true
    }
}