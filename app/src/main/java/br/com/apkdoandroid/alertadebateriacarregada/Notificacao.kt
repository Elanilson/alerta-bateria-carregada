package br.com.apkdoandroid.alertadebateriacarregada
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
class Notificacao (private val context: Context) : NotificationListenerService() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val VIBRATION_ENABLED_KEY = "vibration_enabled"
        private const val SHARED_PREFS_NAME = "notification_prefs"
    }
    private val pattern = longArrayOf(0, 100, 500, 200, 500)
    private var vibrator: Vibrator? = null
    private var isVibrating = false
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    fun sendNotification(title: String, description: String) {
         vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Define a intent que será disparada quando a notificação for clicada
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        if(Build.VERSION.SDK_INT >= 26){
            // Add vibration if enabled
            if (isVibrationEnabled()) {
                // criando a vibração
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.EFFECT_TICK))
            }
        }

        // Constrói a notificação
        val notificationBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.barra_amarela)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(pattern)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL or NotificationCompat.DEFAULT_VIBRATE)
        // Dispara a notificação
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    private fun isVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(VIBRATION_ENABLED_KEY, true)
    }
    fun setVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(VIBRATION_ENABLED_KEY, enabled).apply()
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // chamada quando a notificação é removida
        vibrator?.cancel()
    }

    fun cancelVibration() {
        vibrator?.cancel()
    }

}