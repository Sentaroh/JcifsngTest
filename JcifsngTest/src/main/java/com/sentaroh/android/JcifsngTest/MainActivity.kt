package com.sentaroh.android.JcifsngTest

/*
The MIT License (MIT)
Copyright (c) 2011-2019 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import androidx.preference.PreferenceManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TabStopSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.sentaroh.android.Utilities3.Dialog.CommonDialog
import com.sentaroh.android.Utilities3.NotifyEvent
import com.sentaroh.android.Utilities3.StringUtil

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.LoggerWriter
import org.w3c.dom.Text

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Properties

class MainActivity : AppCompatActivity() {

    private var mRunButton: Button? = null
    private var mCopyButton: Button? = null

    private var mMainView: TextView? = null
    private var mRunStatus: TextView? = null
    private var mMainScrollView: ScrollView? = null
    private var mLogText = ""
    private var mHndl: Handler? = null
    private var mContext: Context? = null
    private var mCommonDlg: CommonDialog? = null

    private var mDomainName: EditText? = null
    private var mAccountName: EditText? = null
    private var mAccountPassword: EditText? = null

    private var mLogLineCount = 0

    private var mActivity: MainActivity? = null

    private val KEY_DOMAIN_NAME = "domain"
    private val KEY_ACCOUNT_NAME = "account"
    private val KEY_ACCOUNT_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        mActivity = this
        mContext = mActivity!!.applicationContext

        mRunButton = findViewById<View>(R.id.run_test_code) as Button
        mCopyButton = findViewById<View>(R.id.main_copy_clipboard) as Button
        mMainView = findViewById<View>(R.id.main_log_view) as TextView
        mRunStatus = findViewById<View>(R.id.main_status) as TextView

        mMainScrollView = findViewById<View>(R.id.main_scroll_view) as ScrollView

        val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
        mDomainName = findViewById<View>(R.id.main_domain_name) as EditText
        mDomainName!!.setText(prefs.getString(KEY_DOMAIN_NAME, ""))

        mAccountName = findViewById<View>(R.id.main_account_name) as EditText
        mAccountName!!.setText(prefs.getString(KEY_ACCOUNT_NAME, ""))

        mAccountPassword = findViewById<View>(R.id.main_account_password) as EditText
        mAccountPassword!!.setText(prefs.getString(KEY_ACCOUNT_PASSWORD, ""))

        val lw = Slf4LogWriter()

        mLog.setLogOption(true, true, true, false, true)
        mLog.setWriter(lw)

        mHndl = Handler()

        mCommonDlg = CommonDialog(mContext, supportFragmentManager)

        checkStoragePermission()

//        val lf0=File("/storage/C8A9-1BE9/zz0");
//        val lf1=File("/storage/C8A9-1BE9/zz1");
//        val lf2=File("/storage/C8A9-1BE9/zz2");
//
//        val time=System.currentTimeMillis();
//        Log.v("JCFS-NG test","time="+time+", date="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(time));
//        Log.v("JCFS-NG test","zz0="+lf0.lastModified()+", date="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(lf0.lastModified()));
//        Log.v("JCFS-NG test","zz1="+lf1.lastModified()+", date="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(lf1.lastModified()));
//        Log.v("JCFS-NG test","zz2="+lf2.lastModified()+", date="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(lf2.lastModified()));
    }

    override fun onResume() {
        super.onResume()
        mRunButton!!.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
            prefs.edit().putString(KEY_DOMAIN_NAME, mDomainName!!.text.toString())
                    .putString(KEY_ACCOUNT_NAME, mAccountName!!.text.toString())
                    .putString(KEY_ACCOUNT_PASSWORD, mAccountPassword!!.text.toString())
                    .commit()

            mLogText = ""
            mLogLineCount = 0
            showText(mMainView, mLogText)
            showText(mRunStatus, "Running")
            mRunButton!!.isEnabled = false
            performTest()
        }
        mCopyButton!!.setOnClickListener {
            val cm = mContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
//            val cd = cm.primaryClip
            cm.setPrimaryClip(ClipData.newPlainText("JcifsngTest", mMainView!!.text.toString()))
            Toast.makeText(mContext, "jcifs-ng log was copied to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLog.setWriter(null)
        /* aa */
    }

    private fun checkStoragePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE == requestCode) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                val ntfy_term = NotifyEvent(mContext)
                ntfy_term.setListener(object : NotifyEvent.NotifyEventListener {
                    override fun positiveResponse(c: Context, o: Array<Any>) {
                        finish()
                    }

                    override fun negativeResponse(c: Context, o: Array<Any>) {}
                })
                mCommonDlg!!.showCommonDialog(false, "E", "Application was termiated because Storage Access not granted.", "", ntfy_term)
            }
        }
    }

    private fun performTest() {
        val th = object : Thread() {
            override fun run() {
                try {
                    val prop = Properties()
                    TestCode.runTest(mActivity!!, prop,
                            mDomainName!!.text.toString(), mAccountName!!.text.toString(), mAccountPassword!!.text.toString())

                    cleanup()
                } catch (e: Exception) {
                    putLogMsg(e.toString())
                    cleanup()
                }

            }
        }
        th.start()
    }

    private fun scrollToBottom() {
        mHndl!!.post { mMainScrollView!!.fullScroll(View.FOCUS_DOWN) }
    }


    private fun cleanup() {
        showText(mRunStatus, "Terminated")
        writeLogFile()
        mHndl!!.post {
            mMainScrollView!!.fullScroll(View.FOCUS_DOWN)
            mRunButton!!.isEnabled = true
        }
        System.gc()
    }

    fun putLogMsg(msg: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val ts = sdf.format(System.currentTimeMillis())
        Log.v("JcifsNgTest", msg)
        mLogText += "$ts $msg\n"
        showText(mMainView, mLogText)
        mLogLineCount++
    }

    private fun writeLogFile() {
        try {
            val fl = getExternalFilesDirs(null)
            val out = File(fl[0], "JcifsNgTest_log.txt")
            val fw = FileWriter(out)
            val bw = BufferedWriter(fw, 1024 * 1024 * 4)
            bw.write(mLogText)
            bw.flush()
            bw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showText(tv: TextView?, msg: String) {
        mHndl!!.post {
            tv!!.text = msg
            //                mMainScrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    internal inner class Slf4LogWriter : LoggerWriter() {
        override fun write(msg: String) {
            putLogMsg(msg)
        }
    }

    companion object {

        private val mLog = LoggerFactory.getLogger(MainActivity::class.java)

        private val REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 10
    }

}
