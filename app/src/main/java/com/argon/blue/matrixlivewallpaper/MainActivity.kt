package com.argon.blue.matrixlivewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.github.dhaval2404.colorpicker.model.ColorSwatch;


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 获取状态栏的 Window 对象
        val window: Window = window

        // 设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.BLACK // 替换为你想要的颜色

        val spinner: Spinner = findViewById(R.id.charset_spinner)
        val options = resources.getStringArray(R.array.charset_spinner_options)
        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 在选项选中时执行的代码
                val selectedOption = parent?.getItemAtPosition(position).toString()
                // 处理选中的选项
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 当没有选项选中时执行的代码
            }
        }

        val textColorPicker:View = findViewById(R.id.text_color_card)
        textColorPicker.setOnClickListener(View.OnClickListener {
            Log.d("GAGA", "oh click!")
            MaterialColorPickerDialog
                .Builder(this@MainActivity)
                .setTitle("Pick Text Color")
                .setColorShape(ColorShape.CIRCLE)
                .setColorSwatch(ColorSwatch._500)
                .setDefaultColor(com.github.dhaval2404.colorpicker.R.color.green_500)
                .setTickColorPerCard(false)
                .setColorListener(object : ColorListener {
                    override fun onColorSelected(color: Int, colorHex: String) {
                        // Handle Color Selection
                        //Log.e("TD_TRACE", "select color: $color")
                        //mLEDView.setTextColor(color)
                        //prefUtil.putTextColor(color)
                        //textColorIndicator.setIconTint(ColorStateList.valueOf(color))
                    }
                })
                .showBottomSheet(supportFragmentManager)
        })
    }


    fun onClick(view: View) {
        Log.d("GG","onClick")
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, MatrixLiveWallpaperService::class.java)
        )
        startActivity(intent);
    }
}