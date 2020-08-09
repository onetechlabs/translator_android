package com.example.translator

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.jacksonandroidnetworking.JacksonParserFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {
    fun append(arr: Array<String>, element: String): Array<String> {
        val list: MutableList<String> = arr.toMutableList()
        list.add(element)
        return list.toTypedArray()
    }
    var fLangVal : String = "0"
    var tLangVal : String = "0"
    var t2translate : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AndroidNetworking.initialize(getApplicationContext())
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        var host: String= resources.getString(R.string.host_api)
        text_translated.setFocusable(false);
        text_translated.setClickable(true);

        val fLang: Spinner = from_lang
        val tLang: Spinner = to_lang
        var DropDownLangListitems = arrayOf<String>()
        var DropDownLangListItemValue = arrayOf<String>()
        AndroidNetworking.post(host+"/dropdown-language-lists")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    // do anything with response
                    var records = response.getJSONObject("data").getJSONArray("records")
                    for (i in 0 .. records.length()-1){
                        DropDownLangListitems = append(DropDownLangListitems, records.getJSONObject(i).getString("language_name"))
                        DropDownLangListItemValue = append(DropDownLangListItemValue, records.getJSONObject(i).getString("id"))
                        dropdownLanguages(DropDownLangListitems, DropDownLangListItemValue, fLang, tLang)
                    }
                    fLangVal=(records.length()-1).toString()
                    tLangVal=(records.length()-1).toString()
                }

                override fun onError(error: ANError) {
                    // handle error
                    var jsonString: String=error.errorBody
                    var jsonObject=JSONObject(jsonString)
                    toast(jsonObject.getString("message"))
                }
            })

        btn1.setOnClickListener {
            t2translate = text_to_translate.getText().toString()
            toast("Please wait .. Text Being Translated")
            AndroidNetworking.post(host+"/translate")
                .addBodyParameter("lang_id_origin", fLangVal)
                .addBodyParameter("lang_id_translated", tLangVal)
                .addBodyParameter("text", t2translate)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        // do anything with response
                        toast("Translation Success ..")
                        text_translated.setText(response.getJSONObject("data").getString("text_before_after"))
                    }

                    override fun onError(error: ANError) {
                        // handle error
                        var jsonString: String=error.errorBody
                        var jsonObject=JSONObject(jsonString)
                        toast(jsonObject.getString("message"))
                    }
                })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.about_app -> {
                aboutApp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun aboutApp() {
        val aboutPage = Intent(this, AppInfoActivity::class.java).apply {}
        startActivity(aboutPage)
    }

    fun dropdownLanguages(DropDownLangListitems: Array<String>, DropDownLangListItemValue: Array<String>, fLang: Spinner, tLang: Spinner){
        println(DropDownLangListitems.contentToString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DropDownLangListitems)
        fLang.adapter = adapter
        tLang.adapter = adapter
        fLang.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val id: String = DropDownLangListItemValue.get(i)
                fLangVal=id
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })
        tLang.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val id: String = DropDownLangListItemValue.get(i)
                tLangVal=id
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })
    }
}