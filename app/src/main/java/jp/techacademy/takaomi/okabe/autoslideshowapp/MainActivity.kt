package jp.techacademy.takaomi.okabe.autoslideshowapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Build
import android.util.Log

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    private var now_id :Long = 0
    private var first_id :Long = 0
    private var last_id :Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        start_stop_button.setOnClickListener {
            if(start_stop_button.text == "停止") {
                start_stop_button.text = "再生"
                go_button.isEnabled = true
                back_button.isEnabled = true
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                    mTimerSec = 0.0
                    timer.text = String.format("%.1f", mTimerSec)
                }
            }else{
                start_stop_button.text = "停止"
                go_button.isEnabled = false
                back_button.isEnabled = false
                if (mTimer == null){
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mTimerSec += 0.1
                            if(mTimerSec == 0.1){
                                now_id++
                                if(now_id > last_id){
                                    now_id = first_id
                                }
                            }else if(mTimerSec > 2.0){
                                mTimerSec = 0.0
                            }
                            mHandler.post {
                                var imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, now_id)
                                imageView.setImageURI(imageUri)
                                timer.text = String.format("%.1f", mTimerSec)
                            }
                        }
                    }, 2000, 100) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                }
            }
        }

        go_button.setOnClickListener {
            now_id++
            if(now_id > last_id){
                now_id = first_id
            }
            var imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, now_id)
            imageView.setImageURI(imageUri)
        }

        back_button.setOnClickListener {
            now_id--
            if(now_id < first_id){
                now_id = last_id
            }
            var imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, now_id)
            imageView.setImageURI(imageUri)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            var num :Int = 0
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                num++
                if ( num == 1 ) {
                    now_id = id
                    first_id = id
                }
                last_id = id
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, first_id)
                imageView.setImageURI(imageUri)
                Log.d("ANDROID", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}